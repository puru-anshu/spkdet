
package com.anshuman.spkdet.voiceid.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;


public class Utils {
    private static Logger logger = LoggerFactory.getLogger(Utils.class);


    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1)
            ext = s.substring(i + 1).toLowerCase();

        if (ext == null)
            return "";
        return ext;
    }


    public static String getBasename(File f) throws IOException {
        String ext = null;
        String s = f.getCanonicalPath();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1)
            ext = s.substring(i + 1).toLowerCase();

        if (ext == null)
            return s;

        return s.substring(0, s.length() - ext.length() - 1);
    }


    public static boolean isWave(File file) {
        AudioFileFormat a = null;
        try {
            a = AudioSystem.getAudioFileFormat(file);
            // logger.info(a.toString());
        } catch (UnsupportedAudioFileException e) {
            logger.warn(e.getMessage());
            return false;
        } catch (IOException e) {
            logger.warn(e.getMessage());
            return false;
        }

        if (a.getType().equals(AudioFileFormat.Type.WAVE)) {
            logger.trace("YES IT IS A WAVE");
            return true;
        }
        return false;
    }


    public static boolean isGoodWave(File file) throws IOException,
            UnsupportedAudioFileException {
//		AudioFileFormat a = null;
//		try {
//			a = AudioSystem.getAudioFileFormat(file);
//			logger.fine(a.toString());
//		} catch (UnsupportedAudioFileException e) {
//			logger.severe(e.getMessage());
//			throw e;
//		} catch (IOException e) {
//			logger.severe(e.getMessage());
//			return false;
//		}
//		if (!a.getType().equals(AudioFileFormat.Type.WAVE)) {
//			return false;
//		}
//		AudioFormat af = a.getFormat();
//		logger.fine("Frame size = "+af.getFrameSize());
//		logger.fine("Frame rate = "+af.getFrameRate());
//		logger.fine(af.toString());
//		if (af.getChannels() != 1)
//			return false;
//		if (af.getSampleRate() != 8000.0)
//			return false;
//		if (af.isBigEndian())
//			return false;
//		if (af.getFrameSize() != 2)
//			return false;
        return true;
    }


    public static void copyAudio(String sourceFileName,
                                 String destinationFileName, int startSecond, int secondsToCopy) {
        AudioInputStream inputStream = null;
        AudioInputStream shortenedStream = null;
        try {
            File file = new File(sourceFileName);
            AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(file);
            AudioFormat format = fileFormat.getFormat();
            inputStream = AudioSystem.getAudioInputStream(file);
            int bytesPerSecond = format.getFrameSize()
                    * (int) format.getFrameRate();
            inputStream.skip(startSecond * bytesPerSecond);
            long framesOfAudioToCopy = secondsToCopy
                    * (int) format.getFrameRate();
            shortenedStream = new AudioInputStream(inputStream, format,
                    framesOfAudioToCopy);
            File destinationFile = new File(destinationFileName);
            AudioSystem.write(shortenedStream, fileFormat.getType(),
                    destinationFile);
        } catch (Exception e) {
            logger.warn(e.getMessage());
        } finally {
            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (Exception e) {
                    logger.warn(e.getMessage());
                }
            if (shortenedStream != null)
                try {
                    shortenedStream.close();
                } catch (Exception e) {
                    logger.warn(e.getMessage());
                }
        }
    }


    public static void copyAudio(File sourceFile,
                                 String destinationFileName, float startSecond, float secondsToCopy) {
        AudioInputStream inputStream = null;
        AudioInputStream shortenedStream = null;
        try {
            AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(sourceFile);
            AudioFormat format = fileFormat.getFormat();
            inputStream = AudioSystem.getAudioInputStream(sourceFile);
            int bytesPerSecond = format.getFrameSize()
                    * (int) format.getFrameRate();
            inputStream.skip((int) (startSecond * 100) * bytesPerSecond / 100);
            long framesOfAudioToCopy = (int) (secondsToCopy * 100)
                    * (int) format.getFrameRate() / 100;
            shortenedStream = new AudioInputStream(inputStream, format,
                    framesOfAudioToCopy);
            File destinationFile = new File(destinationFileName);
            AudioSystem.write(shortenedStream, fileFormat.getType(),
                    destinationFile);
        } catch (Exception e) {
            logger.warn(e.getMessage());
        } finally {
            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (Exception e) {
                    logger.warn(e.getMessage());
                }
            if (shortenedStream != null)
                try {
                    shortenedStream.close();
                } catch (Exception e) {
                    logger.warn(e.getMessage());
                }
        }
    }
}
