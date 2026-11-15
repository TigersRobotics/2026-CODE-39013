package sim;

import com.qualcomm.robotcore.hardware.Servo;

public class SimServo implements Servo {
    private Direction direction = Direction.FORWARD;
    private double position;
    private double lastRequested;

    @Override public void setDirection(Direction direction) { this.direction = direction; }
    @Override public Direction getDirection() { return direction; }

    @Override public void setPosition(double position) {
        lastRequested = position;
        this.position = Math.max(0, Math.min(1, position));
    }

    @Override public double getPosition() { return position; }

    public double getLastRequested() { return lastRequested; }

    public double shaftPosition() {
        return direction == Direction.REVERSE ? 1 - position : position;
    }
}
