package vip.testops.gungnir.converters;

import org.bson.Document;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.internal.analysis.*;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@ReadingConverter
public class BundleCoverageReadConverter implements Converter<Document, BundleCoverageImpl>{
    @Override
    public BundleCoverageImpl convert(Document document) {
        List<Document> packagesDocList = document.getList("packages", Document.class);
        List<IPackageCoverage> packages = new ArrayList<>();
        for(Document doc : packagesDocList) {
            packages.add(packageCoverageConvert(doc));
        }
        BundleCoverageImpl bundleCoverage = new BundleCoverageImpl(document.getString("name"), packages);
        return bundleCoverage;
    }

    private CounterImpl counterConvert(Document document) {
        return CounterImpl.getInstance(document.get("missed", Integer.class), document.get("covered", Integer.class));
    }

    private LineImpl lineConvert(Document document) {
        LineImpl line = LineImpl.EMPTY;
        Document instDoc = document.get("instructions", Document.class);
        Document branchDoc = document.get("branches",  Document.class);
        line = line.increment(
                counterConvert(instDoc),
                counterConvert(branchDoc)
        );
        return line;
    }

    private MethodCoverageImpl methodCoverageConvert(Document document) {
        MethodCoverageImpl methodCoverage = new MethodCoverageImpl(
                document.getString("name"),
                document.getString("desc"),
                document.getString("signature")
        );
        List<Document> lineDocList = (List<Document>) document.get("lines");
        int offset = document.getInteger("offset");
        for(int i = 0; i < lineDocList.size(); i++){
            if(lineDocList.get(i) == null) {
                continue;
            }
            ILine line = lineConvert(lineDocList.get(i));
            methodCoverage.increment(
                    line.getInstructionCounter(),
                    line.getBranchCounter(),
                    offset + i);
        }
        methodCoverage.incrementMethodCounter();
        return methodCoverage;
    }

    private ClassCoverageImpl classCoverageConvert(Document document) {
        ClassCoverageImpl classCoverage = new ClassCoverageImpl(
                document.getString("name"),
                document.getLong("_id"),
                document.getBoolean("noMatch")
        );
        List<Document> methodDocList = document.getList("methods", Document.class);
        for(Document methodDoc : methodDocList) {
            classCoverage.addMethod(methodCoverageConvert(methodDoc));
        }
        return classCoverage;
    }

    private SourceFileCoverageImpl sourceFileCoverageConvert(Document document) {
        SourceFileCoverageImpl sourceFileCoverage = new SourceFileCoverageImpl(
                document.getString("name"),
                document.getString("packagename")
        );
        List<Document> lineDocList = document.getList("lines", Document.class);
        int offset = document.getInteger("offset");
        if (lineDocList != null) {
            for(int i = 0; i < lineDocList.size(); i++) {
                if(lineDocList.get(i) == null) {
                    continue;
                }
                ILine line = lineConvert(lineDocList.get(i));
                sourceFileCoverage.increment(
                        line.getInstructionCounter(),
                        line.getBranchCounter(),
                        offset + i
                );
            }
        }

        try {
            Class<?> superClass = SourceFileCoverageImpl.class.getSuperclass().getSuperclass();

            Field f_complexityCounter = superClass.getDeclaredField("complexityCounter");
            f_complexityCounter.setAccessible(true);
            Document complexityCounterDoc = document.get("complexityCounter", Document.class);
            f_complexityCounter.set(sourceFileCoverage, counterConvert(complexityCounterDoc));

            Field f_methodCounter = superClass.getDeclaredField("methodCounter");
            f_methodCounter.setAccessible(true);
            Document methodCounterDoc = document.get("methodCounter", Document.class);
            f_methodCounter.set(sourceFileCoverage, counterConvert(methodCounterDoc));

            Field f_classCounter = superClass.getDeclaredField("classCounter");
            f_classCounter.setAccessible(true);
            Document classCounterDoc = document.get("classCounter", Document.class);
            f_classCounter.set(sourceFileCoverage, counterConvert(classCounterDoc));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return sourceFileCoverage;
    }

    private PackageCoverageImpl packageCoverageConvert(Document document) {
        List<Document> classCoverageDocList = document.getList("classes", Document.class);
        List<IClassCoverage> classes = new ArrayList<>();
        for(Document doc : classCoverageDocList) {
            classes.add(classCoverageConvert(doc));
        }
        List<Document> sourceFileCoverageDocList = document.getList("sourceFiles", Document.class);
        List<ISourceFileCoverage> sourceFiles = new ArrayList<>();
        for(Document doc : sourceFileCoverageDocList) {
            sourceFiles.add(sourceFileCoverageConvert(doc));
        }
        PackageCoverageImpl packageCoverage = new PackageCoverageImpl(
                document.getString("name"),
                classes,
                sourceFiles
        );
        return packageCoverage;
    }
}
