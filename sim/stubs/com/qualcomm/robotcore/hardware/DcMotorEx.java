package com.qualcomm.robotcore.hardware;

public interface DcMotorEx extends DcMotor {
    void setVelocity(double angularRate);
    double getVelocity();
    void setPIDFCoefficients(RunMode mode, PIDFCoefficients pidfCoefficients);
    PIDFCoefficients getPIDFCoefficients(RunMode mode);
}
