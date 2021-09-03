package vip.testops.gungnir.agent.core.data;

import org.jacoco.core.data.*;
import org.jacoco.core.internal.data.CompactDataInput;

import java.io.IOException;
import java.io.InputStream;

import static java.lang.String.format;

public class GungnirExecutionDataReader {

    protected final CompactDataInput in;

    private ISessionInfoVisitor sessionInfoVisitor = null;

    private IExecutionDataVisitor executionDataVisitor = null;

    private IExtraInfoVisitor extraInfoVisitor = null;

    private boolean firstBlock = true;

    public GungnirExecutionDataReader(final InputStream input) {
        this.in = new CompactDataInput(input);
    }

    public void setSessionInfoVisitor(final ISessionInfoVisitor visitor) {
        this.sessionInfoVisitor = visitor;
    }

    public void setExecutionDataVisitor(final IExecutionDataVisitor visitor) {
        this.executionDataVisitor = visitor;
    }

    public void setExtraInfoVisitor(final IExtraInfoVisitor visitor) {
        this.extraInfoVisitor = visitor;
    }

    public boolean read() throws IOException, IncompatibleExecDataVersionException {
        byte type;
        do {
            int i = in.read();
            if (i == -1) {
                return false; // EOF
            }
            type = (byte) i;
            if (firstBlock && type != GungnirExecutionDataWriter.BLOCK_HEADER) {
                throw new IOException("Invalid execution data file.");
            }
            firstBlock = false;
        } while (readBlock(type));
        return true;
    }

    protected boolean readBlock(final byte blocktype) throws IOException {
        switch (blocktype) {
            case GungnirExecutionDataWriter.BLOCK_HEADER:
                readHeader();
                return true;
            case GungnirExecutionDataWriter.BLOCK_SESSIONINFO:
                readSessionInfo();
                return true;
            case GungnirExecutionDataWriter.BLOCK_EXTRAINFO:
                readExtraInfo();
                return true;
            case GungnirExecutionDataWriter.BLOCK_EXECUTIONDATA:
                readExecutionData();
                return true;
            default:
                throw new IOException(
                        format("Unknown block type %x.", Byte.valueOf(blocktype)));
        }
    }

    private void readHeader() throws IOException {
        if (in.readChar() != GungnirExecutionDataWriter.MAGIC_NUMBER) {
            throw new IOException("Invalid execution data file.");
        }
        final char version = in.readChar();
        if (version != GungnirExecutionDataWriter.FORMAT_VERSION) {
            throw new IncompatibleExecDataVersionException(version);
        }
    }

    private void readSessionInfo() throws IOException {
        if (sessionInfoVisitor == null) {
            throw new IOException("No session info visitor.");
        }
        final String id = in.readUTF();
        final long start = in.readLong();
        final long dump = in.readLong();
        sessionInfoVisitor.visitSessionInfo(new SessionInfo(id, start, dump));
    }

    private void readExtraInfo() throws IOException {
        if(extraInfoVisitor == null) {
            throw new IOException("No extra info visitor.");
        }
        final String projectName = in.readUTF();
        extraInfoVisitor.visitExtraInfo(new ExtraInfo(projectName));
    }

    private void readExecutionData() throws IOException {
        if (executionDataVisitor == null) {
            throw new IOException("No execution data visitor.");
        }
        final long id = in.readLong();
        final String name = in.readUTF();
        final boolean[] probes = in.readBooleanArray();
        executionDataVisitor.visitClassExecution(new ExecutionData(id, name, probes));
    }
}
