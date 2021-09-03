package vip.testops.gungnir.agent.core.data;

import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.internal.data.CompactDataOutput;
import vip.testops.gungnir.agent.core.data.ExtraInfo;
import vip.testops.gungnir.agent.core.data.IExtraInfoVisitor;

import java.io.IOException;
import java.io.OutputStream;

public class GungnirExecutionDataWriter implements IExtraInfoVisitor, ISessionInfoVisitor, IExecutionDataVisitor {

    public static final char FORMAT_VERSION;

    static {
        // Runtime initialize to ensure javac does not inline the value.
        FORMAT_VERSION = 0x1007;
    }

    /** Magic number in header for file format identification. */
    public static final char MAGIC_NUMBER = 0xC0C0;

    /** Block identifier for file headers. */
    public static final byte BLOCK_HEADER = 0x01;

    /** Block identifier for session information. */
    public static final byte BLOCK_SESSIONINFO = 0x10;

    /** Block identifier for execution data of a single class. */
    public static final byte BLOCK_EXECUTIONDATA = 0x11;

    public static final byte BLOCK_EXTRAINFO = 0x12;

    /** Underlying data output */
    protected final CompactDataOutput out;

    public GungnirExecutionDataWriter(final OutputStream output) throws IOException {
        this.out = new CompactDataOutput(output);
        writeHeader();
    }

    private void writeHeader() throws IOException {
        out.writeByte(BLOCK_HEADER);
        out.writeChar(MAGIC_NUMBER);
        out.writeChar(FORMAT_VERSION);
    }

    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void visitClassExecution(ExecutionData data) {
        if (data.hasHits()) {
            try {
                out.writeByte(BLOCK_EXECUTIONDATA);
                out.writeLong(data.getId());
                out.writeUTF(data.getName());
                out.writeBooleanArray(data.getProbes());
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void visitSessionInfo(SessionInfo info) {
        try {
            out.writeByte(BLOCK_SESSIONINFO);
            out.writeUTF(info.getId());
            out.writeLong(info.getStartTimeStamp());
            out.writeLong(info.getDumpTimeStamp());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visitExtraInfo(ExtraInfo info) {
        try {
            out.writeByte(BLOCK_EXTRAINFO);
            out.writeUTF(info.getProjectName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
