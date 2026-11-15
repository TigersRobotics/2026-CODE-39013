package org.firstinspires.ftc.teamcode.constants;

public class TuningConstants {
    public static final double[] DRIVE_SPEED_LEVELS = {0.25, 0.5, 0.75, 1.0};

    public static float INTAKE_SPEED = 4;
    public static float UPTAKE_SPEED = 6;
    public static float SHOOTER_SPEED = 12;

    public static final double FLYWHEEL_F_VALUE = 32767.0 / RobotSpecs.FLYWHEEL_MAX_TICKS_PER_SECOND;
    public static final double FLYWHEEL_P_VALUE = FLYWHEEL_F_VALUE / 10;
    public static final double FLYWHEEL_TARGET_VELOCITY = 1500;
    public static final double FLYWHEEL_VELOCITY_TOLERANCE = 50;
    public static final double FLYWHEEL_HIGH_VELOCITY = 1500;
    public static final double FLYWHEEL_LOW_VELOCITY = 900;

    public static final double HOOD_STEP_ANGLE = 5;
}
