package org.firstinspires.ftc.teamcode.utils;

import static org.firstinspires.ftc.teamcode.constants.Competition.AUDIENCE_CELL_Y_IN;
import static org.firstinspires.ftc.teamcode.constants.Competition.BLUE_HIVE_X_IN;
import static org.firstinspires.ftc.teamcode.constants.Competition.FAR_CELL_Y_IN;
import static org.firstinspires.ftc.teamcode.constants.Competition.RED_HIVE_X_IN;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.BLUE_AUDIENCE_CELL_PIPELINE;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.BLUE_FAR_CELL_PIPELINE;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.RED_AUDIENCE_CELL_PIPELINE;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.RED_FAR_CELL_PIPELINE;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.SHOT_DISTANCES_IN;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.SHOT_FLYWHEEL_VELOCITIES;
import static org.firstinspires.ftc.teamcode.constants.TuningConstants.SHOT_HOOD_ANGLES;

public class AutoAim {
    public static double targetX(boolean red) {
        return red ? RED_HIVE_X_IN : BLUE_HIVE_X_IN;
    }

    public static double targetY(boolean farUp) {
        return farUp ? FAR_CELL_Y_IN : AUDIENCE_CELL_Y_IN;
    }

    public static double distance(double x, double y, boolean red, boolean farUp) {
        return Math.hypot(targetX(red) - x, targetY(farUp) - y);
    }

    public static double turretYaw(double x, double y, double headingDeg, boolean red, boolean farUp) {
        double angle = Math.toDegrees(Math.atan2(targetY(farUp) - y, targetX(red) - x)) - headingDeg;
        return -((angle % 360 + 540) % 360 - 180);
    }

    public static double flywheelVelocity(double distance) {
        return UtilMethods.interpolate(SHOT_DISTANCES_IN, SHOT_FLYWHEEL_VELOCITIES, distance);
    }

    public static double hoodAngle(double distance) {
        return UtilMethods.interpolate(SHOT_DISTANCES_IN, SHOT_HOOD_ANGLES, distance);
    }

    public static int limelightPipeline(boolean red, boolean farUp) {
        if (red) return farUp ? RED_FAR_CELL_PIPELINE : RED_AUDIENCE_CELL_PIPELINE;
        return farUp ? BLUE_FAR_CELL_PIPELINE : BLUE_AUDIENCE_CELL_PIPELINE;
    }
}
