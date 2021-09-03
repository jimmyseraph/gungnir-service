package vip.testops.gungnir.analysis;

import java.io.*;

public class JavaSourceLocator {
    private File baseDir;
    private File packageDir;
    private String packageName;

    public JavaSourceLocator(File baseDir) {
        this.baseDir = baseDir;
    }

    public Reader getFileSource(String packageName, String filename) throws FileNotFoundException{
        if(!packageName.equals(this.packageName)) {
            setPackageDir(packageName);
        }
        return new FileReader(new File(this.packageDir, filename));
    }

    public void setPackageDir(String packageName){
        this.packageName = packageName;
        String packagePath = packageName.replace('.', File.separatorChar);
        this.packageDir = searchPackage(this.baseDir, packagePath);
    }

    private File searchPackage(File searchDir, String targetDir) {
        if(new File(searchDir, targetDir).exists()){
            return new File(searchDir, targetDir);
        } else {
            File[] dirs = searchDir.listFiles((dir, name) -> new File(dir, name).isDirectory());
            File packageDir = null;
            if (dirs == null) {
                return null;
            }
            for(File dir : dirs) {
                if(dir.getName().startsWith(".") || dir.getName().startsWith("target") || dir.getName().startsWith("build")) {
                    continue;
                }
                packageDir = searchPackage(dir, targetDir);
                if (packageDir != null){
                    return packageDir;
                }
            }
            return null;
        }
    }
}
