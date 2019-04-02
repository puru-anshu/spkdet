package com.anshuman.spkdet.dsp.sound;


import com.anshuman.spkdet.dsp.filter.FilterCharacteristicsType;
import com.anshuman.spkdet.dsp.filter.FilterPassType;
import com.anshuman.spkdet.dsp.filter.IirFilterCoefficients;
import com.anshuman.spkdet.dsp.filter.IirFilterDesignFisher;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

public class IirFilterAudioInputStreamFisher {


    private IirFilterAudioInputStreamFisher() {
    }


    public static AudioInputStream getAudioInputStream(AudioInputStream in, FilterPassType filterPassType,
                                                       FilterCharacteristicsType filterCharacteristicsType,
                                                       int filterOrder,
                                                       double ripple, double fcf1, double fcf2) {
        AudioFormat format = in.getFormat();
        double sampleRate = format.getSampleRate();
        double fcf1Rel = fcf1 / sampleRate;
        double fcf2Rel = fcf2 / sampleRate;
        IirFilterCoefficients coeffs = IirFilterDesignFisher.design(filterPassType, filterCharacteristicsType, filterOrder, ripple, fcf1Rel, fcf2Rel);
        return IirFilterAudioInputStream.getAudioInputStream(in, coeffs);
    }

}
