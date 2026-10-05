package com.qualcomm.robotcore.hardware;

// Sim stand-in for the FTC SDK class, only what our code uses
public interface DcMotorEx extends DcMotor {
    double getVelocity();
}
