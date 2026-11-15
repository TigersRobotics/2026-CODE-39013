package org.firstinspires.ftc.teamcode.utils;


public final class UtilMethods {

    public static double expo(double input, double exponent) {
        return Math.signum(input)*Math.pow(Math.abs(input), exponent);
    }

    public static double clip(double num, double min, double max) {
        return Math.min(Math.max(num, min), max);
    }

}
