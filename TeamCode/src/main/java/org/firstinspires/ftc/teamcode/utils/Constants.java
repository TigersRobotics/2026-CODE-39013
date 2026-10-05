package org.firstinspires.ftc.teamcode.utils;

public final class Constants {
    // A class to store values/measurements of the robot
    // Some (All) values are temp as they are pending measurement
    // Really, we haven't built most of the robot yet

    // Ball speed is currently a temp value;
    public static final double BALL_SPEED = 0;
    // The Camera/Turret offset is a temp value;
    public static final double CAMERA_TURRET_OFFSET= 0;

    // temp values
    public static final double MAX_TURRET_ANGLE_UP = 0;
    public static final double MAX_TURRET_ANGLE_DOWN = 0;

    public static final double APRIL_TAG_HIVE_OFFSET_H = 0;
    public static final double APRIL_TAG_HIVE_OFFSET_D = 0;

    public static final String FRONT_LEFT_MOTOR_NAME = "frontLeft";
    public static final String FRONT_RIGHT_MOTOR_NAME = "frontRight";
    public static final String BACK_LEFT_MOTOR_NAME = "backLeft";
    public static final String BACK_RIGHT_MOTOR_NAME = "backRight";

    /**
     * Scales a number to an exponent, but keeps the sign of the number
     * e.g., expo(2, 2) = 4, expo(-2, 2) = -4
     * usually good for the controller inputs(0-1)
     * @param input
     * @param exponent
     */
    public static double expo(double input, double exponent) {
        return Math.signum(input)*Math.pow(Math.abs(input), exponent);
    }

}
