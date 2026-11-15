package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.robotControllers.SingleIntakeController;
import org.firstinspires.ftc.teamcode.utils.OpModeUtils;

@TeleOp(name = "Intake Test")
public class IntakeTest extends OpMode {
    @Override
    public void init() {
        OpModeUtils.initSome(telemetry);
        SingleIntakeController.init(hardwareMap);
        OpModeUtils.updateSome();
    }

    @Override
    public void loop() {
        SingleIntakeController.update(gamepad1.left_trigger - gamepad1.right_trigger);
        OpModeUtils.updateSome();
        telemetry.update();
    }

    @Override
    public void stop() {
        OpModeUtils.stopAll();
    }
}
