package paths;

import java.util.Locale;

public final class FieldPose {
    public final double x;
    public final double y;
    public final double h;

    public FieldPose(double x, double y, double h) {
        this.x = x;
        this.y = y;
        this.h = Geo.wrap(h);
    }

    public static FieldPose pedro(double px, double py, double degrees) {
        return new FieldPose(px - FieldMap.HALF, py - FieldMap.HALF, Math.toRadians(degrees));
    }

    public FieldPose rotated() {
        return new FieldPose(-x, -y, h + Math.PI);
    }

    public FieldPose mirrored() {
        return new FieldPose(-x, y, Math.PI - h);
    }

    public FieldPose rounded() {
        double px = Math.round(pedroX() * 100) / 100.0;
        double py = Math.round(pedroY() * 100) / 100.0;
        double deg = Math.round(pedroDegrees() * 10) / 10.0;
        return pedro(px, py, deg);
    }

    public double pedroX() {
        return x + FieldMap.HALF;
    }

    public double pedroY() {
        return y + FieldMap.HALF;
    }

    public double pedroDegrees() {
        double d = Math.toDegrees(h) % 360;
        if (d < 0) d += 360;
        if (d >= 359.95) d = 0;
        return d;
    }

    public double distance(FieldPose o) {
        return Math.hypot(o.x - x, o.y - y);
    }

    public String pedroText() {
        return String.format(Locale.US, "(%.2f, %.2f, %.1f)", pedroX(), pedroY(), pedroDegrees());
    }

    @Override
    public String toString() {
        return pedroText();
    }
}
