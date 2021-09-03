package vip.testops.gungnir.internal.runtime;

import org.jacoco.core.runtime.IRemoteCommandVisitor;
import org.jacoco.core.runtime.RemoteControlWriter;
import vip.testops.gungnir.internal.data.GungnirExecutionDataWriter;

import java.io.IOException;
import java.io.OutputStream;

public class GungnirRemoteControlWriter extends GungnirExecutionDataWriter implements IRemoteCommandVisitor {

    public GungnirRemoteControlWriter(final OutputStream output) throws IOException {
        super(output);
    }

    public void sendCmdOk() throws IOException {
        out.writeByte(RemoteControlWriter.BLOCK_CMDOK);
    }

    @Override
    public void visitDumpCommand(boolean dump, boolean reset) throws IOException {
        out.writeByte(RemoteControlWriter.BLOCK_CMDDUMP);
        out.writeBoolean(dump);
        out.writeBoolean(reset);
    }
}
