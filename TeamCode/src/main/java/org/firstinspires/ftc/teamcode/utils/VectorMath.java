package org.firstinspires.ftc.teamcode.utils;

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
    

    public static double[] shotAngle(double d, double h, double r, double camAngle, Telemetry telemetry) {
/// returns turret vertical angle, and the horizonal angle. Input angleFromTag: facing right is pos
/// all angles are in degrees, lengths are in meters, speeds are in m/s
        double [] nV = addV(new double[]{d, camAngle}, new double[]{Constants.APRIL_TAG_HIVE_OFFSET_D, 180-r});
        double y = h + Constants.APRIL_TAG_HIVE_OFFSET_H;
        double x = nV[0];

        // it will try to calculate the angle for trajectory
        double angle = angleCalc((byte) -1, x, y);
        double nAngle;

        // checks if the angle is valid
        if (angle >= Constants.MAX_TURRET_ANGLE_UP ||
                angle <= Constants.MAX_TURRET_ANGLE_DOWN || Double.isNaN(angle)){

            // invalid initial angle will return a second angle
            nAngle = angleCalc((byte) 1, x, y);
            if (nAngle >= Constants.MAX_TURRET_ANGLE_UP ||
                    nAngle <= Constants.MAX_TURRET_ANGLE_DOWN || Double.isNaN(nAngle)){

                // will log that it cannot find valid angle, shows init angle and backup angle
                telemetry.addLine(("No valid angle found: " + angle + " " + nAngle));
                return new double[] {Double.NaN};
            }
            return new double[] {nAngle, nV[1]};
        }

        return new double[] {angle, nV[1]};
    }

    private static double angleCalc(byte add, double x, double y) {
        double g = 9.800665;

        double v = Constants.BALL_SPEED;
        double v2 = Math.pow(v, 2);
        double v4 = Math.pow(v, 4);

        double x2 = Math.pow(x, 2);
        double discriminant = v4 - g * (g * x2 + 2 * y * v2);
        if (discriminant < 0) {
            return Double.NaN; // Target is physically out of reach
        }

        double root = add * Math.sqrt(discriminant);
        double arg = (v2 + root) / (g * x);

        return Math.atan(arg)/Math.PI*180;
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

        r1 = Math.sqrt((Math.pow(x1, 2) + Math.pow(y1, 2)));
        t1 = Math.atan2(y1, x1)*180/Math.PI;

        return new double[] {r1,t1};

    }

}