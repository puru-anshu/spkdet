package com.anshuman.spkdet.dsp.math;


public final class Complex {


    public static final Complex I = new Complex(0, 1);


    public static final Complex ZERO = new Complex(0);


    public static final Complex ONE = new Complex(1);


    public static final Complex TWO = new Complex(2);


    public static final Complex NaN = new Complex(Double.NaN, Double.NaN);


    public static final Complex INF = new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

    private final double re;
    private final double im;


    public Complex(double re, double im) {
        this.re = re;
        this.im = im;
    }


    public Complex(double re) {
        this(re, 0);
    }


    public double re() {
        return re;
    }


    public double im() {
        return im;
    }


    public double toDouble(double eps) {
        double absIm = Math.abs(im);
        if (absIm > eps && absIm > Math.abs(re) * eps) {
            throw new RuntimeException("The imaginary part of the complex number is not neglectable small for the conversion to a real number. re=" + re + " im=" + im + " eps=" + eps + ".");
        }
        return re;
    }


    public boolean isNaN() {
        return Double.isNaN(re) || Double.isNaN(im);
    }


    public boolean isInfinite() {
        return Double.isInfinite(re) || Double.isInfinite(im);
    }




    public static Complex expj(double arg) {
        return new Complex(Math.cos(arg), Math.sin(arg));
    }


    public static Complex fromPolar(double abs, double arg) {
        return new Complex(abs * Math.cos(arg), abs * Math.sin(arg));
    }




    public double abs() {
        return Math.hypot(re, im);
    }


    public double arg() {
        return Math.atan2(im, re);
    }


    public Complex conj() {
        return new Complex(re, -im);
    }


    public Complex neg() {
        return new Complex(-re, -im);
    }


    public Complex reciprocal() {
        if (isNaN()) {
            return NaN;
        }
        if (isInfinite()) {
            return new Complex(0, 0);
        }
        double scale = re * re + im * im;
        if (scale == 0) {
            return INF;
        }
        return new Complex(re / scale, -im / scale);
    }


    public Complex exp() {
        return fromPolar(Math.exp(re), im);
    }


    public Complex log() {
        return new Complex(Math.log(abs()), arg());
    }


    public Complex sqr() {
        return new Complex(re * re - im * im, 2 * re * im);
    }


    public Complex sqrt() {
        if (re == 0 && im == 0) {
            return new Complex(0, 0);
        }
        double m = abs();
        return new Complex(Math.sqrt((m + re) / 2), Math.copySign(1, im) * Math.sqrt((m - re) / 2));
    }


















    public Complex add(double x) {
        return new Complex(re + x, im);
    }


    public Complex add(Complex x) {
        return new Complex(re + x.re, im + x.im);
    }


    public Complex sub(double x) {
        return new Complex(re - x, im);
    }


    public Complex sub(Complex x) {
        return new Complex(re - x.re, im - x.im);
    }


    public static Complex sub(double x, Complex y) {
        return new Complex(x - y.re, -y.im);
    }


    public Complex mul(double x) {
        return new Complex(re * x, im * x);
    }


    public Complex mul(Complex x) {
        return new Complex(re * x.re - im * x.im, re * x.im + im * x.re);
    }


    public Complex div(double x) {
        return new Complex(re / x, im / x);
    }


    public Complex div(Complex x) {
        double m = x.re * x.re + x.im * x.im;
        return new Complex((re * x.re + im * x.im) / m, (im * x.re - re * x.im) / m);
    }


    public static Complex div(double x, Complex y) {
        double m = y.re * y.re + y.im * y.im;
        return new Complex(x * y.re / m, -x * y.im / m);
    }


    public Complex pow(int x) {
        return fromPolar(Math.pow(abs(), x), arg() * x);
    }


    public Complex pow(double x) {
        return log().mul(x).exp();
    }


    public Complex pow(Complex x) {
        return log().mul(x).exp();
    }



    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Complex)) {
            return false;
        }
        Complex x = (Complex) obj;
        return re == x.re && im == x.im;
    }

    @Override
    public int hashCode() {
        long b1 = Double.doubleToLongBits(re);
        long b2 = Double.doubleToLongBits(im);
        return (int) (b1 ^ (b1 >>> 32) ^ b2 ^ (b2 >>> 32));
    }

    @Override
    public String toString() {
        return "(" + re + ", " + im + ")";
    }

}
