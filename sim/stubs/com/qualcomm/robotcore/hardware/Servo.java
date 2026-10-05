package com.qualcomm.robotcore.hardware;

// Sim stand-in for the FTC SDK class, only what our code uses
public interface Servo {
    void setPosition(double position);
    double getPosition();
}
