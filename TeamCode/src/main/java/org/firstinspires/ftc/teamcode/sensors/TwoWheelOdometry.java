package org.firstinspires.ftc.teamcode.sensors;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.utils.Constants;

public class TwoWheelOdometry {

    private final GoBildaPinpointDriver odo;


    public TwoWheelOdometry(HardwareMap hardwareMap, String deviceName) {
        odo = hardwareMap.get(GoBildaPinpointDriver.class, deviceName);

        odo.setOffsets(Constants.OFFSETS_OF_ODOMETRY_PODS_MM_X, Constants.OFFSETS_OF_ODOMETRY_PODS_MM_Y, DistanceUnit.MM);
        odo.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_SWINGARM_POD);


        odo.setEncoderDirections(
                GoBildaPinpointDriver.EncoderDirection.FORWARD,
                GoBildaPinpointDriver.EncoderDirection.FORWARD
        );


        odo.resetPosAndIMU();
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