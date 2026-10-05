package sim;

import com.qualcomm.robotcore.hardware.Servo;

/** A fake servo, clamps to 0-1 like the real SDK */
public class SimServo implements Servo {
    private double position;
    private double lastRequested;

    @Override public void setPosition(double position) {
        lastRequested = position;
        this.position = Math.max(0, Math.min(1, position));
    }
    @Override public double getPosition() { return position; }

    /** What the code asked for before clamping, so the sim can flag out of range values */
    public double getLastRequested() { return lastRequested; }
}
