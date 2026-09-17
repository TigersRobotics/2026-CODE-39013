package org.firstinspires.ftc.teamcode.controller;

import com.qualcomm.robotcore.hardware.Gamepad;

public class CustomController {
    private final Gamepad gamepad;

    private Binding.Axis driveX;
    private Binding.Axis driveY;
    private Binding.Axis turnRight;
    private Binding.Axis turnLeft;
    private Binding.Axis boost;
    private Binding.Button toggle4Wheel;

    public CustomController(Gamepad gamepad,
                            Binding.Axis driveX,
                            Binding.Axis driveY,
                            Binding.Axis turnRight,
                            Binding.Axis turnLeft,
                            Binding.Axis boost,
                            Binding.Button toggle4Wheel) {
        this.gamepad = gamepad;
        this.driveX = driveX;
        this.driveY = driveY;
        this.turnRight = turnRight;
        this.turnLeft = turnLeft;
        this.boost = boost;
        this.toggle4Wheel = toggle4Wheel;
    }

    public CustomController(Gamepad gamepad,
                            Binding.Axis driveX,
                            Binding.Axis driveY,
                            Binding.Axis turn,
                            Binding.Axis boost,
                            Binding.Button toggle4Wheel) {
        this.gamepad = gamepad;
        this.driveX = driveX;
        this.driveY = driveY;
        this.turnLeft = turn;
        this.boost = boost;
        this.toggle4Wheel = toggle4Wheel;
    }

    public double getDriveX() {
        return driveX.applyAsDouble(gamepad);
    }
    public double getDriveY() {
        return driveY.applyAsDouble(gamepad);
    }
    public double getTurn() {
        return - turnRight.applyAsDouble(gamepad) + turnLeft.applyAsDouble(gamepad);
    }

    public double getBoost() {
        return boost.applyAsDouble(gamepad);
    }
    public boolean getToggle4Wheel() {
        return toggle4Wheel.test(gamepad);
    }
}
