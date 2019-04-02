

package com.anshuman.spkdet.dsp.util;


import com.anshuman.spkdet.dsp.math.Complex;

import java.util.Arrays;
import java.util.Comparator;


public class ArrayUtils {


    private ArrayUtils() {
    }


    public static Complex[] zeros(int n) {
        Complex[] a = new Complex[n];
        for (int i = 0; i < n; i++) {
            a[i] = Complex.ZERO;
        }
        return a;
    }


    public static double[] toDouble(Complex[] a, double eps) {
        double[] a2 = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            a2[i] = a[i].toDouble(eps);
        }
        return a2;
    }


    public static double[] toDouble(int[] a) {
        double[] a2 = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            a2[i] = a[i];
        }
        return a2;
    }


    public static double[] toDouble(Double[] a) {
        double[] a2 = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            a2[i] = a[i];
        }
        return a2;
    }


    public static Complex[] toComplex(double[] a) {
        Complex[] a2 = new Complex[a.length];
        for (int i = 0; i < a.length; i++) {
            a2[i] = new Complex(a[i]);
        }
        return a2;
    }


    public static Double[] toObject(double[] a) {
        Double[] a2 = new Double[a.length];
        for (int i = 0; i < a.length; i++) {
            a2[i] = a[i];
        }
        return a2;
    }


    public static double[] multiply(double[] a, double f) {
        double[] a2 = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            a2[i] = a[i] * f;
        }
        return a2;
    }


    public static Complex[] multiply(Complex[] a, double f) {
        Complex[] a2 = new Complex[a.length];
        for (int i = 0; i < a.length; i++) {
            a2[i] = a[i].mul(f);
        }
        return a2;
    }


    public static double[] divide(double[] a, double f) {
        double[] a2 = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            a2[i] = a[i] / f;
        }
        return a2;
    }


    public static Complex[] divide(Complex[] a, double f) {
        Complex[] a2 = new Complex[a.length];
        for (int i = 0; i < a.length; i++) {
            a2[i] = a[i].div(f);
        }
        return a2;
    }


    public static double[] reverse(double[] a) {
        double[] a2 = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            a2[i] = a[a.length - 1 - i];
        }
        return a2;
    }


    public static double[] sortByMagnitude(double[] a) {
        Double[] a2 = toObject(a);
        Arrays.sort(a2, new Comparator<Double>() {
            public int compare(Double o1, Double o2) {
                return Double.compare(Math.abs(o1.doubleValue()), Math.abs(o2.doubleValue()));
            }

            public boolean equals(Object obj) {
                throw new AssertionError();
            }
        });
        double[] a3 = toDouble(a2);
        return a3;
    }


    public static Complex[] sortByImRe(Complex[] a) {
        Complex[] a2 = copy(a);
        Arrays.sort(a2, new Comparator<Complex>() {
            public int compare(Complex o1, Complex o2) {
                if (o1.im() < o2.im()) {
                    return -1;
                } else if (o1.im() > o2.im()) {
                    return 1;
                }
                return Double.compare(o1.re(), o2.re());
            }

            public boolean equals(Object obj) {
                throw new AssertionError();
            }
        });
        return a2;
    }


    public static Complex[] sortByAbsImNegImRe(Complex[] a) {
        Complex[] a2 = copy(a);
        Arrays.sort(a2, new Comparator<Complex>() {
            public int compare(Complex o1, Complex o2) {
                double absIm1 = Math.abs(o1.im());
                double absIm2 = Math.abs(o2.im());
                if (absIm1 < absIm2) {
                    return -1;
                } else if (absIm1 > absIm2) {
                    return 1;
                }
                if (o1.im() > o2.im()) {
                    return -1;
                } else if (o1.im() < o2.im()) {
                    return 1;
                }
                return Double.compare(o1.re(), o2.re());
            }

            public boolean equals(Object obj) {
                throw new AssertionError();
            }
        });
        return a2;
    }

    private static Complex[] copy(Complex[] a) {
        Complex[] a2 = new Complex[a.length];
        for (int i = 0; i < a.length; i++) {
            a2[i] = a[i];
        }
        return a2;
    }


    public static String toString(double[] a) {
        StringBuilder s = new StringBuilder();
        s.append("[");
        for (int i = 0; i < a.length; i++) {
            if (i > 0) {
                s.append(" ");
            }
            s.append(a[i]);
        }
        s.append("]");
        return s.toString();
    }


    public static String toString(Complex[] a) {
        StringBuilder s = new StringBuilder();
        s.append("[");
        for (int i = 0; i < a.length; i++) {
            if (i > 0) {
                s.append(" ");
            }
            s.append(a[i]);
        }
        s.append("]");
        return s.toString();
    }

}
