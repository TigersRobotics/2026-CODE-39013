package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.robotControllers.UptakeServoController;
import org.firstinspires.ftc.teamcode.utils.OpModeUtils;

@TeleOp(name = "Kicker Test")
public class UptakeServoTest extends OpMode {
    @Override
    public void init() {
        OpModeUtils.initSome(telemetry);
        UptakeServoController.init(hardwareMap);
        OpModeUtils.updateSome();
    }

    @Override
    public void loop() {
        if (gamepad1.crossWasPressed()) UptakeServoController.shoot();
        UptakeServoController.update();
        OpModeUtils.updateSome();
        telemetry.update();
    }

    @Override
    public void stop() {
        OpModeUtils.stopAll();
    }
}
