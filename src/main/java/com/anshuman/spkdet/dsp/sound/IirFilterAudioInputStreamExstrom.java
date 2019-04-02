package com.anshuman.spkdet.dsp.sound;


import com.anshuman.spkdet.dsp.filter.FilterPassType;
import com.anshuman.spkdet.dsp.filter.IirFilterCoefficients;
import com.anshuman.spkdet.dsp.filter.IirFilterDesignExstrom;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

public class IirFilterAudioInputStreamExstrom {


    private IirFilterAudioInputStreamExstrom() {
    }

    /**
     *
     * @param in
     * @param filterPassType
     * @param filterOrder
     * @param fcf1
     * @param fcf2
     * @return
     */
    public static AudioInputStream getAudioInputStream(AudioInputStream in,
                                                       FilterPassType filterPassType,
                                                       int filterOrder,
                                                       double fcf1,
                                                       double fcf2) {
        AudioFormat format = in.getFormat();
        double sampleRate = format.getSampleRate();
        double fcf1Rel = fcf1 / sampleRate;
        double fcf2Rel = fcf2 / sampleRate;
        IirFilterCoefficients coeffs = IirFilterDesignExstrom.design(filterPassType, filterOrder, fcf1Rel, fcf2Rel);
        return IirFilterAudioInputStream.getAudioInputStream(in, coeffs);
    }

}
