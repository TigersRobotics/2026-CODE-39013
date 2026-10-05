package com.qualcomm.robotcore.eventloop.opmode;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;

// Sim stand-in for the FTC SDK class, only what our code uses
public abstract class OpMode {
    public HardwareMap hardwareMap;
    public Telemetry telemetry;
    public Gamepad gamepad1;
    public Gamepad gamepad2;

    public abstract void init();
    public abstract void loop();
    public void start() {}
    public void stop() {}
}
