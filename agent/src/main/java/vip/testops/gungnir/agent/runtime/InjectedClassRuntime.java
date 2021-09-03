package vip.testops.gungnir.agent.runtime;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class InjectedClassRuntime extends AbstractRuntime{

    private static final String FIELD_NAME = "data";

    private static final String FIELD_TYPE = "Ljava/lang/Object;";

    private final Class<?> locator;

    private final String injectedClassName;

    public InjectedClassRuntime(final Class<?> locator,
                                final String simpleClassName) {
        this.locator = locator;
        this.injectedClassName = locator.getPackage().getName().replace('.',
                '/') + '/' + simpleClassName;
    }

    @Override
    public void startup(GungnirRuntimeData data) throws Exception {
        super.startup(data);
        Lookup.privateLookupIn(locator, Lookup.lookup()) //
                .defineClass(createClass(injectedClassName)) //
                .getField(FIELD_NAME) //
                .set(null, data);
    }

    @Override
    public int generateDataAccessor(long classid, String classname, int probecount, MethodVisitor mv) {
        mv.visitFieldInsn(Opcodes.GETSTATIC, injectedClassName, FIELD_NAME, FIELD_TYPE);
        GungnirRuntimeData.generateAccessCall(classid, classname, probecount, mv);
        return 6;
    }

    @Override
    public void shutdown() { }

    private static byte[] createClass(final String name) {
        final ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V9, Opcodes.ACC_SYNTHETIC | Opcodes.ACC_PUBLIC,
                name.replace('.', '/'), null, "java/lang/Object", null);
        cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, FIELD_NAME,
                FIELD_TYPE, null, null);
        cw.visitEnd();
        return cw.toByteArray();
    }

    private static class Lookup {
        private final Object instance;

        private Lookup(final Object instance) {
            this.instance = instance;
        }

        /**
         * @return a lookup object for the caller of this method
         */
        static Lookup lookup() throws Exception {
            return new Lookup(Class //
                    .forName("java.lang.invoke.MethodHandles") //
                    .getMethod("lookup") //
                    .invoke(null));
        }

        static Lookup privateLookupIn(final Class<?> targetClass, Lookup lookup) throws Exception {
            return new Lookup(Class
                    .forName("java.lang.invoke.MethodHandles")
                    .getMethod("privateLookupIn", Class.class,
                            Class.forName(
                                    "java.lang.invoke.MethodHandles$Lookup")) //
                    .invoke(null, targetClass, lookup.instance));
        }

        Class<?> defineClass(final byte[] bytes) throws Exception {
            return (Class<?>) Class //
                    .forName("java.lang.invoke.MethodHandles$Lookup")
                    .getMethod("defineClass", byte[].class)
                    .invoke(this.instance, new Object[] { bytes });
        }
    }
}
