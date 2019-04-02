package com.anshuman.spkdet.dsp.filter;

import com.anshuman.spkdet.dsp.util.ArrayUtils;

public class IirFilterDesignExstrom {


    private IirFilterDesignExstrom() {
    }


    public static IirFilterCoefficients design(FilterPassType filterPassType, int filterOrder, double fcf1, double fcf2) {
        if (filterOrder < 1) {
            throw new IllegalArgumentException("Invalid filterOrder.");
        }
        if (fcf1 <= 0 || fcf1 >= 0.5) {
            throw new IllegalArgumentException("Invalid fcf1.");
        }
        if (filterPassType == FilterPassType.bandpass || filterPassType == FilterPassType.bandstop) {
            if (fcf2 <= 0 || fcf2 >= 0.5) {
                throw new IllegalArgumentException("Invalid fcf2.");
            }
        }
        IirFilterCoefficients coeffs = new IirFilterCoefficients();
        coeffs.a = calculateACoefficients(filterPassType, filterOrder, fcf1, fcf2);
        double[] bUnscaled = calculateBCoefficients(filterPassType, filterOrder, fcf1, fcf2);
        double scalingFactor = calculateScalingFactor(filterPassType, filterOrder, fcf1, fcf2);
        coeffs.b = ArrayUtils.multiply(bUnscaled, scalingFactor);
        return coeffs;
    }


    private static double[] calculateACoefficients(FilterPassType filterPassType, int filterOrder, double fcf1, double fcf2) {
        switch (filterPassType) {
            case lowpass:
            case highpass:
                return calculateACoefficients_lp_hp(filterOrder, fcf1);
            case bandpass:
            case bandstop:
                return calculateACoefficients_bp_bs(filterPassType, filterOrder, fcf1, fcf2);
            default:
                throw new AssertionError();
        }
    }


    private static double[] calculateACoefficients_lp_hp(int n, double fcf) {
        double[] rcof = new double[2 * n];
        double theta = 2 * Math.PI * fcf;
        double st = Math.sin(theta);
        double ct = Math.cos(theta);

        for (int k = 0; k < n; k++) {
            double parg = Math.PI * (2 * k + 1) / (2 * n);
            double sparg = Math.sin(parg);
            double cparg = Math.cos(parg);
            double a = 1 + st * sparg;
            rcof[2 * k] = -ct / a;
            rcof[2 * k + 1] = -st * cparg / a;
        }

        double[] wcof = binomial_mult(n, rcof);

        double[] dcof = new double[n + 1];
        dcof[0] = 1;
        for (int k = 1; k <= n; k++) {
            dcof[k] = wcof[2 * k - 2];
        }
        return dcof;
    }


    private static double[] calculateACoefficients_bp_bs(FilterPassType filterPassType, int n, double f1f, double f2f) {
        double cp = Math.cos(2 * Math.PI * (f2f + f1f) / 2);
        double theta = 2 * Math.PI * (f2f - f1f) / 2;
        double st = Math.sin(theta);
        double ct = Math.cos(theta);
        double s2t = 2 * st * ct;
        double c2t = 2 * ct * ct - 1;

        double[] rcof = new double[2 * n];
        double[] tcof = new double[2 * n];
        double flip = (filterPassType == FilterPassType.bandstop) ? -1 : 1;

        for (int k = 0; k < n; k++) {
            double parg = Math.PI * (2 * k + 1) / (2 * n);
            double sparg = Math.sin(parg);
            double cparg = Math.cos(parg);
            double a = 1 + s2t * sparg;
            rcof[2 * k] = c2t / a;
            rcof[2 * k + 1] = s2t * cparg / a * flip;
            tcof[2 * k] = -2 * cp * (ct + st * sparg) / a;
            tcof[2 * k + 1] = -2 * cp * st * cparg / a * flip;
        }

        double[] wcof = trinomial_mult(n, tcof, rcof);

        double[] dcof = new double[2 * n + 1];
        dcof[0] = 1;
        for (int k = 1; k <= 2 * n; k++) {
            dcof[k] = wcof[2 * k - 2];
        }
        return dcof;
    }


    private static double[] calculateBCoefficients(FilterPassType filterPassType, int filterOrder, double fcf1, double fcf2) {
        switch (filterPassType) {
            case lowpass: {
                int[] a = calculateBCoefficients_lp(filterOrder);
                return ArrayUtils.toDouble(a);
            }
            case highpass: {
                int[] a = calculateBCoefficients_hp(filterOrder);
                return ArrayUtils.toDouble(a);
            }
            case bandpass: {
                int[] a = calculateBCoefficients_bp(filterOrder);
                return ArrayUtils.toDouble(a);
            }
            case bandstop: {
                return calculateBCoefficients_bs(filterOrder, fcf1, fcf2);
            }
            default:
                throw new AssertionError();
        }
    }


    private static int[] calculateBCoefficients_lp(int n) {
        int[] ccof = new int[n + 1];
        ccof[0] = 1;
        ccof[1] = n;
        int m = n / 2;
        for (int i = 2; i <= m; i++) {
            ccof[i] = (n - i + 1) * ccof[i - 1] / i;
            ccof[n - i] = ccof[i];
        }
        ccof[n - 1] = n;
        ccof[n] = 1;
        return ccof;
    }


    private static int[] calculateBCoefficients_hp(int n) {
        int[] ccof = calculateBCoefficients_lp(n);
        for (int i = 1; i <= n; i += 2) {
            ccof[i] = -ccof[i];
        }
        return ccof;
    }


    private static int[] calculateBCoefficients_bp(int n) {
        int[] tcof = calculateBCoefficients_hp(n);
        int[] ccof = new int[2 * n + 1];
        for (int i = 0; i < n; i++) {
            ccof[2 * i] = tcof[i];
            ccof[2 * i + 1] = 0;
        }
        ccof[2 * n] = tcof[n];
        return ccof;
    }


    private static double[] calculateBCoefficients_bs(int n, double f1f, double f2f) {
        double alpha = -2 * Math.cos(2 * Math.PI * (f2f + f1f) / 2) / Math.cos(2 * Math.PI * (f2f - f1f) / 2);
        double[] ccof = new double[2 * n + 1];
        ccof[0] = 1;
        ccof[1] = alpha;
        ccof[2] = 1;
        for (int i = 1; i < n; i++) {
            ccof[2 * i + 2] += ccof[2 * i];
            for (int j = 2 * i; j > 1; j--) {
                ccof[j + 1] += alpha * ccof[j] + ccof[j - 1];
            }
            ccof[2] += alpha * ccof[1] + 1;
            ccof[1] += alpha;
        }
        return ccof;
    }


    private static double calculateScalingFactor(FilterPassType filterPassType, int filterOrder, double fcf1, double fcf2) {
        switch (filterPassType) {
            case lowpass:
            case highpass:
                return calculateScalingFactor_lp_hp(filterPassType, filterOrder, fcf1);
            case bandpass:
            case bandstop:
                return calculateScalingFactor_bp_bs(filterPassType, filterOrder, fcf1, fcf2);
            default:
                throw new AssertionError();
        }
    }


    private static double calculateScalingFactor_lp_hp(FilterPassType filterPassType, int n, double fcf) {
        double omega = 2 * Math.PI * fcf;
        double fomega = Math.sin(omega);
        double parg0 = Math.PI / (2 * n);
        int m = n / 2;
        double sf = 1;
        for (int k = 0; k < n / 2; k++) {
            sf *= 1 + fomega * Math.sin((2 * k + 1) * parg0);
        }
        double fomega2;
        switch (filterPassType) {
            case lowpass: {
                fomega2 = Math.sin(omega / 2);
                if (n % 2 != 0) {
                    sf *= fomega2 + Math.cos(omega / 2);
                }
                break;
            }
            case highpass: {
                fomega2 = Math.cos(omega / 2);
                if (n % 2 != 0) {
                    sf *= fomega2 + Math.sin(omega / 2);
                }
                break;
            }
            default:
                throw new AssertionError();
        }
        sf = Math.pow(fomega2, n) / sf;
        return sf;
    }


    private static double calculateScalingFactor_bp_bs(FilterPassType filterPassType, int n, double f1f, double f2f) {
        double tt = Math.tan(2 * Math.PI * (f2f - f1f) / 2);
        double ctt_tt = (filterPassType == FilterPassType.bandpass) ? 1 / tt : tt;
        double sfr = 1;
        double sfi = 0;
        for (int k = 0; k < n; k++) {
            double parg = Math.PI * (2 * k + 1) / (2 * n);
            double sparg = ctt_tt + Math.sin(parg);
            double cparg = Math.cos(parg);
            double a = (sfr + sfi) * (sparg - cparg);
            double b = sfr * sparg;
            double c = -sfi * cparg;
            sfr = b - c;
            sfi = a - b - c;
        }
        return 1 / sfr;
    }


    private static double[] binomial_mult(int n, double[] p) {
        double[] a = new double[2 * n];
        for (int i = 0; i < n; i++) {
            for (int j = i; j > 0; j--) {
                a[2 * j] += p[2 * i] * a[2 * (j - 1)] - p[2 * i + 1] * a[2 * (j - 1) + 1];
                a[2 * j + 1] += p[2 * i] * a[2 * (j - 1) + 1] + p[2 * i + 1] * a[2 * (j - 1)];
            }
            a[0] += p[2 * i];
            a[1] += p[2 * i + 1];
        }
        return a;
    }


    private static double[] trinomial_mult(int n, double[] b, double[] c) {
        double[] a = new double[4 * n];

        a[2] = c[0];
        a[3] = c[1];
        a[0] = b[0];
        a[1] = b[1];

        for (int i = 1; i < n; i++) {
            a[2 * (2 * i + 1)] += c[2 * i] * a[2 * (2 * i - 1)] - c[2 * i + 1] * a[2 * (2 * i - 1) + 1];
            a[2 * (2 * i + 1) + 1] += c[2 * i] * a[2 * (2 * i - 1) + 1] + c[2 * i + 1] * a[2 * (2 * i - 1)];

            for (int j = 2 * i; j > 1; j--) {
                a[2 * j] += b[2 * i] * a[2 * (j - 1)] - b[2 * i + 1] * a[2 * (j - 1) + 1] + c[2 * i] * a[2 * (j - 2)] - c[2 * i + 1] * a[2 * (j - 2) + 1];
                a[2 * j + 1] += b[2 * i] * a[2 * (j - 1) + 1] + b[2 * i + 1] * a[2 * (j - 1)] + c[2 * i] * a[2 * (j - 2) + 1] + c[2 * i + 1] * a[2 * (j - 2)];
            }

            a[2] += b[2 * i] * a[0] - b[2 * i + 1] * a[1] + c[2 * i];
            a[3] += b[2 * i] * a[1] + b[2 * i + 1] * a[0] + c[2 * i + 1];
            a[0] += b[2 * i];
            a[1] += b[2 * i + 1];
        }

        return a;
    }

}
