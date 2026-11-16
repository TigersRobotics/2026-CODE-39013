package org.firstinspires.ftc.robotcore.external.navigation;

public class Pose2D {
    private final double xInches, yInches, headingRadians;

    public Pose2D(DistanceUnit distanceUnit, double x, double y, AngleUnit headingUnit, double heading) {
        xInches = distanceUnit.toInches(x);
        yInches = distanceUnit.toInches(y);
        headingRadians = headingUnit.toRadians(heading);
    }

    public double getX(DistanceUnit unit) { return unit.fromInches(xInches); }
    public double getY(DistanceUnit unit) { return unit.fromInches(yInches); }
    public double getHeading(AngleUnit unit) { return unit.fromRadians(headingRadians); }
}
