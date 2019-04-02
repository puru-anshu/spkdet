package com.anshuman.spkdet.dsp.sound;


import com.anshuman.spkdet.dsp.filter.IirFilter;
import com.anshuman.spkdet.dsp.filter.IirFilterCoefficients;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.IOException;
import java.io.InputStream;


public class IirFilterAudioInputStream {


    private IirFilterAudioInputStream() {
    }


    public static AudioInputStream getAudioInputStream(AudioInputStream in, IirFilterCoefficients coeffs) {
        FilterStream filterStream = new FilterStream(in, coeffs);
        return new AudioInputStream(filterStream, in.getFormat(), in.getFrameLength());
    }



    private static class FilterStream extends InputStream {

        private static final int inBufFrames = 4096;

        private AudioInputStream in;
        private AudioFormat format;
        private int channels;
        private int frameSize;
        private IirFilter[] iirFilters;
        private byte[] inBuf;
        private double[][] doubleBufs;

        public FilterStream(AudioInputStream in, IirFilterCoefficients coeffs) {
            this.in = in;
            format = in.getFormat();
            channels = format.getChannels();
            frameSize = format.getFrameSize();
            inBuf = new byte[inBufFrames * frameSize];
            doubleBufs = new double[channels][];
            iirFilters = new IirFilter[channels];
            for (int channel = 0; channel < channels; channel++) {
                doubleBufs[channel] = new double[inBufFrames];
                iirFilters[channel] = new IirFilter(coeffs);
            }
        }

        @Override
        public int read(byte[] outBuf, int outOffs, int len1) throws IOException {
            int len2 = Math.min(len1, inBuf.length);
            int len3 = (len2 / frameSize) * frameSize;
            int len = in.read(inBuf, 0, len3);
            if (len <= 0) {
                return len;
            }
            if (len % frameSize != 0) {
                throw new AssertionError();
            }
            int frames = len / frameSize;
            AudioIo.unpackAudioStreamBytes(format, inBuf, 0, doubleBufs, 0, frames);
            for (int channel = 0; channel < channels; channel++) {
                IirFilter iirFilter = iirFilters[channel];
                double[] doubleBuf = doubleBufs[channel];
                for (int i = 0; i < frames; i++) {
                    doubleBuf[i] = (double) iirFilter.step(doubleBuf[i]);
                }
            }
            AudioIo.packAudioStreamBytes(format, doubleBufs, 0, outBuf, outOffs, frames);
            return len;
        }

        @Override
        public int read() throws IOException {
            throw new AssertionError();
        }

    }
}
