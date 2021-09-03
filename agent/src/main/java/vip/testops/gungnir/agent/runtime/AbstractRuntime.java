package vip.testops.gungnir.agent.runtime;

import java.util.Random;

public abstract class AbstractRuntime implements IRuntime{
    protected GungnirRuntimeData data;

    @Override
    public void startup(final GungnirRuntimeData data) throws Exception {
        this.data = data;
    }

    private static final Random RANDOM = new Random();

    public static String createRandomId() {
        return Integer.toHexString(RANDOM.nextInt());
    }
}
