package com.qualcomm.hardware.limelightvision;

public class LLResult {
    private final boolean valid;
    private final double tx, ty, ta;

    public LLResult(boolean valid, double tx, double ty, double ta) {
        this.valid = valid;
        this.tx = tx;
        this.ty = ty;
        this.ta = ta;
    }

    public boolean isValid() { return valid; }
    public double getTx() { return tx; }
    public double getTy() { return ty; }
    public double getTa() { return ta; }
}
