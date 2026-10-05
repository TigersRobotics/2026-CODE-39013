package sim;

import com.qualcomm.robotcore.hardware.DcMotorEx;

/** A fake motor that remembers what the robot code told it */
public class SimMotor implements DcMotorEx {
    private static final double MAX_TICKS_PER_SECOND = 2800;

    private Direction direction = Direction.FORWARD;
    private RunMode mode = RunMode.RUN_WITHOUT_ENCODER;
    private double power;

    @Override public void setDirection(Direction direction) { this.direction = direction; }
    @Override public Direction getDirection() { return direction; }
    @Override public void setMode(RunMode mode) { this.mode = mode; }
    @Override public void setPower(double power) { this.power = Math.max(-1, Math.min(1, power)); }
    @Override public double getPower() { return power; }
    @Override public double getVelocity() { return power * MAX_TICKS_PER_SECOND; }

    /** Power after the direction setting, which is what the motor shaft actually does */
    public double shaftPower() {
        return direction == Direction.REVERSE ? -power : power;
    }
}
