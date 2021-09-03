package vip.testops.gungnir.agent.runtime;
import static java.lang.String.format;
import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.security.ProtectionDomain;

public class ModifiedSystemClassRuntime extends AbstractRuntime{

    private static final String ACCESS_FIELD_TYPE = "Ljava/lang/Object;";

    private final Class<?> systemClass;

    private final String systemClassName;

    private final String accessFieldName;

    public ModifiedSystemClassRuntime(final Class<?> systemClass, final String accessFieldName) {
        super();
        this.systemClass = systemClass;
        this.systemClassName = systemClass.getName().replace('.', '/');
        this.accessFieldName = accessFieldName;
    }

    @Override
    public void startup(final GungnirRuntimeData data) throws Exception {
        super.startup(data);
        final Field field = systemClass.getField(accessFieldName);
        field.set(null, data);
    }

    @Override
    public int generateDataAccessor(final long classid, final String classname, final int probecount, final MethodVisitor mv) {
        mv.visitFieldInsn(Opcodes.GETSTATIC, systemClassName, accessFieldName, ACCESS_FIELD_TYPE);
        GungnirRuntimeData.generateAccessCall(classid, classname, probecount, mv);
        return 6;
    }

    @Override
    public void shutdown() { }

    public static IRuntime createFor(final Instrumentation inst, final String className) throws ClassNotFoundException {
        return createFor(inst, className, "jacocoAccess");
    }

    public static IRuntime createFor(final Instrumentation inst, final String className, final String accessFieldName)throws ClassNotFoundException {
        final ClassFileTransformer transformer = new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String name, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] source) throws IllegalClassFormatException {
                if(name.equals(className)) {
                    return instrument(source, accessFieldName);
                }
                return null;
            }
        };
        inst.addTransformer(transformer);
        final Class<?> clazz = Class.forName(className.replace('/', '.'));
        inst.removeTransformer(transformer);
        try {
            clazz.getField(accessFieldName);
        } catch (final NoSuchFieldException e) {
            throw new RuntimeException(format("Class %s could not be instrumented.", className), e);
        }
        return new ModifiedSystemClassRuntime(clazz, accessFieldName);
    }

    public static byte[] instrument(final byte[] source, final String accessFieldName) {
        final ClassReader reader = InstrSupport.classReaderFor(source);
        final ClassWriter writer = new ClassWriter(reader, 0);
        reader.accept(new ClassVisitor(InstrSupport.ASM_API_VERSION, writer) {

            @Override
            public void visitEnd() {
                createDataField(cv, accessFieldName);
                super.visitEnd();
            }

        }, ClassReader.EXPAND_FRAMES);
        return writer.toByteArray();
    }

    private static void createDataField(final ClassVisitor visitor, final String dataField) {
        visitor.visitField(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC
                        | Opcodes.ACC_TRANSIENT,
                dataField, ACCESS_FIELD_TYPE, null, null);
    }
}
