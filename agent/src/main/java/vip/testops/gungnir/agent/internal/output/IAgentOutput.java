package vip.testops.gungnir.agent.internal.output;

import vip.testops.gungnir.agent.runtime.GungnirAgentOptions;
import vip.testops.gungnir.agent.runtime.GungnirRuntimeData;

import java.io.IOException;

public interface IAgentOutput {
    /**
     * Configure the agent controller with the supplied options and connect it
     * to the coverage runtime
     *
     * @param options
     *            Options used to configure the agent controller
     * @param data
     *            Execution data for this agent
     * @throws Exception
     *             in case startup fails
     */
    void startup(GungnirAgentOptions options, GungnirRuntimeData data) throws Exception;

    /**
     * Shutdown the agent controller and clean up any resources it has created.
     *
     * @throws Exception
     *             in case shutdown fails
     */
    void shutdown() throws Exception;

    /**
     * Write all execution data in the runtime to a location determined by the
     * agent controller. This method should only be called by the Agent
     *
     * @param reset
     *            if <code>true</code> execution data is cleared afterwards
     * @throws IOException
     *             in case writing fails
     */
    void writeExecutionData(boolean reset) throws IOException;
}
