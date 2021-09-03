package vip.testops.gungnir.agent.internal;

import org.jacoco.core.JaCoCo;
import org.jacoco.core.runtime.AbstractRuntime;
import vip.testops.gungnir.agent.core.data.GungnirExecutionDataWriter;
import vip.testops.gungnir.agent.internal.output.IAgentOutput;
import vip.testops.gungnir.agent.internal.output.NoneOutput;
import vip.testops.gungnir.agent.internal.output.TcpClientOutput;
import vip.testops.gungnir.agent.runtime.GungnirAgentOptions;
import vip.testops.gungnir.agent.runtime.GungnirRuntimeData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GungnirAgent implements IAgent{

    private static GungnirAgent singleton;

    private final GungnirAgentOptions options;

    private final IExceptionLogger logger;

    private final GungnirRuntimeData data;

    private IAgentOutput output;

    private ScheduledExecutorService scheduledExecutorService;

//    private Callable<Void> jmxRegistration;

    public static synchronized GungnirAgent getInstance(final GungnirAgentOptions options)
            throws Exception {
        if (singleton == null) {
            final GungnirAgent agent = new GungnirAgent(options, IExceptionLogger.SYSTEM_ERR);
            agent.startup();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> agent.shutdown()));
            singleton = agent;
        }
        return singleton;
    }

    public static synchronized GungnirAgent getInstance()
            throws IllegalStateException {
        if (singleton == null) {
            throw new IllegalStateException("Gungnir agent not started.");
        }
        return singleton;
    }

    GungnirAgent(final GungnirAgentOptions options, final IExceptionLogger logger) {
        this.options = options;
        this.logger = logger;
        this.data = new GungnirRuntimeData();
    }

    public GungnirRuntimeData getData() {
        return data;
    }

    public void startup() throws Exception {
        try {
            String sessionId = options.getSessionId();
            String projectName = options.getProjectName();
            if (sessionId == null) {
                sessionId = createSessionId();
            }
            data.setSessionId(sessionId);
            data.setProjectName(projectName);
            output = createAgentOutput();
            output.startup(options, data);
            scheduledExecutorService = Executors.newScheduledThreadPool(1);
            scheduledExecutorService.scheduleAtFixedRate(() -> {
                try {
                    output.writeExecutionData(false);
                } catch (IOException e) {
                    logger.logExeption(e);
                }
            }, 5, 3, TimeUnit.MINUTES);
        } catch (final Exception e) {
            logger.logExeption(e);
            throw e;
        }
    }

    public void shutdown() {
        try {
            if (options.getDumpOnExit()) {
                output.writeExecutionData(false);
            }
            if(scheduledExecutorService != null){
                scheduledExecutorService.shutdown();
            }
            output.shutdown();
//            if (jmxRegistration != null) {
//                jmxRegistration.call();
//            }
        } catch (final Exception e) {
            logger.logExeption(e);
        }
    }

    IAgentOutput createAgentOutput() {
        final GungnirAgentOptions.OutputMode controllerType = options.getOutput();
        switch (controllerType) {
            case file:
//                return new FileOutput();
            case tcpserver:
//                return new TcpServerOutput(logger);
            case tcpclient:
                return new TcpClientOutput(logger);
            case none:
                return new NoneOutput();
            default:
                throw new AssertionError(controllerType);
        }
    }

    private String createSessionId() {
        String host;
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (final Exception e) {
            // Also catch platform specific exceptions (like on Android) to
            // avoid bailing out here
            host = "unknownhost";
        }
        return host + "-" + AbstractRuntime.createRandomId();
    }

    @Override
    public String getVersion() {
        return JaCoCo.VERSION;
    }

    @Override
    public String getSessionId() {
        return data.getSessionId();
    }

    @Override
    public void setSessionId(String id) {
        data.setSessionId(id);
    }

    @Override
    public String getProjectName() {
        return data.getProjectName();
    }

    @Override
    public void setProjectName(String projectName) {
        data.setProjectName(projectName);
    }

    @Override
    public void reset() {
        data.reset();
    }

    @Override
    public byte[] getExecutionData(boolean reset) {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            final GungnirExecutionDataWriter writer = new GungnirExecutionDataWriter(buffer);
            data.collect(writer, writer, writer, reset);
        } catch (final IOException e) {
            // Must not happen with ByteArrayOutputStream
            throw new AssertionError(e);
        }
        return buffer.toByteArray();
    }

    @Override
    public void dump(boolean reset) throws IOException {
        output.writeExecutionData(reset);
    }
}
