package com.anshuman.spkdet.dsp.math;


import com.anshuman.spkdet.dsp.util.ArrayUtils;

public class PolynomialRootFinderLaguerre {

    private static final double EPSS = 1E-14;


    private PolynomialRootFinderLaguerre() {
    }


    public static Complex[] findRoots(double[] coeffs) {
        return findRoots(ArrayUtils.toComplex(coeffs));
    }


    public static Complex[] findRoots(Complex[] coeffs) {
        int n = coeffs.length - 1;
        Complex[] zeros = new Complex[n];
        Complex[] a = coeffs;
        for (int i = 0; i < n; i++) {
            Complex zero;
            int ctr = 0;
            while (true) {
                Complex startX = (ctr == 0) ? new Complex(0) : randomStart();
                zero = laguer(a, startX);
                if (zero != null) {
                    break;
                }
                if (ctr++ > 1000) {
                    throw new RuntimeException("Root finding aborted in random loop.");
                }
            }
            zeros[i] = zero;
            a = PolynomialUtils.deflate(a, zero, 0);
        }

        for (int i = 0; i < n; i++) {
            zeros[i] = laguer(coeffs, zeros[i]);
            if (zeros[i] == null) {
                throw new RuntimeException("Polish failed.");
            }
        }
        return zeros;
    }

    private static Complex laguer(Complex[] a, Complex startX) {
        int n = a.length - 1;
        Complex cn = new Complex(n);
        Complex x = startX;
        for (int iter = 0; iter < 80; iter++) {
            Complex b = a[0];
            double err = b.abs();
            Complex d = Complex.ZERO;
            Complex f = Complex.ZERO;
            double absX = x.abs();
            for (int i = 1; i <= n; i++) {
                f = x.mul(f).add(d);
                d = x.mul(d).add(b);
                b = x.mul(b).add(a[i]);
                err = b.abs() + absX * err;
            }
            err *= EPSS;
            if (b.abs() <= err) {
                return x;
            }
            Complex g = d.div(b);
            Complex g2 = g.mul(g);
            Complex h = g2.sub(Complex.TWO.mul(f.div(b)));
            Complex sq = cn.sub(Complex.ONE).mul(cn.mul(h).sub(g2)).sqrt();
            Complex gp = g.add(sq);
            Complex gm = g.sub(sq);
            double abp = gp.abs();
            double abm = gm.abs();
            if (abp < abm) {
                gp = gm;
            }
            Complex dx;
            if (abp > 0 || abm > 0) {
                dx = cn.div(gp);
            } else {
                dx = new Complex(Math.log(1 + absX), iter + 1).exp();
            }
            x = x.sub(dx);
        }
        return null;
    }

    private static Complex randomStart() {
        return new Complex(Math.random() * 2 - 1, Math.random() * 2 - 1);
    }

}
