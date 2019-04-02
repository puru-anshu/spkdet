package com.anshuman.spkdet.dsp.filter;


import com.anshuman.spkdet.dsp.math.Complex;
import com.anshuman.spkdet.dsp.math.PolynomialUtils;
import com.anshuman.spkdet.dsp.util.ArrayUtils;

public class IirFilterDesignFisher {

    private static class PolesAndZeros {
        public Complex[] poles;
        public Complex[] zeros;
    }


    private IirFilterDesignFisher() {
    }


    private static Complex[] getPoles(FilterCharacteristicsType filterCharacteristicsType, int filterOrder, double ripple) {
        switch (filterCharacteristicsType) {
            case bessel: {
                Complex[] poles = BesselFilterDesign.computePoles(filterOrder);



                return poles;
            }
            case butterworth: {
                Complex[] poles = new Complex[filterOrder];
                for (int i = 0; i < filterOrder; i++) {
                    double theta = (filterOrder / 2.0 + 0.5 + i) * Math.PI / filterOrder;
                    poles[i] = Complex.expj(theta);
                }
                return poles;
            }
            case chebyshev: {
                if (ripple >= 0.0) {
                    throw new IllegalArgumentException("Chebyshev ripple must be negative.");
                }
                Complex[] poles = getPoles(FilterCharacteristicsType.butterworth, filterOrder, 0);
                double rip = Math.pow(10, -ripple / 10);
                double eps = Math.sqrt(rip - 1);
                double y = asinh(1.0 / eps) / filterOrder;
                if (y <= 0) {
                    throw new AssertionError();
                }
                double sinhY = Math.sinh(y);
                double coshY = Math.cosh(y);
                for (int i = 0; i < filterOrder; i++) {
                    poles[i] = new Complex(poles[i].re() * sinhY, poles[i].im() * coshY);
                }
                return poles;
            }
            default:
                throw new UnsupportedOperationException("Filter characteristics type " + filterCharacteristicsType + " not yet implemented.");
        }
    }


    private static PolesAndZeros normalize(Complex[] poles, FilterPassType filterPassType,
                                           double fcf1,
                                           double fcf2, boolean preWarp) {
        int n = poles.length;
        boolean fcf2IsRelevant = filterPassType == FilterPassType.bandpass || filterPassType == FilterPassType.bandstop;
        if (fcf1 <= 0 || fcf1 >= 0.5) {
            throw new IllegalArgumentException("Invalid fcf1.");
        }
        if (fcf2IsRelevant && (fcf2 <= 0 || fcf2 >= 0.5)) {
            throw new IllegalArgumentException("Invalid fcf2.");
        }
        double fcf1Warped = Math.tan(Math.PI * fcf1) / Math.PI;
        double fcf2Warped = fcf2IsRelevant ? Math.tan(Math.PI * fcf2) / Math.PI : 0;
        double w1 = 2 * Math.PI * (preWarp ? fcf1Warped : fcf1);
        double w2 = 2 * Math.PI * (preWarp ? fcf2Warped : fcf2);
        switch (filterPassType) {
            case lowpass: {
                PolesAndZeros sPlane = new PolesAndZeros();
                sPlane.poles = ArrayUtils.multiply(poles, w1);
                sPlane.zeros = new Complex[0];
                return sPlane;
            }
            case highpass: {
                PolesAndZeros sPlane = new PolesAndZeros();
                sPlane.poles = new Complex[n];
                for (int i = 0; i < n; i++) {
                    sPlane.poles[i] = Complex.div(w1, poles[i]);
                }
                sPlane.zeros = ArrayUtils.zeros(n);
                return sPlane;
            }
            case bandpass: {
                double w0 = Math.sqrt(w1 * w2);
                double bw = w2 - w1;
                PolesAndZeros sPlane = new PolesAndZeros();
                sPlane.poles = new Complex[2 * n];
                for (int i = 0; i < n; i++) {
                    Complex hba = poles[i].mul(bw / 2);
                    Complex temp = Complex.sub(1, Complex.div(w0, hba).sqr()).sqrt();
                    sPlane.poles[i] = hba.mul(temp.add(1));
                    sPlane.poles[n + i] = hba.mul(Complex.sub(1, temp));
                }
                sPlane.zeros = ArrayUtils.zeros(n);
                return sPlane;
            }
            case bandstop: {
                double w0 = Math.sqrt(w1 * w2);
                double bw = w2 - w1;
                PolesAndZeros sPlane = new PolesAndZeros();
                sPlane.poles = new Complex[2 * n];
                for (int i = 0; i < n; i++) {
                    Complex hba = Complex.div(bw / 2, poles[i]);
                    Complex temp = Complex.sub(1, Complex.div(w0, hba).sqr()).sqrt();
                    sPlane.poles[i] = hba.mul(temp.add(1));
                    sPlane.poles[n + i] = hba.mul(Complex.sub(1, temp));
                }
                sPlane.zeros = new Complex[2 * n];
                for (int i = 0; i < n; i++) {
                    sPlane.zeros[i] = new Complex(0, w0);
                    sPlane.zeros[n + i] = new Complex(0, -w0);
                }
                return sPlane;
            }
            default:
                throw new UnsupportedOperationException("Filter pass type " + filterPassType + " not yet implemented.");
        }
    }

    private enum SToZMappingMethod {
        bilinearTransform, matchedZTransform
    }


    private static PolesAndZeros MapSPlaneToZPlane(PolesAndZeros sPlane, SToZMappingMethod sToZMappingMethod) {
        switch (sToZMappingMethod) {
            case bilinearTransform: {
                PolesAndZeros zPlane = new PolesAndZeros();
                zPlane.poles = doBilinearTransform(sPlane.poles);
                Complex[] a = doBilinearTransform(sPlane.zeros);
                zPlane.zeros = extend(a, sPlane.poles.length, new Complex(-1));
                return zPlane;
            }
            case matchedZTransform: {
                PolesAndZeros zPlane = new PolesAndZeros();
                zPlane.poles = doMatchedZTransform(sPlane.poles);
                zPlane.zeros = doMatchedZTransform(sPlane.zeros);
                return zPlane;
            }
            default:
                throw new UnsupportedOperationException("Mapping method " + sToZMappingMethod + " not yet implemented.");
        }
    }

    private static Complex[] doBilinearTransform(Complex[] a) {
        Complex[] a2 = new Complex[a.length];
        for (int i = 0; i < a.length; i++) {
            a2[i] = doBilinearTransform(a[i]);
        }
        return a2;
    }

    private static Complex doBilinearTransform(Complex x) {
        return x.add(2).div(Complex.sub(2, x));
    }

    private static Complex[] extend(Complex[] a, int n, Complex fill) {
        if (a.length >= n) {
            return a;
        }
        Complex[] a2 = new Complex[n];
        for (int i = 0; i < a.length; i++) {
            a2[i] = a[i];
        }
        for (int i = a.length; i < n; i++) {
            a2[i] = fill;
        }
        return a2;
    }

    private static Complex[] doMatchedZTransform(Complex[] a) {
        Complex[] a2 = new Complex[a.length];
        for (int i = 0; i < a.length; i++) {
            a2[i] = a[i].exp();
        }
        return a2;
    }


    private static PolynomialUtils.RationalFraction computeTransferFunction(PolesAndZeros zPlane) {
        Complex[] topCoeffsComplex = PolynomialUtils.expand(zPlane.zeros);
        Complex[] bottomCoeffsComplex = PolynomialUtils.expand(zPlane.poles);
        final double eps = 1E-10;
        PolynomialUtils.RationalFraction tf = new PolynomialUtils.RationalFraction();
        tf.top = ArrayUtils.toDouble(topCoeffsComplex, eps);
        tf.bottom = ArrayUtils.toDouble(bottomCoeffsComplex, eps);


        return tf;
    }


    private static double computeGain(PolynomialUtils.RationalFraction tf, FilterPassType filterPassType, double fcf1, double fcf2) {
        switch (filterPassType) {
            case lowpass: {
                return computeGainAt(tf, Complex.ONE);
            }
            case highpass: {
                return computeGainAt(tf, new Complex(-1));
            }
            case bandpass: {
                double centerFreq = (fcf1 + fcf2) / 2;
                Complex w = Complex.expj(2 * Math.PI * centerFreq);
                return computeGainAt(tf, w);
            }
            case bandstop: {
                double dcGain = computeGainAt(tf, Complex.ONE);
                double hfGain = computeGainAt(tf, new Complex(-1));
                return Math.sqrt(dcGain * hfGain);
            }
            default: {
                throw new RuntimeException("Unsupported filter pass type.");
            }
        }
    }

    private static double computeGainAt(PolynomialUtils.RationalFraction tf, Complex w) {
        return PolynomialUtils.evaluate(tf, w).abs();
    }


    private static IirFilterCoefficients computeIirFilterCoefficients(PolynomialUtils.RationalFraction tf) {


        double scale = tf.bottom[0];
        IirFilterCoefficients coeffs = new IirFilterCoefficients();
        coeffs.a = ArrayUtils.divide(tf.bottom, scale);
        coeffs.a[0] = 1;
        coeffs.b = ArrayUtils.divide(tf.top, scale);
        return coeffs;
    }


    public static IirFilterCoefficients design(FilterPassType filterPassType, FilterCharacteristicsType filterCharacteristicsType,
                                               int filterOrder, double ripple, double fcf1, double fcf2) {
        Complex[] poles = getPoles(filterCharacteristicsType, filterOrder, ripple);
        SToZMappingMethod sToZMappingMethod = (filterCharacteristicsType == FilterCharacteristicsType.bessel) ? SToZMappingMethod.matchedZTransform : SToZMappingMethod.bilinearTransform;
        boolean preWarp = sToZMappingMethod == SToZMappingMethod.bilinearTransform;
        PolesAndZeros sPlane = normalize(poles, filterPassType, fcf1, fcf2, preWarp);

        PolesAndZeros zPlane = MapSPlaneToZPlane(sPlane, sToZMappingMethod);


        PolynomialUtils.RationalFraction tf = computeTransferFunction(zPlane);
        double gain = computeGain(tf, filterPassType, fcf1, fcf2);

        IirFilterCoefficients coeffs = computeIirFilterCoefficients(tf);


        coeffs.b = ArrayUtils.divide(coeffs.b, gain);
        return coeffs;
    }

    private static double asinh(double x) {
        return Math.log(x + Math.sqrt(1 + x * x));
    }

}
