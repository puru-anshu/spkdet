package com.anshuman.spkdet.dsp.filter;


import com.anshuman.spkdet.dsp.math.Complex;
import com.anshuman.spkdet.dsp.math.PolynomialRootFinderJenkinsTraub;
import com.anshuman.spkdet.dsp.math.PolynomialUtils;
import com.anshuman.spkdet.dsp.util.ArrayUtils;

public class BesselFilterDesign {


    private BesselFilterDesign() {
    }


    public static double[] computePolynomialCoefficients(int n) {
        double m = 1;
        for (int i = 1; i <= n; i++) {
            m = m * (n + i) / 2;
        }
        double[] a = new double[n + 1];
        a[0] = m;
        a[n] = 1;
        for (int i = 1; i < n; i++) {
            a[i] = a[i - 1] * 2 * (n - i + 1) / (2 * n - i + 1) / i;
        }
        return a;
    }


    public static Complex transferFunction(double[] polyCoeffs, Complex s) {
        PolynomialUtils.RationalFraction f = new PolynomialUtils.RationalFraction();
        f.top = new double[]{polyCoeffs[polyCoeffs.length - 1]};
        f.bottom = polyCoeffs;
        return PolynomialUtils.evaluate(f, s);
    }


    public static double computeGain(double[] polyCoeffs, double w) {
        Complex s = new Complex(0, w);
        Complex t = transferFunction(polyCoeffs, s);
        return t.abs();
    }


    public static double findFrequencyForGain(double[] polyCoeffs, double gain) {
        final double eps = 1E-15;
        if (gain > (1 - 1E-6) || gain < 1E-6) {
            throw new IllegalArgumentException();
        }
        int ctr;

        double wLo = 1;
        ctr = 0;
        while (computeGain(polyCoeffs, wLo) < gain) {
            wLo /= 2;
            if (ctr++ > 100) {
                throw new AssertionError();
            }
        }

        double wHi = 1;
        ctr = 0;
        while (computeGain(polyCoeffs, wHi) > gain) {
            wHi *= 2;
            if (ctr++ > 100) {
                throw new AssertionError();
            }
        }

        ctr = 0;
        while (true) {
            if (wHi - wLo < eps) {
                break;
            }
            double wm = (wHi + wLo) / 2;
            double gm = computeGain(polyCoeffs, wm);
            if (gm > gain) {
                wLo = wm;
            } else {
                wHi = wm;
            }
            if (ctr++ > 1000) {
                throw new AssertionError("No convergence.");
            }
        }
        return wLo;
    }


    public static double findFrequencyScalingFactor(double[] polyCoeffs) {
        double dB3 = 1 / Math.sqrt(2);
        return findFrequencyForGain(polyCoeffs, dB3);
    }


    public static Complex[] computePoles(int n) {
        double[] besselPolyCoeffs = computePolynomialCoefficients(n);
        double[] polyCoeffs = ArrayUtils.reverse(besselPolyCoeffs);
        double scalingFactor = findFrequencyScalingFactor(polyCoeffs);
        Complex[] poles = PolynomialRootFinderJenkinsTraub.findRoots(polyCoeffs);
        Complex[] scaledPoles = ArrayUtils.divide(poles, scalingFactor);
        return scaledPoles;
    }

}
