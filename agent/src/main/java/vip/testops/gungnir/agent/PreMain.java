package vip.testops.gungnir.agent;

import vip.testops.gungnir.agent.internal.GungnirAgent;
import vip.testops.gungnir.agent.internal.IExceptionLogger;
import vip.testops.gungnir.agent.runtime.GungnirAgentOptions;
import vip.testops.gungnir.agent.runtime.IRuntime;
import vip.testops.gungnir.agent.runtime.InjectedClassRuntime;
import vip.testops.gungnir.agent.runtime.ModifiedSystemClassRuntime;

import java.lang.instrument.Instrumentation;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class PreMain {
    private PreMain() {}

    public static void premain(final String options, final Instrumentation inst) throws Exception {
        final GungnirAgentOptions agentOptions = new GungnirAgentOptions(options);

        final GungnirAgent agent = GungnirAgent.getInstance(agentOptions);
        final IRuntime runtime = createRuntime(inst);
        runtime.startup(agent.getData());
        inst.addTransformer(new CoverageTransformer(runtime, agentOptions, IExceptionLogger.SYSTEM_ERR));
    }

    private static IRuntime createRuntime(final Instrumentation inst)
            throws Exception {

        if (redefineJavaBaseModule(inst)) {
            return new InjectedClassRuntime(Object.class, "$Gungnir");
        }

        return ModifiedSystemClassRuntime.createFor(inst,
                "java/lang/UnknownError");
    }

    private static boolean redefineJavaBaseModule(
            final Instrumentation instrumentation) throws Exception {
        try {
            Class.forName("java.lang.Module");
        } catch (final ClassNotFoundException e) {
            return false;
        }

        Instrumentation.class.getMethod("redefineModule", //
                Class.forName("java.lang.Module"), //
                Set.class, //
                Map.class, //
                Map.class, //
                Set.class, //
                Map.class //
        ).invoke(instrumentation, // instance
                getModule(Object.class), // module
                Collections.emptySet(), // extraReads
                Collections.emptyMap(), // extraExports
                Collections.singletonMap("java.lang",
                        Collections.singleton(
                                getModule(InjectedClassRuntime.class))), // extraOpens
                Collections.emptySet(), // extraUses
                Collections.emptyMap() // extraProvides
        );
        return true;
    }

    /**
     * @return {@code cls.getModule()}
     */
    private static Object getModule(final Class<?> cls) throws Exception {
        return Class.class //
                .getMethod("getModule") //
                .invoke(cls);
    }
}
