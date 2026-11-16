package org.firstinspires.ftc.teamcode.utils;


public final class UtilMethods {

    public static double expo(double input, double exponent) {
        return Math.signum(input)*Math.pow(Math.abs(input), exponent);
    }

    public static double clip(double num, double min, double max) {
        return Math.min(Math.max(num, min), max);
    }

    public static double interpolate(double[] xs, double[] ys, double x) {
        if (x <= xs[0]) return ys[0];
        for (int i = 1; i < xs.length; i++) {
            if (x <= xs[i]) return ys[i - 1] + (ys[i] - ys[i - 1]) * (x - xs[i - 1]) / (xs[i] - xs[i - 1]);
        }
        return ys[ys.length - 1];
    }

}
