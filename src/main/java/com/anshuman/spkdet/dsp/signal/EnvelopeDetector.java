package com.anshuman.spkdet.dsp.signal;

import com.anshuman.spkdet.dsp.filter.*;



public class EnvelopeDetector {

    private IirFilter iirFilter;
    private double gAttack;
    private double gRelease;
    private double level;


    public EnvelopeDetector(int samplingRate) {
        double attackTime = 0.0015;
        double releaseTime = 0.03;
        double lowerFilterCutoffFreq = 133;
//        double upperFilterCutoffFreq = Math.min(4700, samplingRate / 2 - 100);

        double upperFilterCutoffFreq = samplingRate > 8000?4700:3500;
        int filterOrder = 4;

        double filterRipple = -0.5;
        double fcf1Rel = lowerFilterCutoffFreq / samplingRate;
        double fcf2Rel = upperFilterCutoffFreq / samplingRate;
        IirFilterCoefficients coeffs = IirFilterDesignFisher.
                design(FilterPassType.bandpass, FilterCharacteristicsType.chebyshev,
                        filterOrder, filterRipple, fcf1Rel,
                        fcf2Rel);
        IirFilter iirFilter = new IirFilter(coeffs);
        init(samplingRate, attackTime, releaseTime, iirFilter);
    }


    public EnvelopeDetector(int samplingRate, double attackTime,
                            double releaseTime, IirFilter iirFilter) {
        init(samplingRate, attackTime, releaseTime, iirFilter);
    }

    private void init(int samplingRate, double attackTime, double releaseTime, IirFilter iirFilter) {
        gAttack = Math.exp(-1 / (samplingRate * attackTime));
        gRelease = Math.exp(-1 / (samplingRate * releaseTime));
        this.iirFilter = iirFilter;
    }


    public double step(double inputValue) {
        double prefiltered = (iirFilter == null) ? inputValue : iirFilter.step(inputValue);
        double inLevel = Math.abs(prefiltered);
        double g = (inLevel > level) ? gAttack : gRelease;
        level = g * level + (1 - g) * inLevel;
        return level;
    }


    public double[] process(double[] in) {
        double[] out = new double[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] =  step(in[i]);
        }
        return out;
    }

}
