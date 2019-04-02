package com.anshuman.spkdet.dsp.signal;

import java.util.Random;


public class BrownNoiseGenerator {

    private double minValue;
    private double maxValue;
    private double slope;
    private double hpFilter;
    private double valueRange;
    private double centerValue;
    private double currentValue;
    private Random random;


    public BrownNoiseGenerator() {
        this(-1.0, 1.0);
    }


    public BrownNoiseGenerator(double minValue, double maxValue) {
        this(minValue, maxValue, (maxValue - minValue) / 20, 0.02);
    }


    public BrownNoiseGenerator(double minValue, double maxValue, double slope, double hpFilter) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.slope = slope;
        this.hpFilter = hpFilter;
        if (minValue >= maxValue) {
            throw new IllegalArgumentException("Invalid minValue/maxValue.");
        }
        valueRange = maxValue - minValue;
        if (slope <= 0 || slope >= valueRange / 2) {
            throw new IllegalArgumentException("Invalid slope.");
        }

        if (hpFilter < 0 || hpFilter >= 1) {
            throw new IllegalArgumentException("Invalid hpFilter value.");
        }
        centerValue = (minValue + maxValue) / 2;
        currentValue = centerValue;
        random = new Random();
    }


    public double getNext() {
        double whiteNoise = (random.nextFloat() * 2 - 1) * slope;


        double v = currentValue;
        if (hpFilter > 0) {
            v -= (v - centerValue) * hpFilter;
        }
        double next = v + whiteNoise;
        if (next < minValue || next > maxValue) {
            next = v - whiteNoise;
        }
        currentValue = next;
        return next;
    }

}
