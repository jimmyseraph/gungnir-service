package vip.testops.gungnir.internal.analysis;

import org.jacoco.core.analysis.ICoverageVisitor;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.internal.analysis.ClassAnalyzer;
import org.jacoco.core.internal.analysis.ClassCoverageImpl;
import org.jacoco.core.internal.analysis.StringPool;
import org.jacoco.core.internal.data.CRC64;
import org.jacoco.core.internal.flow.ClassProbesAdapter;
import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

public class GungnirAnalyzer {

    private final ExecutionDataStore executionDataStore;
    private final ICoverageVisitor coverageVisitor;
    private final StringPool stringPool;

    public GungnirAnalyzer(final ExecutionDataStore executionDataStore, final ICoverageVisitor coverageVisitor) {
        this.executionDataStore = executionDataStore;
        this.coverageVisitor = coverageVisitor;
        this.stringPool = new StringPool();
    }

    private ClassVisitor createAnalyzingVisitor(final long classid, final String className) {
        final ExecutionData data = executionDataStore.get(classid);
        final boolean[] probes;
        final boolean noMatch;
        if (data == null) {
            probes = null;
            noMatch = executionDataStore.contains(className);
        } else {
            probes = data.getProbes();
            noMatch = false;
        }
        final ClassCoverageImpl coverage = new ClassCoverageImpl(className, classid, noMatch);
        final ClassAnalyzer analyzer = new ClassAnalyzer(coverage, probes,
                stringPool) {
            @Override
            public void visitEnd() {
                super.visitEnd();
                coverageVisitor.visitCoverage(coverage);
            }
        };
        return new ClassProbesAdapter(analyzer, false);

    }

    private void analyzeClass(final byte[] name) {
        final long classId = CRC64.classId(name);
        final ClassVisitor visitor = createAnalyzingVisitor(classId, new String(name));
//        reader.accept(visitor, 0);
    }
}
