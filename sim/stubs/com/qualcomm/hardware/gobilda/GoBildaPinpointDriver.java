package com.qualcomm.hardware.gobilda;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

public class GoBildaPinpointDriver {
    public enum DeviceStatus {
        NOT_READY, READY, CALIBRATING, FAULT_X_POD_NOT_DETECTED, FAULT_Y_POD_NOT_DETECTED,
        FAULT_NO_PODS_DETECTED, FAULT_IMU_RUNAWAY, FAULT_BAD_READ
    }
    public enum EncoderDirection { FORWARD, REVERSED }
    public enum GoBildaOdometryPods { goBILDA_SWINGARM_POD, goBILDA_4_BAR_POD }

    public void setOffsets(double xOffset, double yOffset, DistanceUnit unit) {}
    public void setEncoderResolution(GoBildaOdometryPods pods) {}
    public void setEncoderDirections(EncoderDirection xEncoder, EncoderDirection yEncoder) {}
    public void resetPosAndIMU() {}
    public void update() {}
    public Pose2D getPosition() { return null; }
    public void setPosition(Pose2D pos) {}
    public double getVelX(DistanceUnit unit) { return 0; }
    public double getVelY(DistanceUnit unit) { return 0; }
    public DeviceStatus getDeviceStatus() { return DeviceStatus.NOT_READY; }
}
