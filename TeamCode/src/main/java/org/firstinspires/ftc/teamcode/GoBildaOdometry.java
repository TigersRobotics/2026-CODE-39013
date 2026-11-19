package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.constants.Competition;
import org.firstinspires.ftc.teamcode.pedro.Constants;

import org.firstinspires.ftc.teamcode.utils.TimedTelemetry;

public class GoBildaOdometry {

    private final GoBildaPinpointDriver odo;
    private static boolean setInitYet = false;
    public GoBildaOdometry(HardwareMap hardwareMap, String deviceName) {
        if (hardwareMap.get(GoBildaPinpointDriver.class, deviceName)==null) TimedTelemetry.addLine("Test Failed", 1000);
        else TimedTelemetry.addLine("Test Passed",1000);

        odo = hardwareMap.get(GoBildaPinpointDriver.class, deviceName);
        odo.setOffsets(Constants.localizerConfig.xPodOffset.get(), Constants.localizerConfig.yPodOffset.get(), Constants.localizerConfig.globalDistanceUnit.get());
        odo.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_SWINGARM_POD);
        odo.setEncoderDirections(
                Constants.localizerConfig.xPodDirection.get(),
                Constants.localizerConfig.yPodDirection.get()
        );

        odo.resetPosAndIMU();
        TimedTelemetry.addLine("setPose reached");
    }
    public void update() {
        odo.update();
        if (odo.getDeviceStatus() == GoBildaPinpointDriver.DeviceStatus.NOT_READY) return;


    }

    public double getVelX(DistanceUnit unit) {
        return odo.getVelX(unit); // per second
    }

    public double getVelY(DistanceUnit unit) {
        return odo.getVelY(unit); // per second
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