package vip.testops.gungnir.agent.internal.output;

import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.RuntimeData;
import vip.testops.gungnir.agent.runtime.GungnirAgentOptions;
import vip.testops.gungnir.agent.runtime.GungnirRuntimeData;

import java.io.IOException;

public class NoneOutput implements IAgentOutput{
    @Override
    public void startup(GungnirAgentOptions options, GungnirRuntimeData data) throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public void writeExecutionData(boolean reset) throws IOException {

    }
}
