package com.qualcomm.robotcore.hardware;

public class Gamepad {
    private static final float TRIGGER_THRESHOLD = 0.5f;

    public float left_stick_x, left_stick_y, right_stick_x, right_stick_y;
    public float left_trigger, right_trigger;
    public boolean left_bumper, right_bumper;
    public boolean a, b, x, y;
    public boolean dpad_up, dpad_down, dpad_left, dpad_right;
    public boolean back, start, guide, left_stick_button, right_stick_button, touchpad;

    public boolean cross, circle, square, triangle, share, options, ps;

    private enum Button {
        LEFT_BUMPER, RIGHT_BUMPER, DPAD_UP, DPAD_DOWN, DPAD_LEFT, DPAD_RIGHT,
        CROSS, CIRCLE, SQUARE, TRIANGLE, LEFT_TRIGGER, RIGHT_TRIGGER
    }

    private final boolean[] last = new boolean[Button.values().length];
    private final boolean[] pressed = new boolean[Button.values().length];

    public void latchPresses() {
        cross = a;
        circle = b;
        square = x;
        triangle = y;
        share = back;
        options = start;
        ps = guide;

        latch(Button.LEFT_BUMPER, left_bumper);
        latch(Button.RIGHT_BUMPER, right_bumper);
        latch(Button.DPAD_UP, dpad_up);
        latch(Button.DPAD_DOWN, dpad_down);
        latch(Button.DPAD_LEFT, dpad_left);
        latch(Button.DPAD_RIGHT, dpad_right);
        latch(Button.CROSS, a);
        latch(Button.CIRCLE, b);
        latch(Button.SQUARE, x);
        latch(Button.TRIANGLE, y);
        latch(Button.LEFT_TRIGGER, left_trigger > TRIGGER_THRESHOLD);
        latch(Button.RIGHT_TRIGGER, right_trigger > TRIGGER_THRESHOLD);
    }

    private void latch(Button button, boolean down) {
        int i = button.ordinal();
        if (down && !last[i]) pressed[i] = true;
        last[i] = down;
    }

    private boolean consume(Button button) {
        int i = button.ordinal();
        boolean p = pressed[i];
        pressed[i] = false;
        return p;
    }

    public boolean leftBumperWasPressed() { return consume(Button.LEFT_BUMPER); }
    public boolean rightBumperWasPressed() { return consume(Button.RIGHT_BUMPER); }
    public boolean dpadUpWasPressed() { return consume(Button.DPAD_UP); }
    public boolean dpadDownWasPressed() { return consume(Button.DPAD_DOWN); }
    public boolean dpadLeftWasPressed() { return consume(Button.DPAD_LEFT); }
    public boolean dpadRightWasPressed() { return consume(Button.DPAD_RIGHT); }
    public boolean crossWasPressed() { return consume(Button.CROSS); }
    public boolean circleWasPressed() { return consume(Button.CIRCLE); }
    public boolean squareWasPressed() { return consume(Button.SQUARE); }
    public boolean triangleWasPressed() { return consume(Button.TRIANGLE); }
    public boolean aWasPressed() { return consume(Button.CROSS); }
    public boolean bWasPressed() { return consume(Button.CIRCLE); }
    public boolean xWasPressed() { return consume(Button.SQUARE); }
    public boolean yWasPressed() { return consume(Button.TRIANGLE); }
    public boolean leftTriggerWasPressed() { return consume(Button.LEFT_TRIGGER); }
    public boolean rightTriggerWasPressed() { return consume(Button.RIGHT_TRIGGER); }
}
