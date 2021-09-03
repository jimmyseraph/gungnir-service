package vip.testops.gungnir.agent.internal.output;

import vip.testops.gungnir.agent.internal.IExceptionLogger;
import vip.testops.gungnir.agent.runtime.GungnirAgentOptions;
import vip.testops.gungnir.agent.runtime.GungnirRuntimeData;

import java.io.IOException;
import java.net.Socket;

public class TcpClientOutput implements IAgentOutput{

    private final IExceptionLogger logger;

    private TcpConnection connection;

    private Thread worker;

    public TcpClientOutput(final IExceptionLogger logger) {
        this.logger = logger;
    }

    @Override
    public void startup(GungnirAgentOptions options, GungnirRuntimeData data) throws Exception {
        final Socket socket = createSocket(options);
        connection = new TcpConnection(socket, data);
        connection.init();
        worker = new Thread(() -> {
            try {
                connection.run();
            } catch (final IOException e) {
                logger.logExeption(e);
            }
        });
        worker.setName(getClass().getName());
        worker.setDaemon(true);
        worker.start();
    }

    @Override
    public void shutdown() throws Exception {
        connection.close();
        worker.join();
    }

    @Override
    public void writeExecutionData(boolean reset) throws IOException {
        connection.writeExecutionData(reset);
    }

    protected Socket createSocket(final GungnirAgentOptions options)
            throws IOException {
        return new Socket(options.getAddress(), options.getPort());
    }
}
