package org.firstinspires.ftc.teamcode.utils;


public class VectorMath {
    public static double[] rotateVector(double x, double y, double angleInRadians) {
        double cos = Math.cos(angleInRadians);
        double sin = Math.sin(angleInRadians);

        double newX = x * cos - y * sin;
        double newY = x * sin + y * cos;

        return new double[]{newX, newY};
    }



}
