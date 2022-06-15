package xyz.xieguoking;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * @author xieguoking
 * @author (2021 / 8 / 21 add by xieguoking
 * @version 1.0
 * @since 1.0
 */
public class EmptyFolder {
    public static void main(String[] args) {
        List<File> emptyFolders = new LinkedList<>();
        List<File> emptyFiles = new LinkedList<>();

        new EmptyFolder().scan(new File("C:\\jee_dev\\repository\\"), emptyFolders, emptyFiles);

        System.out.println(emptyFolders.size());
        emptyFolders.forEach(file -> {
            file.delete();
        });

        /*
        emptyFiles.forEach(file -> {
            file.delete();
        });
         */

    }

    public void scan(File rootFile, List<File> emptyFolders, List<File> emptyFiles) {
        if (rootFile.isDirectory()) {
            for (File file : rootFile.listFiles()) {
                if (file.isDirectory()) {
                    if (file.listFiles().length == 0) {
                        emptyFolders.add(file);
                    } else {
                        scan(file, emptyFolders, emptyFiles);
                    }
                } else if (file.isFile() && file.length() == 0) {
                    emptyFiles.add(file);
                }
            }
        }
    }

}
