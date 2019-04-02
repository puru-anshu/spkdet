package com.anshuman.spkdet.mistral;

import java.io.File;

/**
 * User: Anshuman
 * Date: 12/07/14
 * Time: 3:40 PM
 */
public class AudioUtils {


    public static File getSpeechWav(File wavFile) {
        String outFile = "/tmp/" + wavFile.getName();
        String command = "/usr/local/bin/sox " + wavFile.getPath() + " " + outFile + " silence 1 0.1 0.1% -1 0.1 0.1%";
//        System.out.println("command = " + command);
        try {
            Process process = Runtime.getRuntime().exec(command);
            int exitValue = process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return new File(outFile);
    }


    public static String getBaseName(File f) {
        return f.getName().split("\\.(?=[^\\.]+$)")[0];
    }

}
