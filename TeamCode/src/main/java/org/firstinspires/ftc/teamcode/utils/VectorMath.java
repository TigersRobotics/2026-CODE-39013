package org.firstinspires.ftc.teamcode.utils;

import org.firstinspires.ftc.robotcore.external.Const;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class VectorMath {

    public static double[] rotateVector(double x, double y, double angleInRadians) {
        double cos = Math.cos(angleInRadians);
        double sin = Math.sin(angleInRadians);

        double newX = x * cos - y * sin;
        double newY = x * sin + y * cos;

        return new double[]{newX, newY};
    }

    public static double[] rotateVector(double x, double y, double angle, boolean inRad) {
        if (inRad) {
            return rotateVector(x, y, angle);
        } else {
            return rotateVector(x, y, Math.toRadians(angle));
        }
    }


    public static double shotAngle(double d, double h, double r, double camAngle, Telemetry telemetry) {
        double [] nV = addV(new double[]{d, camAngle}, new double[]{Constants.APRIL_TAG_HIVE_OFFSET_D, 180-r});
        double y = h + Constants.APRIL_TAG_HIVE_OFFSET_H;
        double x = nV[0];

        // it will try to calculate the angle for trajectory
        double angle = angleCalc((byte) -1, x, y);
        double nAngle;

        // checks if the angle is valid
        if (angle >= Constants.MAX_TURRET_ANGLE_UP ||
                angle <= Constants.MAX_TURRET_ANGLE_DOWN) {

            // invalid initial angle will return a second angle
            nAngle = angleCalc((byte) 1, x, y);
            if (nAngle >= Constants.MAX_TURRET_ANGLE_UP ||
                    nAngle <= Constants.MAX_TURRET_ANGLE_DOWN) {

                // will log that it cannot find valid angle, shows init angle and backup angle
                telemetry.addLine(("No valid angle found: " + angle + " " + nAngle));
                return -1;
            }
            return nAngle;
        }

        return angle;
    }

    private static double angleCalc(byte add, double x, double y) {
        double g = 9.800665;

        double v = Constants.BALL_SPEED;
        double v2 = Math.pow(v, 2);
        double v4 = Math.pow(v, 4);

        double x2 = Math.pow(x, 2);
        double root = (add) * Math.sqrt((v4 - g * (g * x2 + 2 * y * v2)));
        double arg = (v4 + root) / (g * x);

        return Math.atan(arg);
    }

    private static double[] addV(double[] v1, double[] v2){
        double r1 = v1[0];
        double t1 = v1[1]*Math.PI/180;

        double r2 = v2[0];
        double t2 = v2[1]*Math.PI/180;

        double x1 = r1*Math.cos(t1);
        double y1 = r1*Math.sin(t1);

        double x2 = r2*Math.cos(t2);
        double y2 = r2*Math.sin(t2);

        x1+=x2;
        y1+=y2;

        return null;



    }

}