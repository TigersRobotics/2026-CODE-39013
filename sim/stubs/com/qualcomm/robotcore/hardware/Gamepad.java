package com.qualcomm.robotcore.hardware;

// Sim stand-in for the FTC SDK class, only what our code uses
public class Gamepad {
    public float left_stick_x, left_stick_y, right_stick_x, right_stick_y;
    public float left_trigger, right_trigger;
    public boolean left_bumper, right_bumper;
    public boolean a, b, x, y;
    public boolean dpad_up, dpad_down, dpad_left, dpad_right;

    private boolean lastLeftBumper, lastRightBumper, lastDpadUp, lastDpadDown;
    private boolean leftBumperPressed, rightBumperPressed, dpadUpPressed, dpadDownPressed;

    /** The sim calls this after setting the fields each frame, like the SDK does for new gamepad data */
    public void latchPresses() {
        if (left_bumper && !lastLeftBumper) leftBumperPressed = true;
        if (right_bumper && !lastRightBumper) rightBumperPressed = true;
        if (dpad_up && !lastDpadUp) dpadUpPressed = true;
        if (dpad_down && !lastDpadDown) dpadDownPressed = true;
        lastLeftBumper = left_bumper;
        lastRightBumper = right_bumper;
        lastDpadUp = dpad_up;
        lastDpadDown = dpad_down;
    }

    // true once per press, same as the SDK
    public boolean leftBumperWasPressed() { boolean p = leftBumperPressed; leftBumperPressed = false; return p; }
    public boolean rightBumperWasPressed() { boolean p = rightBumperPressed; rightBumperPressed = false; return p; }
    public boolean dpadUpWasPressed() { boolean p = dpadUpPressed; dpadUpPressed = false; return p; }
    public boolean dpadDownWasPressed() { boolean p = dpadDownPressed; dpadDownPressed = false; return p; }
}
