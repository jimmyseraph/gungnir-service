package vip.testops.gungnir.agent.internal;

import org.jacoco.core.internal.ContentTypeDetector;
import org.jacoco.core.internal.InputStreams;
import org.jacoco.core.internal.Pack200Streams;
import org.jacoco.core.internal.flow.ClassProbesAdapter;
import org.jacoco.core.internal.instr.*;
import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import vip.testops.gungnir.agent.internal.data.GungnirCRC64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.*;

public class GungnirInstrumenter {
    private final IExecutionDataAccessorGenerator accessorGenerator;

    private final SignatureRemover signatureRemover;

    public GungnirInstrumenter(final IExecutionDataAccessorGenerator runtime){
        this.accessorGenerator = runtime;
        this.signatureRemover = new SignatureRemover();
    }

    public void setRemoveSignatures(final boolean flag) {
        signatureRemover.setActive(flag);
    }

    private byte[] instrument(final byte[] className, final byte[] source) {
        final long classId = GungnirCRC64.classId(className);
        final ClassReader reader = InstrSupport.classReaderFor(source);
        final ClassWriter writer = new ClassWriter(reader, 0) {
            @Override
            protected String getCommonSuperClass(final String type1,
                                                 final String type2) {
                throw new IllegalStateException();
            }
        };
        final IProbeArrayStrategy strategy = ProbeArrayStrategyFactory
                .createFor(classId, reader, accessorGenerator);
        final int version = InstrSupport.getMajorVersion(reader);
        final ClassVisitor visitor = new ClassProbesAdapter(
                new ClassInstrumenter(strategy, writer),
                InstrSupport.needsFrames(version));
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        return writer.toByteArray();
    }

    public byte[] instrument(final byte[] buffer, final String name)
            throws IOException {
        try {
            return instrument(name.getBytes(StandardCharsets.UTF_8), buffer);
        } catch (final RuntimeException e) {
            throw instrumentError(name, e);
        }
    }

    public byte[] instrument(final InputStream input, final String name)
            throws IOException {
        final byte[] bytes;
        try {
            bytes = InputStreams.readFully(input);
        } catch (final IOException e) {
            throw instrumentError(name, e);
        }
        return instrument(bytes, name);
    }

    public void instrument(final InputStream input, final OutputStream output,
                           final String name) throws IOException {
        output.write(instrument(input, name));
    }

    private IOException instrumentError(final String name,
                                        final Exception cause) {
        final IOException ex = new IOException(
                String.format("Error while instrumenting %s.", name));
        ex.initCause(cause);
        return ex;
    }

    public int instrumentAll(final InputStream input, final OutputStream output,
                             final String name) throws IOException {
        final ContentTypeDetector detector;
        try {
            detector = new ContentTypeDetector(input);
        } catch (final IOException e) {
            throw instrumentError(name, e);
        }
        switch (detector.getType()) {
            case ContentTypeDetector.CLASSFILE:
                instrument(detector.getInputStream(), output, name);
                return 1;
            case ContentTypeDetector.ZIPFILE:
                return instrumentZip(detector.getInputStream(), output, name);
            case ContentTypeDetector.GZFILE:
                return instrumentGzip(detector.getInputStream(), output, name);
            case ContentTypeDetector.PACK200FILE:
                return instrumentPack200(detector.getInputStream(), output, name);
            default:
                copy(detector.getInputStream(), output, name);
                return 0;
        }
    }

    private int instrumentZip(final InputStream input,
                              final OutputStream output, final String name) throws IOException {
        final ZipInputStream zipin = new ZipInputStream(input);
        final ZipOutputStream zipout = new ZipOutputStream(output);
        ZipEntry entry;
        int count = 0;
        while ((entry = nextEntry(zipin, name)) != null) {
            final String entryName = entry.getName();
            if (signatureRemover.removeEntry(entryName)) {
                continue;
            }

            final ZipEntry newEntry = new ZipEntry(entryName);
            newEntry.setMethod(entry.getMethod());
            switch (entry.getMethod()) {
                case ZipEntry.DEFLATED:
                    zipout.putNextEntry(newEntry);
                    count += filterOrInstrument(zipin, zipout, name, entryName);
                    break;
                case ZipEntry.STORED:
                    // Uncompressed entries must be processed in-memory to calculate
                    // mandatory entry size and CRC
                    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    count += filterOrInstrument(zipin, buffer, name, entryName);
                    final byte[] bytes = buffer.toByteArray();
                    newEntry.setSize(bytes.length);
                    newEntry.setCompressedSize(bytes.length);
                    newEntry.setCrc(crc(bytes));
                    zipout.putNextEntry(newEntry);
                    zipout.write(bytes);
                    break;
                default:
                    throw new AssertionError(entry.getMethod());
            }
            zipout.closeEntry();
        }
        zipout.finish();
        return count;
    }

    private int filterOrInstrument(final InputStream in, final OutputStream out,
                                   final String name, final String entryName) throws IOException {
        if (signatureRemover.filterEntry(entryName, in, out)) {
            return 0;
        } else {
            return instrumentAll(in, out, name + "@" + entryName);
        }
    }

    private static long crc(final byte[] data) {
        final CRC32 crc = new CRC32();
        crc.update(data);
        return crc.getValue();
    }

    private ZipEntry nextEntry(final ZipInputStream input,
                               final String location) throws IOException {
        try {
            return input.getNextEntry();
        } catch (final IOException e) {
            throw instrumentError(location, e);
        }
    }

    private int instrumentGzip(final InputStream input,
                               final OutputStream output, final String name) throws IOException {
        final GZIPInputStream gzipInputStream;
        try {
            gzipInputStream = new GZIPInputStream(input);
        } catch (final IOException e) {
            throw instrumentError(name, e);
        }
        final GZIPOutputStream gzout = new GZIPOutputStream(output);
        final int count = instrumentAll(gzipInputStream, gzout, name);
        gzout.finish();
        return count;
    }

    private int instrumentPack200(final InputStream input,
                                  final OutputStream output, final String name) throws IOException {
        final InputStream unpackedInput;
        try {
            unpackedInput = Pack200Streams.unpack(input);
        } catch (final IOException e) {
            throw instrumentError(name, e);
        }
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final int count = instrumentAll(unpackedInput, buffer, name);
        Pack200Streams.pack(buffer.toByteArray(), output);
        return count;
    }

    private void copy(final InputStream input, final OutputStream output,
                      final String name) throws IOException {
        final byte[] buffer = new byte[1024];
        int len;
        while ((len = read(input, buffer, name)) != -1) {
            output.write(buffer, 0, len);
        }
    }

    private int read(final InputStream input, final byte[] buffer,
                     final String name) throws IOException {
        try {
            return input.read(buffer);
        } catch (final IOException e) {
            throw instrumentError(name, e);
        }
    }
}
