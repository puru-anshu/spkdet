

package com.anshuman.spkdet.dsp.filter;


public class IirFilter {

    private int n1;
    private int n2;
    private double[] a;
    private double[] b;

    private double[] buf1;
    private double[] buf2;
    private int pos1;
    private int pos2;


    public IirFilter(IirFilterCoefficients coeffs) {
        a = coeffs.a;
        b = coeffs.b;
        if (a.length < 1 || b.length < 1 || a[0] != 1.0) {
            throw new IllegalArgumentException("Invalid coefficients.");
        }
        n1 = b.length - 1;
        n2 = a.length - 1;
        buf1 = new double[n1];
        buf2 = new double[n2];
    }


    public double step(double inputValue) {
        double acc = b[0] * inputValue;
        for (int j = 1; j <= n1; j++) {
            int p = (pos1 + n1 - j) % n1;
            acc += b[j] * buf1[p];
        }
        for (int j = 1; j <= n2; j++) {
            int p = (pos2 + n2 - j) % n2;
            acc -= a[j] * buf2[p];
        }
        if (n1 > 0) {
            buf1[pos1] = inputValue;
            pos1 = (pos1 + 1) % n1;
        }
        if (n2 > 0) {
            buf2[pos2] = acc;
            pos2 = (pos2 + 1) % n2;
        }
        return acc;
    }

}
