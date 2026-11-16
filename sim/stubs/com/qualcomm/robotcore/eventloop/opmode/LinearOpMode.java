package com.qualcomm.robotcore.eventloop.opmode;

public abstract class LinearOpMode extends OpMode {
    private volatile boolean started;
    private volatile boolean stopRequested;

    public abstract void runOpMode() throws InterruptedException;

    @Override public final void init() {}
    @Override public final void loop() {}
    @Override public final void start() { started = true; }
    @Override public final void stop() { stopRequested = true; }

    public final void requestOpModeStop() { stopRequested = true; }

    public void waitForStart() {
        while (!started && !isStopRequested()) sleep(1);
    }

    public final boolean isStarted() { return started; }

    public final boolean isStopRequested() {
        return stopRequested || Thread.currentThread().isInterrupted();
    }

    public final boolean opModeInInit() { return !started && !isStopRequested(); }

    public final boolean opModeIsActive() {
        boolean active = started && !isStopRequested();
        if (active) idle();
        return active;
    }

    public final void idle() { Thread.yield(); }

    public final void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
