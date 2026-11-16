package org.firstinspires.ftc.teamcode.constants;

public class TuningConstants {
    public static final double[] DRIVE_SPEED_LEVELS = {0.25, 0.5, 0.75, 1.0};
    public static final double TRIGGER_THRESHOLD = 0.2;

    public static final double INTAKE_POWER = 1.0;
    public static final double INTAKE_REVERSE_POWER = -1.0;
    public static final double UPTAKE_POWER = 1.0;

    public static final double FLYWHEEL_F_VALUE = 32767.0 / RobotSpecs.FLYWHEEL_MAX_TICKS_PER_SECOND;
    public static final double FLYWHEEL_P_VALUE = FLYWHEEL_F_VALUE / 10;
    public static final double FLYWHEEL_TARGET_VELOCITY = 1500;
    public static final double FLYWHEEL_VELOCITY_TOLERANCE = 50;
    public static final double FLYWHEEL_HIGH_VELOCITY = 1500;
    public static final double FLYWHEEL_LOW_VELOCITY = 900;

    public static final double HOOD_STEP_ANGLE = 5;

    public static final int AUTO_SHOTS_PER_VOLLEY = 4;
    public static final double AUTO_SHOOT_TIMEOUT_MS = 3000;
    public static final double AUTO_PICKUP_MS = 1000;
    public static final double AUTO_PATH_TIMEOUT_MS = 6500;
    public static final double AUTO_PARK_BY_MS = 26500;

    public static final double[] SHOT_DISTANCES_IN = {30, 40, 50, 60, 70, 80, 90};
    public static final double[] SHOT_FLYWHEEL_VELOCITIES = {988, 1004, 1037, 1075, 1115, 1156, 1198};
    public static final double[] SHOT_HOOD_ANGLES = {75, 70.6, 65.9, 61.9, 59, 56.6, 54.7};

    public static final int LIMELIGHT_PIPELINE = 0;
    public static final int RED_AUDIENCE_CELL_PIPELINE = 0;
    public static final int RED_FAR_CELL_PIPELINE = 1;
    public static final int BLUE_AUDIENCE_CELL_PIPELINE = 2;
    public static final int BLUE_FAR_CELL_PIPELINE = 3;
    public static final double LIMELIGHT_TILT_STEP = 0.05;
    public static final double TURRET_AIM_GAIN = 0.2;
    public static final double TURRET_AIM_DEADBAND = 1.0;

    public static final double FORESIGHT_FORWARD_TRANSLATIONAL_PRIMARY_KP = 0.15;
    public static final double FORESIGHT_FORWARD_TRANSLATIONAL_SECONDARY_KP = 0.05;
    public static final double FORESIGHT_STRAFE_TRANSLATIONAL_PRIMARY_KP = 0.15;
    public static final double FORESIGHT_STRAFE_TRANSLATIONAL_SECONDARY_KP = 0.05;
    public static final double FORESIGHT_TRANSLATIONAL_SWITCH_DISTANCE = 2.5;
    public static final double FORESIGHT_COAST_KV = 0.018;
    public static final double FORESIGHT_BRAKE_KV = 0.018;
    public static final double FORESIGHT_HEADING_KP = 1.0;
    public static final double FORESIGHT_HEADING_BRAKE_LINEAR = 0.1;
    public static final double FORESIGHT_HEADING_BRAKE_QUADRATIC = 0.02;
    public static final double FORESIGHT_FORWARD_BRAKE_LINEAR = 0.05;
    public static final double FORESIGHT_FORWARD_BRAKE_QUADRATIC = 0.003;
    public static final double FORESIGHT_STRAFE_BRAKE_LINEAR = 0.06;
    public static final double FORESIGHT_STRAFE_BRAKE_QUADRATIC = 0.004;
    public static final double FORESIGHT_MAX_FORWARD_VELOCITY = 55;
    public static final double FORESIGHT_MAX_STRAFE_VELOCITY = 45;
    public static final double FORESIGHT_FORWARD_DECELERATION = 40;
    public static final double FORESIGHT_STRAFE_DECELERATION = 60;
}
