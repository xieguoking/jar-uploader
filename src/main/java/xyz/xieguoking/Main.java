package xyz.xieguoking;

import java.io.File;

/**
 * @author xieguoking
 * @author (2021 / 4 / 7 add by xieguoking
 * @version 1.0
 * @since 1.0
 */
public class Main {
    public static void main(String[] args) {
        final Option option = parseArgs(args);

        if (option.repositoryUrl == null || option.repositoryId == null || option.root == null) {
            help();
            System.exit(-1);
        }
        if (option.help) {
            help();
        }

        // program arguments  --dir /home/xieguoking/libs/ --url http://10.1.63.183:8082/content/repositories/releases/ --id nexus

        new Uploader(new File(option.root), option.repositoryUrl, option.repositoryId).uploadAll();
    }

    private static void help() {
        System.out.printf("java Main\n");
        System.out.printf("\t-h --help\n");
        System.out.printf("\t--url repositoryUrl\n");
        System.out.printf("\t--id repositoryId\n");
        System.out.printf("\t--dir jar root path\n");
    }

    private static Option parseArgs(String[] args) {
        Option option = new Option();
        for (int i = 0; i < args.length; i++) {
            String v = args[i];
            if (v.equals("-h") || v.equals("--help")) {
                option.help = true;
            } else if (v.startsWith("--url")) {
                option.repositoryUrl = args[++i];
            } else if (v.startsWith("--id")) {
                option.repositoryId = args[++i];
            } else if (v.startsWith("--dir")) {
                option.root = args[++i];
            }
        }
        return option;
    }

    static class Option {
        boolean help = false;
        String repositoryUrl;
        String repositoryId;
        String root;
    }
}
