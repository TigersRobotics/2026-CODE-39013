package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.robotControllers.UptakeController;
import org.firstinspires.ftc.teamcode.utils.OpModeUtils;

@TeleOp(name = "Uptake Test")
public class UptakeTest extends OpMode {
    @Override
    public void init() {
        OpModeUtils.initSome(telemetry);
        UptakeController.init(hardwareMap);
        OpModeUtils.updateSome();
    }

    @Override
    public void loop() {
        UptakeController.update(gamepad1.right_trigger - gamepad1.left_trigger);
        OpModeUtils.updateSome();
        telemetry.update();
    }

    @Override
    public void stop() {
        OpModeUtils.stopAll();
    }
}
