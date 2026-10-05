package com.qualcomm.robotcore.hardware;

// Sim stand-in for the FTC SDK class, only what our code uses
public interface DcMotor {
    enum Direction { FORWARD, REVERSE }
    enum RunMode { RUN_WITHOUT_ENCODER, RUN_USING_ENCODER, RUN_TO_POSITION, STOP_AND_RESET_ENCODER }

    void setDirection(Direction direction);
    Direction getDirection();
    void setPower(double power);
    double getPower();
    void setMode(RunMode mode);
}
