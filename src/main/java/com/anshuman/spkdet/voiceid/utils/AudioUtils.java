package com.anshuman.spkdet.voiceid.utils;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Anshuman
 * Date: 03/09/14
 * Time: 12:07 PM
 */
public class AudioUtils {

    private static Logger logger = LoggerFactory.getLogger(AudioUtils.class);

    private String wavPath;
    private String basename;

    public AudioUtils(String wavPath) {
        this.wavPath = wavPath;
        basename = new File(wavPath).getName().replace(".wav", "");

    }

    public File removeSilence() throws IOException {
        File tmpFile = File.createTempFile(basename + "_full", ".wav");
        String command = "/usr/local/bin/sox " + wavPath + " " + tmpFile.getPath() + " silence 1 0.1 0.1% -1 0.1 0.1%";
        CommandLine cmdLine = CommandLine.parse(command);

        DefaultExecutor executor = new DefaultExecutor();
        int exitValue = executor.execute(cmdLine);
        return tmpFile;


    }

    private double getDuration(File file) throws IOException, UnsupportedAudioFileException {
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
        AudioFormat format = audioInputStream.getFormat();
        long frames = audioInputStream.getFrameLength();
        double durationInSeconds = (frames + 0.0) / format.getFrameRate();
        return durationInSeconds;

    }


    private File trimFile(File baseFile, int startSec, int endSec) throws IOException {
        int duration = endSec - startSec;
        File tmpFile = File.createTempFile(basename + "_trim", ".wav");
        String command = "/usr/local/bin/sox " + baseFile.getPath() + " " + tmpFile.getPath() + " trim " + startSec + " " + duration;
        CommandLine cmdLine = CommandLine.parse(command);

        DefaultExecutor executor = new DefaultExecutor();
        int exitValue = executor.execute(cmdLine);
        return tmpFile;


    }

    public List<File> getSegmentFiles() throws Exception {
        List<File> files = new ArrayList<File>();
        File mFile = removeSilence();
        double duration = getDuration(mFile);
        logger.warn("Total duration of file " + wavPath + " is " + duration);
        files.add(mFile);
//        if (duration > 20) {
//            int end = (int) duration;
//            int start = end - 20;
//            files.add(trimFile(mFile, start, end));
//            files.add(trimFile(mFile, 0, 20));
//        }

        return files;
    }


}
