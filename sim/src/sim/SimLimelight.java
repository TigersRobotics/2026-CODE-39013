package sim;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;

import org.firstinspires.ftc.teamcode.constants.RobotSpecs;

public class SimLimelight extends Limelight3A {
    public static final double HORIZONTAL_FOV = 54.5;
    public static final double TARGET_HEIGHT_ABOVE_CAMERA = 20;
    public static final double MAX_RANGE = 150;

    private final SimRobot robot;

    public SimLimelight(SimRobot robot) {
        this.robot = robot;
    }

    public double cameraHeading() {
        return robot.heading + (RobotSpecs.LIMELIGHT_ON_TURRET ? robot.turretAngle() : 0);
    }

    public LLResult view() {
        double dx = robot.targetX - robot.x, dy = robot.targetY - robot.y;
        double distance = Math.hypot(dx, dy);
        double bearing = Math.atan2(dx, dy) - cameraHeading();
        double tx = Math.toDegrees(Math.atan2(Math.sin(bearing), Math.cos(bearing)));
        double tilt = (robot.limelightTilt.shaftPosition() - 0.5) * 180;
        double ty = Math.toDegrees(Math.atan2(TARGET_HEIGHT_ABOVE_CAMERA, distance)) - tilt;
        boolean valid = isRunning() && distance < MAX_RANGE && Math.abs(tx) <= HORIZONTAL_FOV / 2;
        return new LLResult(valid, valid ? tx : 0, valid ? ty : 0, valid ? 100 / (1 + distance) : 0);
    }

    @Override
    public LLResult getLatestResult() {
        return view();
    }
}
