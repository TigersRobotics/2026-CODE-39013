package com.qualcomm.hardware.limelightvision;

public class Limelight3A {
    private boolean running;
    private int pipeline;

    public void start() { running = true; }
    public void pause() { running = false; }
    public void stop() { running = false; }
    public boolean isRunning() { return running; }
    public boolean isConnected() { return true; }

    public boolean pipelineSwitch(int index) {
        pipeline = index;
        return true;
    }

    public int getPipeline() { return pipeline; }

    public LLResult getLatestResult() { return null; }
}
