package org.firstinspires.ftc.robotcore.external.navigation;

public enum UnnormalizedAngleUnit {
    DEGREES, RADIANS;

    public double fromRadians(double value) { return this == RADIANS ? value : Math.toDegrees(value); }
    public double toRadians(double value) { return this == RADIANS ? value : Math.toRadians(value); }
}
