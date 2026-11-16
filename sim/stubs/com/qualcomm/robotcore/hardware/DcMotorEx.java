package com.qualcomm.robotcore.hardware;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

public interface DcMotorEx extends DcMotor {
    void setVelocity(double angularRate);
    double getVelocity();
    void setPIDFCoefficients(RunMode mode, PIDFCoefficients pidfCoefficients);
    PIDFCoefficients getPIDFCoefficients(RunMode mode);

    default double getCurrent(CurrentUnit unit) { return 0; }
}
