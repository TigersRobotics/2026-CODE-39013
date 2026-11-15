package org.firstinspires.ftc.teamcode.sensors;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.constants.RobotSpecs;
import org.firstinspires.ftc.teamcode.utils.TimedTelemetry;

public class GoBildaOdometry {

    private final GoBildaPinpointDriver odo;
    public GoBildaOdometry(HardwareMap hardwareMap, String deviceName) {
        odo = hardwareMap.get(GoBildaPinpointDriver.class, deviceName);
        odo.setOffsets(RobotSpecs.OFFSETS_OF_ODOMETRY_PODS_IN_X, RobotSpecs.OFFSETS_OF_ODOMETRY_PODS_IN_Y, DistanceUnit.INCH);
        odo.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_SWINGARM_POD);
        odo.setEncoderDirections(
                GoBildaPinpointDriver.EncoderDirection.FORWARD,
                GoBildaPinpointDriver.EncoderDirection.FORWARD
        );

        odo.resetPosAndIMU();
        TimedTelemetry.addLine("Odometry Initialized");
    }
    public void update() {
        odo.update();
    }

    public Pose2D getPose() {
        return odo.getPosition();
    }
    public double getX(DistanceUnit unit) {
        return odo.getPosition().getX(unit);
    }
    public double getY(DistanceUnit unit) {
        return odo.getPosition().getY(unit);
    }
    public double getHeading(AngleUnit unit) {
        return odo.getPosition().getHeading(unit);
    }
    public void reset() {
        odo.resetPosAndIMU();
    }

    public void setPose(Pose2D newPose) {
        odo.setPosition(newPose);
    }

    public GoBildaPinpointDriver.DeviceStatus getStatus() {
        return odo.getDeviceStatus();
    }
}
