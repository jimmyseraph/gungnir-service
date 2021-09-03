package vip.testops.gungnir.agent.internal.output;

import org.jacoco.core.runtime.IRemoteCommandVisitor;
import vip.testops.gungnir.agent.runtime.GungnirRemoteControlReader;
import vip.testops.gungnir.agent.runtime.GungnirRemoteControlWriter;
import vip.testops.gungnir.agent.runtime.GungnirRuntimeData;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class TcpConnection implements IRemoteCommandVisitor {

    private final GungnirRuntimeData data;

    private final Socket socket;

    private GungnirRemoteControlWriter writer;

    private GungnirRemoteControlReader reader;

    private boolean initialized;

    public TcpConnection(final Socket socket, final GungnirRuntimeData data) {
        this.socket = socket;
        this.data = data;
        this.initialized = false;
    }

    public void init() throws IOException {
        this.writer = new GungnirRemoteControlWriter(socket.getOutputStream());
        this.reader = new GungnirRemoteControlReader(socket.getInputStream());
        this.reader.setRemoteCommandVisitor(this);
        this.initialized = true;
    }

    public void run() throws IOException {
        try {
            while (reader.read()) {
            }
        } catch (final SocketException e) {
            // If the local socket is closed while polling for commands the
            // SocketException is expected.
            if (!socket.isClosed()) {
                throw e;
            }
        } finally {
            close();
        }
    }

    public void close() throws IOException {
        if (!socket.isClosed()) {
            socket.close();
        }
    }

    public void writeExecutionData(final boolean reset) throws IOException {
        if (initialized && !socket.isClosed()) {
            visitDumpCommand(true, reset);
        }
    }

    @Override
    public void visitDumpCommand(boolean dump, boolean reset) throws IOException {
        if (dump) {
            data.collect(writer, writer, writer, reset);
        } else {
            if (reset) {
                data.reset();
            }
        }
        writer.sendCmdOk();
    }
}
