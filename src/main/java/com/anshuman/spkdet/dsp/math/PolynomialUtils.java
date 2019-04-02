package com.anshuman.spkdet.dsp.math;


public class PolynomialUtils {


    private PolynomialUtils() {
    }


    public static class RationalFraction {
        public double[] top;
        public double[] bottom;
    }


    public static Complex evaluate(double[] a, Complex x) {
        if (a.length == 0) {
            throw new IllegalArgumentException();
        }
        Complex sum = new Complex(a[0]);
        for (int i = 1; i < a.length; i++) {
            sum = sum.mul(x).add(a[i]);
        }
        return sum;
    }


    public static Complex evaluate(RationalFraction f, Complex x) {
        Complex v1 = evaluate(f.top, x);
        Complex v2 = evaluate(f.bottom, x);
        return v1.div(v2);
    }


    public static double[] multiply(double[] a1, double[] a2) {
        int n1 = a1.length - 1;
        int n2 = a2.length - 1;
        int n3 = n1 + n2;
        double[] a3 = new double[n3 + 1];
        for (int i = 0; i <= n3; i++) {
            double t = 0;
            int p1 = Math.max(0, i - n2);
            int p2 = Math.min(n1, i);
            for (int j = p1; j <= p2; j++) {
                t += a1[n1 - j] * a2[n2 - i + j];
            }
            a3[n3 - i] = t;
        }
        return a3;
    }


    public static Complex[] multiply(Complex[] a1, Complex[] a2) {
        int n1 = a1.length - 1;
        int n2 = a2.length - 1;
        int n3 = n1 + n2;
        Complex[] a3 = new Complex[n3 + 1];
        for (int i = 0; i <= n3; i++) {
            Complex t = Complex.ZERO;
            int p1 = Math.max(0, i - n2);
            int p2 = Math.min(n1, i);
            for (int j = p1; j <= p2; j++) {
                t = t.add(a1[n1 - j].mul(a2[n2 - i + j]));
            }
            a3[n3 - i] = t;
        }
        return a3;
    }


    public static Complex[] deflate(Complex[] a, Complex z, double eps) {
        int n = a.length - 1;
        Complex[] a2 = new Complex[n];
        a2[0] = a[0];
        for (int i = 1; i < n; i++) {
            a2[i] = z.mul(a2[i - 1]).add(a[i]);
        }
        Complex remainder = z.mul(a2[n - 1]).add(a[n]);
        if (eps > 0 && (Math.abs(remainder.re()) > eps || Math.abs(remainder.im()) > eps)) {
            throw new RuntimeException("Polynom deflatation failed, remainder = " + remainder + ".");
        }
        return a2;
    }


    public static Complex[] expand(Complex[] zeros) {
        int n = zeros.length;
        if (n == 0) {
            return new Complex[]{Complex.ONE};
        }
        Complex[] a = new Complex[]{Complex.ONE, zeros[0].neg()};
        for (int i = 1; i < n; i++) {
            Complex[] a2 = new Complex[]{Complex.ONE, zeros[i].neg()};
            a = multiply(a, a2);
        }
        return a;
    }

}
