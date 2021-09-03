package vip.testops.gungnir.internal.analysis;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Optional;

@Slf4j
public class SourceParser {
    public static String getPackageFromSource(File source) throws FileNotFoundException {
        JavaParser parser = new JavaParser();
        ParseResult<CompilationUnit> result = parser.parse(source);
        if(!result.isSuccessful()){
            log.error("{} parse failed", source.getName());
            return null;
        }
        Optional<CompilationUnit> optional = result.getResult();
        if(!optional.isPresent()) {
            log.error("no result");
            return null;
        }
        Optional<PackageDeclaration> po = optional.get().getPackageDeclaration();
        if(!po.isPresent()) {
            log.error("no package declaration found");
            return null;
        }
        return po.get().getNameAsString();
    }


}
