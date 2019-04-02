package com.anshuman.spkdet.dsp.sound;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;


public class AudioIo {


    public static class AudioSignal {


        public int samplingRate;


        public double[][] data;


        public int getLength() {
            return data[0].length;
        }


        public int getChannels() {
            return data.length;
        }

        public double getDuration() {
            return (getLength() * 1.0) / (double) samplingRate;
        }

    }


    private AudioIo() {
    }


    public static void saveWavFile(String fileName, AudioSignal signal, int pos, int len) throws Exception {
        AudioFormat format = new AudioFormat(signal.samplingRate, 16, signal.getChannels(), true, false);
        AudioBytesPackerStream audioBytesPackerStream = new AudioBytesPackerStream(format, signal.data, pos, len);
        AudioInputStream audioInputStream = new AudioInputStream(audioBytesPackerStream, format, len);
        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, new File(fileName));
    }


    public static void saveWavFile(String fileName, AudioSignal signal) throws Exception {
        saveWavFile(fileName, signal, 0, signal.getLength());
    }


    public static void saveWavFile(String fileName, double[] buf, int samplingRate) throws Exception {
        AudioSignal signal = new AudioSignal();
        signal.samplingRate = samplingRate;
        signal.data = new double[][]{buf};
        saveWavFile(fileName, signal);
    }

    private static class AudioBytesPackerStream extends InputStream {
        AudioFormat format;
        double[][] inBufs;
        int inOffs;
        int inLen;
        int pos;

        public AudioBytesPackerStream(AudioFormat format, double[][] inBufs, int inOffs, int inLen) {
            this.format = format;
            this.inBufs = inBufs;
            this.inOffs = inOffs;
            this.inLen = inLen;
        }

        @Override
        public int read() throws IOException {
            throw new AssertionError("Not implemented.");
        }

        @Override
        public int read(byte[] outBuf, int outOffs, int outLen) throws IOException {
            int remFrames = inLen - pos;
            if (remFrames <= 0) {
                return -1;
            }
            int reqFrames = outLen / format.getFrameSize();
            int trFrames = Math.min(remFrames, reqFrames);
            packAudioStreamBytes(format, inBufs, inOffs + pos, outBuf, outOffs, trFrames);
            pos += trFrames;
            return trFrames * format.getFrameSize();
        }
    }


    public static AudioSignal loadWavFile(String fileName) throws IOException, UnsupportedAudioFileException {
        AudioSignal signal = new AudioSignal();
        AudioInputStream stream = AudioSystem.getAudioInputStream(new File(fileName));
        AudioFormat format = stream.getFormat();
        signal.samplingRate = Math.round(format.getSampleRate());
        int frameSize = format.getFrameSize();
        int channels = format.getChannels();
        long totalFramesLong = stream.getFrameLength();
        if (totalFramesLong > Integer.MAX_VALUE) {
            throw new IOException("Sound file too long.");
        }
        int totalFrames = (int) totalFramesLong;
        signal.data = new double[channels][];
        for (int channel = 0; channel < channels; channel++) {
            signal.data[channel] = new double[totalFrames];
        }
        final int blockFrames = 0x4000;
        byte[] blockBuf = new byte[frameSize * blockFrames];
        int pos = 0;
        while (pos < totalFrames) {
            int reqFrames = Math.min(totalFrames - pos, blockFrames);
            int trBytes = stream.read(blockBuf, 0, reqFrames * frameSize);
            if (trBytes % frameSize != 0) {
                throw new AssertionError();
            }
            int trFrames = trBytes / frameSize;
            unpackAudioStreamBytes(format, blockBuf, 0, signal.data, pos, trFrames);
            pos += trFrames;
        }
        return signal;
    }


    public static void play(AudioSignal signal) throws Exception {
        int channels = signal.getChannels();
        AudioFormat format = new AudioFormat(signal.samplingRate, 16, channels, true, false);
        int frameSize = format.getFrameSize();
        SourceDataLine line = AudioSystem.getSourceDataLine(format);
        line.open(format, signal.samplingRate * frameSize);
        line.start();
        final int blockFrames = 0x4000;
        byte[] blockBuf = new byte[frameSize * blockFrames];
        int pos = 0;
        while (pos < signal.getLength()) {
            int frames = Math.min(signal.getLength() - pos, blockFrames);
            packAudioStreamBytes(format, signal.data, pos, blockBuf, 0, frames);
            int bytes = frames * frameSize;
            int trBytes = line.write(blockBuf, 0, bytes);
            if (trBytes != bytes) {
                throw new AssertionError();
            }
            pos += frames;
        }
        line.drain();
        line.stop();
        line.close();
    }


    public static void play(double[] buf, int samplingRate) throws Exception {
        AudioSignal signal = new AudioSignal();
        signal.data = new double[][]{buf};
        signal.samplingRate = samplingRate;
        play(signal);
    }


    public static void unpackAudioStreamBytes(AudioFormat format, byte[] inBuf, int inPos, double[][] outBufs, int outPos, int frames) {
        int channels = format.getChannels();
        boolean bigEndian = format.isBigEndian();
        int sampleBits = format.getSampleSizeInBits();
        int frameSize = format.getFrameSize();
        if (outBufs.length != channels) {
            throw new IllegalArgumentException("Number of channels not equal to number of buffers.");
        }
        if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
            throw new UnsupportedOperationException("Audio stream format not supported (not signed PCM).");
        }
        if (sampleBits != 16 && sampleBits != 24) {
            throw new UnsupportedOperationException("Audio stream format not supported (" + sampleBits + " bits per sample).");
        }
        int sampleSize = (sampleBits + 7) / 8;
        if (sampleSize * channels != frameSize) {
            throw new AssertionError();
        }
        double maxValue = (double) ((1 << (sampleBits - 1)) - 1);
        for (int channel = 0; channel < channels; channel++) {
            double[] outBuf = outBufs[channel];
            int p0 = inPos + channel * sampleSize;
            for (int i = 0; i < frames; i++) {
                int v = unpackSignedInt(inBuf, p0 + i * frameSize, sampleBits, bigEndian);
                outBuf[outPos + i] = v / maxValue;
            }
        }
    }


    public static void packAudioStreamBytes(AudioFormat format, double[][] inBufs, int inPos, byte[] outBuf, int outPos, int frames) {
        int channels = format.getChannels();
        boolean bigEndian = format.isBigEndian();
        int sampleBits = format.getSampleSizeInBits();
        int frameSize = format.getFrameSize();
        if (inBufs.length != channels) {
            throw new IllegalArgumentException("Number of channels not equal to number of buffers.");
        }
        if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
            throw new UnsupportedOperationException("Audio stream format not supported (not signed PCM).");
        }
        if (sampleBits != 16 && sampleBits != 24) {
            throw new UnsupportedOperationException("Audio stream format not supported (" + sampleBits + " bits per sample).");
        }
        int sampleSize = (sampleBits + 7) / 8;
        if (sampleSize * channels != frameSize) {
            throw new AssertionError();
        }
        int maxValue = (1 << (sampleBits - 1)) - 1;
        for (int channel = 0; channel < channels; channel++) {
            double[] inBuf = inBufs[channel];
            int p0 = outPos + channel * sampleSize;
            for (int i = 0; i < frames; i++) {
                double clipped = Math.max(-1, Math.min(1, inBuf[inPos + i]));
                int v = (int) (Math.round(clipped * maxValue));
                packSignedInt(v, outBuf, p0 + i * frameSize, sampleBits, bigEndian);
            }
        }
    }

    private static int unpackSignedInt(byte[] buf, int pos, int bits, boolean bigEndian) {
        switch (bits) {
            case 16:
                if (bigEndian) {
                    return (buf[pos] << 8) | (buf[pos + 1] & 0xFF);
                } else {
                    return (buf[pos + 1] << 8) | (buf[pos] & 0xFF);
                }
            case 24:
                if (bigEndian) {
                    return (buf[pos] << 16) | ((buf[pos + 1] & 0xFF) << 8) | (buf[pos + 2] & 0xFF);
                } else {
                    return (buf[pos + 2] << 16) | ((buf[pos + 1] & 0xFF) << 8) | (buf[pos] & 0xFF);
                }
            default:
                throw new AssertionError();
        }
    }

    private static void packSignedInt(int i, byte[] buf, int pos, int bits, boolean bigEndian) {
        switch (bits) {
            case 16:
                if (bigEndian) {
                    buf[pos] = (byte) ((i >>> 8) & 0xFF);
                    buf[pos + 1] = (byte) (i & 0xFF);
                } else {
                    buf[pos] = (byte) (i & 0xFF);
                    buf[pos + 1] = (byte) ((i >>> 8) & 0xFF);
                }
                break;
            case 24:
                if (bigEndian) {
                    buf[pos] = (byte) ((i >>> 16) & 0xFF);
                    buf[pos + 1] = (byte) ((i >>> 8) & 0xFF);
                    buf[pos + 2] = (byte) (i & 0xFF);
                } else {
                    buf[pos] = (byte) (i & 0xFF);
                    buf[pos + 1] = (byte) ((i >>> 8) & 0xFF);
                    buf[pos + 2] = (byte) ((i >>> 16) & 0xFF);
                }
                break;
            default:
                throw new AssertionError();
        }
    }

}
