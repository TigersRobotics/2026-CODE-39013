package org.firstinspires.ftc.teamcode;


import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.robotControllers.TurretController;

@TeleOp
public class ControllerTest extends OpMode {
    TurretController turretController;
    final boolean ENABLE_TURRET_CONTROLLER = true;

    @Override
    public void init() {
        turretController = new TurretController(hardwareMap);

    }
    @Override
    public void loop() {
        if(ENABLE_TURRET_CONTROLLER) {
            turretController.setPosition(gamepad1.left_stick_x*Math.PI, true);
        }
    }


}
