package paths;

public final class Leg {
    public enum Mode { LINEAR, TANGENT, REVERSE_TANGENT, CONSTANT }

    public final FieldPose start;
    public final FieldPose end;
    public final double c1x, c1y, c2x, c2y;
    public final Mode mode;
    public final boolean straight;

    public Leg(FieldPose start, FieldPose end, double c1x, double c1y, double c2x, double c2y, Mode mode, boolean straight) {
        this.start = start;
        this.end = end;
        this.c1x = c1x;
        this.c1y = c1y;
        this.c2x = c2x;
        this.c2y = c2y;
        this.mode = mode;
        this.straight = straight;
    }

    public static Leg line(FieldPose start, FieldPose end, Mode mode) {
        double dx = end.x - start.x, dy = end.y - start.y;
        return new Leg(start, end, start.x + dx / 3, start.y + dy / 3, start.x + 2 * dx / 3, start.y + 2 * dy / 3, mode, true);
    }

    public Mode exportMode() {
        if (mode == Mode.LINEAR && Math.abs(Geo.wrap(end.h - start.h)) < Math.toRadians(0.05)) return Mode.CONSTANT;
        return mode;
    }

    public Leg withEndHeading(double h) {
        return new Leg(start, new FieldPose(end.x, end.y, h), c1x, c1y, c2x, c2y, mode, straight);
    }

    public Leg rotated() {
        return new Leg(start.rotated(), end.rotated(), -c1x, -c1y, -c2x, -c2y, mode, straight);
    }

    public Leg mirrored() {
        Mode m = mode;
        return new Leg(start.mirrored(), end.mirrored(), -c1x, c1y, -c2x, c2y, m, straight);
    }

    public Leg rounded() {
        FieldPose s = start.rounded(), e = end.rounded();
        double r1x = round(c1x), r1y = round(c1y), r2x = round(c2x), r2y = round(c2y);
        if (straight) return line(s, e, mode);
        return new Leg(s, e, r1x, r1y, r2x, r2y, mode, false);
    }

    private static double round(double v) {
        return Math.round((v + FieldMap.HALF) * 100) / 100.0 - FieldMap.HALF;
    }

    public double[] point(double t) {
        double u = 1 - t;
        double b0 = u * u * u, b1 = 3 * u * u * t, b2 = 3 * u * t * t, b3 = t * t * t;
        return new double[]{
                b0 * start.x + b1 * c1x + b2 * c2x + b3 * end.x,
                b0 * start.y + b1 * c1y + b2 * c2y + b3 * end.y};
    }
}
