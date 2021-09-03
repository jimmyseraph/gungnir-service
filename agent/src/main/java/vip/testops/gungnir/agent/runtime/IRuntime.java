package vip.testops.gungnir.agent.runtime;

import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;

public interface IRuntime extends IExecutionDataAccessorGenerator {

    /**
     * Starts the coverage runtime. This method MUST be called before any class
     * instrumented for this runtime is loaded.
     *
     * @param data the execution data for this runtime
     * @throws Exception any internal problem during startup
     */
    void startup(GungnirRuntimeData data) throws Exception;

    /**
     * Allows the coverage runtime to cleanup internals. This class should be
     * called when classes instrumented for this runtime are not used any more.
     */
    void shutdown();
}
