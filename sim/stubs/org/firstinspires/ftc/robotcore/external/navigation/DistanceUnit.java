package org.firstinspires.ftc.robotcore.external.navigation;

public enum DistanceUnit {
    METER(39.37007874015748), CM(0.3937007874015748), MM(0.03937007874015748), INCH(1);

    private final double inches;

    DistanceUnit(double inches) {
        this.inches = inches;
    }

    public double toInches(double value) { return value * inches; }
    public double fromInches(double value) { return value / inches; }
    public double fromUnit(DistanceUnit unit, double value) { return fromInches(unit.toInches(value)); }
}
