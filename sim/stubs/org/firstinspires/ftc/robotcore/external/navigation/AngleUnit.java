package org.firstinspires.ftc.robotcore.external.navigation;

public enum AngleUnit {
    DEGREES, RADIANS;

    public double toRadians(double value) { return this == RADIANS ? value : Math.toRadians(value); }
    public double toDegrees(double value) { return this == DEGREES ? value : Math.toDegrees(value); }
    public double fromRadians(double value) { return this == RADIANS ? value : Math.toDegrees(value); }
    public double fromUnit(AngleUnit unit, double value) { return fromRadians(unit.toRadians(value)); }
}
