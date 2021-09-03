package vip.testops.gungnir.internal.os;

import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class FileUtil {

    public static boolean deleteDirectory(File dir) {
        if(!dir.isDirectory()) {
            log.info("{} not exist", dir.getName());
            return true;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return false;
        }
        for(File file : files) {
            if(file.isDirectory()) {
                if(!deleteDirectory(file)) {
                    return false;
                }
            }else {
                if (!file.delete()) {
                    log.error("file: {} delete failed.", file.getAbsoluteFile());
                    return false;
                }
            }
        }
        if(!dir.delete()){
            log.error("directory {} delete failed", dir.getAbsolutePath());
            return false;
        } else {
            return true;
        }

    }

    public static void main(String[] args) {
        System.out.println(deleteDirectory(new File("repo")));
    }
}
