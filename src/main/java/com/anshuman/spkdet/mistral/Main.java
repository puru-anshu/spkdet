package com.anshuman.spkdet.mistral;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Anshuman
 * Date: 01/08/14
 * Time: 4:28 PM
 */
public class Main {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage train testdirectory [traindirectory] [wordirectory] ");
            return;
        }

        String testDir = args[0];
        Verifier verifier = new Verifier();
//
//        if (args.length > 2) {
//            String worldDir = args[2];
//            File dir = new File(worldDir);
//            List<User> users = new ArrayList<User>();
//            for (File tFile : dir.listFiles()) {
//                if (tFile.getName().endsWith(".wav")) {
//                    User user = new User(AudioUtils.getBaseName(tFile), tFile.getAbsolutePath());
//                    verifier.normalizeAudio(user);
//                    users.add(user);
//                }
//            }
//
//            verifier.prepareWorld(users);
//
//        }
//
//
        List<User> trainedUser = new ArrayList<User>();
        if (args.length > 1) {
            String worldDir = args[1];
            File dir = new File(worldDir);

            for (File tFile : dir.listFiles()) {
                if (tFile.getName().endsWith(".wav")) {
                    User user = new User(AudioUtils.getBaseName(tFile), tFile.getAbsolutePath());
                    verifier.trainUser(user);
                    trainedUser.add(user);

                }
            }

        }
//        List<User > trainedUser = new ArrayList<User>();
        String worldDir = Option.gmmPath;
//        File dir = new File(worldDir);
//
//        for (File tFile : dir.listFiles()) {
//            if (tFile.getName().endsWith(".gmm") && !tFile.getName().startsWith("world")) {
//                User user = new User(AudioUtils.getBaseName(tFile), "");
//                trainedUser.add(user);
//            }
//        }


        int scount = 0;
        File dir = new File(testDir);
        int t = 0;
        for (File tFile : dir.listFiles()) {
            if (tFile.getName().endsWith(".wav")) {
                User result = verifier.identifyUser(tFile, trainedUser);
                t++;
                if (tFile.getName().startsWith(result.getCallerId())) {
                    scount++;
                }
                System.out.println("\n" + tFile.getName() + " matched with " + result);

            }
        }

        System.out.println("Succ = " + scount + " out of " + t);


    }
}
