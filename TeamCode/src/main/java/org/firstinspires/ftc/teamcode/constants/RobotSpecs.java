package org.firstinspires.ftc.teamcode.constants;

public class RobotSpecs {
    public static final double[] CAMERA_TURRET_OFFSET = new double[]{
            0, 0
    };

    public static final double MAX_TURRET_ANGLE_UP = 75;
    public static final double MAX_TURRET_ANGLE_DOWN = 15;
    public static final double OFFSETS_OF_ODOMETRY_PODS_IN_X = 0.0;
    public static final double OFFSETS_OF_ODOMETRY_PODS_IN_Y = 0.0;
    public static final double BALL_SPEED = 10;

    public static final double FLYWHEEL_MAX_TICKS_PER_SECOND = 2800;

    public static final double UPTAKE_SERVO_UP_ANGLE = 0.5;
    public static final double UPTAKE_SERVO_DEFAULT_ANGLE = 0;
    public static final double UPTAKE_SERVO_KICK_MS = 250;
    public static final double UPTAKE_SERVO_RETRACT_MS = 250;

    public static final double HOOD_SERVO_MAX_ANGLE = 180;
    public static final double HOOD_MIN_ANGLE = 0;
    public static final double HOOD_MAX_ANGLE = 90;
    public static final double HOOD_DEFAULT_ANGLE = 45;

    public static final double TURRET_SERVO_RANGE_DEGREES = 300;
    public static final double TURRET_GEAR_RATIO = 48.0 / 10.0;
    public static final double MAX_YAW = TURRET_SERVO_RANGE_DEGREES / TURRET_GEAR_RATIO / 2;
    public static final double MIN_YAW = -MAX_YAW;
    public static final boolean TURRET_SERVOS_MIRRORED = false;

    public static final double LIMELIGHT_TILT_DEFAULT_POSITION = 0.5;
    public static final boolean LIMELIGHT_ON_TURRET = true;
}
