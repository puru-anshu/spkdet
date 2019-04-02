package com.anshuman.spkdet.dsp.signal;
public class RmsNormalizer {


    public static void normalize(double[][] signals, double targetRms, int segmentSize) {
        double maxRms = findMaxRmsLevel(signals, segmentSize);
        if (maxRms == 0) {
            return;
        }
        double factor = targetRms / maxRms;

        amplifySignal(signals, factor);
    }

    public static double findMaxRmsLevel(double[][] signals, int segmentSize) {
        double maxRms = 0;
        for (int channel = 0; channel < signals.length; channel++) {
            double rms = findMaxRmsLevel(signals[channel], segmentSize);
            if (rms > maxRms) {
                maxRms = rms;
            }
        }
        return maxRms;
    }

    private static double findMaxRmsLevel(double[] signal, int segmentSize) {
        double maxRms = 0;
        int p = 0;
        while (p < signal.length) {
            int endP = (p + segmentSize * 5 / 3 > signal.length) ? signal.length : p + segmentSize;

            double rms = computeRms(signal, p, endP - p);
            if (rms > maxRms) {
                maxRms = rms;
            }
            p = endP;
        }
        return maxRms;
    }

    private static double computeRms(double[] signal, int startPos, int len) {
        double a = 0;
        for (int p = startPos; p < startPos + len; p++) {
            a += signal[p] * signal[p];
        }
        return Math.sqrt(a / len);
    }

    private static void amplifySignal(double[][] signals, double factor) {
        for (int channel = 0; channel < signals.length; channel++) {
            amplifySignal(signals[channel], factor);
        }
    }

    private static void amplifySignal(double[] signal, double factor) {
        for (int p = 0; p < signal.length; p++) {
            signal[p] *= factor;
        }
    }

}
