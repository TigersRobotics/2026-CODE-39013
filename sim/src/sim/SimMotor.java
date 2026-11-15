package sim;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;

public class SimMotor implements DcMotorEx {
    public static final double MAX_TICKS_PER_SECOND = 2800;
    public static final double SPIN_UP_TIME = 0.3;

    private Direction direction = Direction.FORWARD;
    private RunMode mode = RunMode.RUN_WITHOUT_ENCODER;
    private ZeroPowerBehavior zeroPowerBehavior = ZeroPowerBehavior.BRAKE;
    private PIDFCoefficients velocityPidf = new PIDFCoefficients(10, 3, 0, 0);
    private double power;
    private double targetVelocity;
    private boolean velocityControl;
    private double velocity;

    @Override public void setDirection(Direction direction) { this.direction = direction; }
    @Override public Direction getDirection() { return direction; }
    @Override public void setMode(RunMode mode) { this.mode = mode; }
    @Override public RunMode getMode() { return mode; }
    @Override public void setZeroPowerBehavior(ZeroPowerBehavior behavior) { zeroPowerBehavior = behavior; }
    @Override public ZeroPowerBehavior getZeroPowerBehavior() { return zeroPowerBehavior; }

    @Override public void setPower(double power) {
        this.power = Math.max(-1, Math.min(1, power));
        velocityControl = false;
    }

    @Override public double getPower() { return power; }

    @Override public void setVelocity(double angularRate) {
        targetVelocity = Math.max(-MAX_TICKS_PER_SECOND, Math.min(MAX_TICKS_PER_SECOND, angularRate));
        power = targetVelocity / MAX_TICKS_PER_SECOND;
        velocityControl = true;
    }

    @Override public double getVelocity() { return velocity; }

    @Override public void setPIDFCoefficients(RunMode mode, PIDFCoefficients pidf) {
        if (mode == RunMode.RUN_USING_ENCODER) velocityPidf = new PIDFCoefficients(pidf);
    }

    @Override public PIDFCoefficients getPIDFCoefficients(RunMode mode) {
        return new PIDFCoefficients(velocityPidf);
    }

    public double getTargetVelocity() { return velocityControl ? targetVelocity : 0; }

    public void step(double dt) {
        double target = power * MAX_TICKS_PER_SECOND;
        if (velocityControl && mode == RunMode.RUN_USING_ENCODER) {
            target = velocityPidf.p == 0 && velocityPidf.f == 0 ? 0 : targetVelocity;
        }
        velocity += (target - velocity) * Math.min(1, dt / SPIN_UP_TIME);
    }

    public double shaftPower() {
        return direction == Direction.REVERSE ? -power : power;
    }
}
