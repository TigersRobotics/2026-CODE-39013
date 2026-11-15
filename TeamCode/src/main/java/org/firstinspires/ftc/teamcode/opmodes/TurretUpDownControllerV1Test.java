package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.robotControllers.TurretUpAngleController;
import org.firstinspires.ftc.teamcode.utils.OpModeUtils;


@TeleOp (name = "Hood Test")
public class TurretUpDownControllerV1Test extends OpMode {
    @Override
    public void init() {
        OpModeUtils.initSome(telemetry);
        TurretUpAngleController.init(hardwareMap);
        OpModeUtils.updateSome();
    }

    @Override
    public void loop() {
        boolean up = gamepad1.dpadUpWasPressed();
        boolean down = gamepad1.dpadDownWasPressed();
        TurretUpAngleController.update(up ? 1 : down ? -1 : 0);
        OpModeUtils.updateSome();
        telemetry.update();
    }

    @Override
    public void stop() {
        OpModeUtils.stopAll();
    }

}
