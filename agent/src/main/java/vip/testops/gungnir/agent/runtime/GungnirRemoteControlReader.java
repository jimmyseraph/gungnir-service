package vip.testops.gungnir.agent.runtime;

import org.jacoco.core.runtime.IRemoteCommandVisitor;
import org.jacoco.core.runtime.RemoteControlWriter;
import vip.testops.gungnir.agent.core.data.GungnirExecutionDataReader;

import java.io.IOException;
import java.io.InputStream;

public class GungnirRemoteControlReader extends GungnirExecutionDataReader {
    private IRemoteCommandVisitor remoteCommandVisitor;

    public GungnirRemoteControlReader(final InputStream input) throws IOException {
        super(input);
    }

    @Override
    protected boolean readBlock(final byte blockid) throws IOException {
        switch (blockid) {
            case RemoteControlWriter.BLOCK_CMDDUMP:
                readDumpCommand();
                return true;
            case RemoteControlWriter.BLOCK_CMDOK:
                return false;
            default:
                return super.readBlock(blockid);
        }
    }

    public void setRemoteCommandVisitor(final IRemoteCommandVisitor visitor) {
        this.remoteCommandVisitor = visitor;
    }

    private void readDumpCommand() throws IOException {
        if (remoteCommandVisitor == null) {
            throw new IOException("No remote command visitor.");
        }
        final boolean dump = in.readBoolean();
        final boolean reset = in.readBoolean();
        remoteCommandVisitor.visitDumpCommand(dump, reset);
    }

}
