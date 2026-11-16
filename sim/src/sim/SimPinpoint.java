package sim;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

public class SimPinpoint extends GoBildaPinpointDriver {
    public static final double PEDRO_OFFSET = Field.HALF;

    private final SimRobot robot;
    private double x, y, heading;
    private double velX, velY;
    private double lastX, lastY, lastHeading;

    public SimPinpoint(SimRobot robot) {
        this.robot = robot;
        rememberTruth();
    }

    public double trueX() { return robot.x + PEDRO_OFFSET; }
    public double trueY() { return robot.y + PEDRO_OFFSET; }
    public double trueHeading() { return Math.PI / 2 - robot.heading; }

    private void rememberTruth() {
        lastX = trueX();
        lastY = trueY();
        lastHeading = trueHeading();
    }

    @Override
    public void resetPosAndIMU() {
        x = 0;
        y = 0;
        heading = 0;
        rememberTruth();
    }

    @Override
    public void update() {
        double dx = trueX() - lastX, dy = trueY() - lastY;
        double forward = dx * Math.cos(lastHeading) + dy * Math.sin(lastHeading);
        double left = -dx * Math.sin(lastHeading) + dy * Math.cos(lastHeading);
        x += forward * Math.cos(heading) - left * Math.sin(heading);
        y += forward * Math.sin(heading) + left * Math.cos(heading);
        heading += trueHeading() - lastHeading;
        rememberTruth();

        double sin = Math.sin(robot.heading), cos = Math.cos(robot.heading);
        double trueVelX = robot.forwardVel * sin + robot.strafeVel * cos;
        double trueVelY = robot.forwardVel * cos - robot.strafeVel * sin;
        double offset = heading - trueHeading();
        velX = trueVelX * Math.cos(offset) - trueVelY * Math.sin(offset);
        velY = trueVelX * Math.sin(offset) + trueVelY * Math.cos(offset);
    }

    @Override
    public Pose2D getPosition() {
        return new Pose2D(DistanceUnit.INCH, x, y, AngleUnit.RADIANS, Math.atan2(Math.sin(heading), Math.cos(heading)));
    }

    @Override
    public void setPosition(Pose2D pos) {
        x = pos.getX(DistanceUnit.INCH);
        y = pos.getY(DistanceUnit.INCH);
        heading = pos.getHeading(AngleUnit.RADIANS);
        rememberTruth();
    }

    @Override
    public double getVelX(DistanceUnit unit) { return unit.fromInches(velX); }

    @Override
    public double getVelY(DistanceUnit unit) { return unit.fromInches(velY); }

    @Override
    public DeviceStatus getDeviceStatus() { return DeviceStatus.READY; }
}
