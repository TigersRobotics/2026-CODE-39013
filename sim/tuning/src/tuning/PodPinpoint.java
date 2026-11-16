package tuning;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.robotcore.external.navigation.UnnormalizedAngleUnit;

public class PodPinpoint extends GoBildaPinpointDriver {
    public static final double SWINGARM_TICKS_PER_MM = 13.26291192;
    public static final double FOUR_BAR_TICKS_PER_MM = 19.89436789;
    public static final long READ_TIME_MS = 2;

    private final TuningRobot robot;
    private double xOffset, yOffset;
    private double ticksPerMm = FOUR_BAR_TICKS_PER_MM;
    private int xSign = 1, ySign = 1;
    private double x, y, heading, velX, velY, velHeading;
    private double[] read = new double[6];

    public PodPinpoint(TuningRobot robot) {
        this.robot = robot;
    }

    void track(double dt) {
        double turn = -robot.turnVel;
        double forwardPod = robot.forwardVel - turn * TuningRobot.X_POD_OFFSET;
        double strafePod = -robot.strafeVel + turn * TuningRobot.Y_POD_OFFSET;
        double scale = TuningRobot.POD_TICKS_PER_MM / ticksPerMm;

        double forward = forwardPod * scale * xSign * (TuningRobot.X_POD_REVERSED ? -1 : 1) + turn * xOffset;
        double left = strafePod * scale * ySign * (TuningRobot.Y_POD_REVERSED ? -1 : 1) - turn * yOffset;

        velX = forward * Math.cos(heading) - left * Math.sin(heading);
        velY = forward * Math.sin(heading) + left * Math.cos(heading);
        velHeading = turn;
        x += velX * dt;
        y += velY * dt;
        heading += turn * dt;
    }

    @Override
    public void update() {
        try {
            Thread.sleep(READ_TIME_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        synchronized (robot) {
            read = new double[]{x, y, Math.atan2(Math.sin(heading), Math.cos(heading)), velX, velY, velHeading};
        }
    }

    @Override public double getPosX(DistanceUnit unit) { return unit.fromInches(read[0]); }
    @Override public double getPosY(DistanceUnit unit) { return unit.fromInches(read[1]); }
    @Override public double getHeading(AngleUnit unit) { return unit.fromRadians(read[2]); }
    @Override public double getVelX(DistanceUnit unit) { return unit.fromInches(read[3]); }
    @Override public double getVelY(DistanceUnit unit) { return unit.fromInches(read[4]); }
    @Override public double getHeadingVelocity(UnnormalizedAngleUnit unit) { return unit.fromRadians(read[5]); }

    @Override
    public Pose2D getPosition() {
        return new Pose2D(DistanceUnit.INCH, read[0], read[1], AngleUnit.RADIANS, read[2]);
    }

    @Override
    public void setPosition(Pose2D pos) {
        synchronized (robot) {
            x = pos.getX(DistanceUnit.INCH);
            y = pos.getY(DistanceUnit.INCH);
            heading = pos.getHeading(AngleUnit.RADIANS);
        }
    }

    @Override
    public void resetPosAndIMU() {
        setPosition(new Pose2D(DistanceUnit.INCH, 0, 0, AngleUnit.RADIANS, 0));
    }

    @Override
    public void setOffsets(double xOffset, double yOffset, DistanceUnit unit) {
        this.xOffset = unit.toInches(xOffset);
        this.yOffset = unit.toInches(yOffset);
    }

    @Override
    public void setEncoderResolution(GoBildaOdometryPods pods) {
        ticksPerMm = pods == GoBildaOdometryPods.goBILDA_SWINGARM_POD ? SWINGARM_TICKS_PER_MM : FOUR_BAR_TICKS_PER_MM;
    }

    @Override
    public void setEncoderResolution(double ticksPerUnit, DistanceUnit unit) {
        ticksPerMm = ticksPerUnit / (unit.toInches(1) * 25.4);
    }

    @Override
    public void setEncoderDirections(EncoderDirection xEncoder, EncoderDirection yEncoder) {
        xSign = xEncoder == EncoderDirection.FORWARD ? 1 : -1;
        ySign = yEncoder == EncoderDirection.FORWARD ? 1 : -1;
    }

    @Override
    public DeviceStatus getDeviceStatus() { return DeviceStatus.READY; }
}
