package paths;

import java.util.concurrent.atomic.AtomicLong;

public final class Evaluator {
    public static final AtomicLong TOTAL = new AtomicLong();
    private static final ThreadLocal<Evaluator> LOCAL = ThreadLocal.withInitial(Evaluator::new);
    private static final int MIN_SAMPLES = 24;
    private static final int MAX_SAMPLES = 800;
    private static final double MAX_CURVATURE = 100;

    private final double[] px = new double[MAX_SAMPLES + 1];
    private final double[] py = new double[MAX_SAMPLES + 1];
    private final double[] ph = new double[MAX_SAMPLES + 1];
    private final double[] pk = new double[MAX_SAMPLES + 1];
    private final double[] pg = new double[MAX_SAMPLES + 1];
    private final double[] pt = new double[MAX_SAMPLES + 1];
    private final double[] ps = new double[MAX_SAMPLES + 1];
    private final double[] ds = new double[MAX_SAMPLES + 1];
    private final double[] cap = new double[MAX_SAMPLES + 1];
    private final double[] v = new double[MAX_SAMPLES + 1];

    public long count;

    public static Evaluator get() {
        return LOCAL.get();
    }

    public int sampleCount(Leg leg) {
        double a = Math.hypot(leg.c1x - leg.start.x, leg.c1y - leg.start.y);
        double b = Math.hypot(leg.c2x - leg.c1x, leg.c2y - leg.c1y);
        double c = Math.hypot(leg.end.x - leg.c2x, leg.end.y - leg.c2y);
        double maxSeg = Math.max(a, Math.max(b, c));
        int n = (int) Math.ceil(3 * maxSeg / Spec.SAMPLE_SPACING);
        return Math.max(MIN_SAMPLES, Math.min(MAX_SAMPLES, n));
    }

    public Result evaluate(Leg leg, FieldMap field) {
        count++;
        TOTAL.incrementAndGet();
        int n = sample(leg);
        double length = ps[n];
        if (length < 1) return Result.fail(10, length, 0);

        double violation = 0;
        double penalty = 0;
        double minClear = Double.MAX_VALUE;
        double soft = Spec.SOFT_OBSTACLE;
        double near = Spec.CORNER_REACH + soft + FieldMap.FLOWER_RADIUS;
        for (int i = 0; i <= n; i++) {
            double x = px[i], y = py[i], h = ph[i];
            double c = Math.cos(h), s = Math.sin(h);
            double reach = Spec.HALF_SIZE * (Math.abs(c) + Math.abs(s));
            double dsPrev = i > 0 ? ds[i - 1] : 0, dsNext = i < n ? ds[i] : 0;
            double dhPrev = i > 0 ? Math.abs(Geo.wrap(ph[i] - ph[i - 1])) : 0;
            double dhNext = i < n ? Math.abs(Geo.wrap(ph[i + 1] - ph[i])) : 0;
            double slack = 0.5 * (Math.max(dsPrev, dsNext) + Spec.CORNER_REACH * Math.max(dhPrev, dhNext));
            double weight = 0.5 * (dsPrev + dsNext);

            double wall = FieldMap.HALF - Math.max(Math.abs(x), Math.abs(y)) - reach;
            if (wall < -Spec.WALL_TOLERANCE) violation += -wall;
            if (wall < Spec.SOFT_WALL) {
                double q = (Spec.SOFT_WALL - Math.max(0, wall)) / Spec.SOFT_WALL;
                penalty += Spec.WALL_WEIGHT * q * q * weight;
            }
            double half = field.side * x - reach;
            if (half < Spec.CENTER_LINE_MARGIN) violation += Spec.CENTER_LINE_MARGIN - half;
            if (half < Spec.SOFT_WALL) {
                double q = (Spec.SOFT_WALL - Math.max(0, half)) / Spec.SOFT_WALL;
                penalty += Spec.WALL_WEIGHT * q * q * weight;
            }
            for (double[] o : FieldMap.OBSTACLES) {
                double dx = o[0] - x, dy = o[1] - y;
                if (dx * dx + dy * dy > near * near) continue;
                double clear = Geo.circleClearance(x, y, c, s, o[0], o[1], o[2]);
                if (clear < minClear) minClear = clear;
                double need = Spec.OBSTACLE_MARGIN + slack;
                if (clear < need) violation += need - clear;
                if (clear < soft) {
                    double q = (soft - Math.max(0, clear)) / soft;
                    penalty += Spec.OBSTACLE_WEIGHT * q * q * weight;
                }
            }
            double k = Math.abs(pk[i]);
            if (k > 1 / Spec.SOFT_RADIUS) penalty += Spec.CURVE_WEIGHT * (Math.min(k, 2) - 1 / Spec.SOFT_RADIUS) * weight;
        }
        if (violation > 0) return Result.fail(violation, length, minClear);

        double time = profile(n) + Spec.SETTLE_TIME;
        return new Result(true, time, time + penalty, length, minClear, 0);
    }

    private int sample(Leg leg) {
        int n = sampleCount(leg);
        double x0 = leg.start.x, y0 = leg.start.y, x1 = leg.c1x, y1 = leg.c1y;
        double x2 = leg.c2x, y2 = leg.c2y, x3 = leg.end.x, y3 = leg.end.y;
        double chordT = Math.atan2(y3 - y0, x3 - x0);
        double lastT = chordT;
        for (int i = 0; i <= n; i++) {
            double t = (double) i / n, u = 1 - t;
            double b0 = u * u * u, b1 = 3 * u * u * t, b2 = 3 * u * t * t, b3 = t * t * t;
            px[i] = b0 * x0 + b1 * x1 + b2 * x2 + b3 * x3;
            py[i] = b0 * y0 + b1 * y1 + b2 * y2 + b3 * y3;
            double d1x = 3 * u * u * (x1 - x0) + 6 * u * t * (x2 - x1) + 3 * t * t * (x3 - x2);
            double d1y = 3 * u * u * (y1 - y0) + 6 * u * t * (y2 - y1) + 3 * t * t * (y3 - y2);
            double d2x = 6 * u * (x2 - 2 * x1 + x0) + 6 * t * (x3 - 2 * x2 + x1);
            double d2y = 6 * u * (y2 - 2 * y1 + y0) + 6 * t * (y3 - 2 * y2 + y1);
            double sp = Math.hypot(d1x, d1y);
            if (sp > 1e-6) {
                pk[i] = Math.max(-MAX_CURVATURE, Math.min(MAX_CURVATURE, (d1x * d2y - d1y * d2x) / (sp * sp * sp)));
                pt[i] = Math.atan2(d1y, d1x);
            } else {
                pk[i] = i == 0 || i == n ? 0 : MAX_CURVATURE;
                if (i == 0) pt[i] = Math.atan2(y2 - y0, x2 - x0);
                else if (i == n) pt[i] = Math.atan2(y3 - y1, x3 - x1);
                else pt[i] = lastT;
            }
            lastT = pt[i];
        }
        ps[0] = 0;
        for (int i = 0; i < n; i++) {
            ds[i] = Math.hypot(px[i + 1] - px[i], py[i + 1] - py[i]);
            ps[i + 1] = ps[i] + ds[i];
        }
        double length = ps[n];
        double h0 = leg.start.h;
        double delta = Geo.wrap(leg.end.h - h0);
        Leg.Mode mode = leg.mode;
        for (int i = 0; i <= n; i++) {
            if (mode == Leg.Mode.LINEAR) ph[i] = h0 + delta * (length > 0 ? ps[i] / length : 0);
            else if (mode == Leg.Mode.CONSTANT) ph[i] = h0;
            else if (mode == Leg.Mode.TANGENT) ph[i] = pt[i];
            else ph[i] = pt[i] + Math.PI;
        }
        for (int i = 0; i <= n; i++) {
            double before = i > 0 ? headingRate(i - 1) : 0;
            double after = i < n ? headingRate(i) : 0;
            pg[i] = Math.max(before, after);
        }
        return n;
    }

    private double headingRate(int i) {
        double dh = Math.abs(Geo.wrap(ph[i + 1] - ph[i]));
        return ds[i] > 1e-9 ? dh / ds[i] : 0;
    }

    private double profile(int n) {
        for (int i = 0; i <= n; i++) {
            double a = pt[i] - ph[i];
            double g = Math.abs(pg[i]);
            double use = Math.abs(Math.cos(a)) + Math.abs(Math.sin(a)) / Spec.STRAFE_EFFICIENCY + Spec.TURN_RADIUS * g;
            double c = Spec.MAX_SPEED / use;
            if (g > 1e-9) c = Math.min(c, Spec.MAX_TURN_RATE / g);
            double k = Math.abs(pk[i]);
            if (k > 1e-9) c = Math.min(c, Math.sqrt(Spec.MAX_LATERAL_ACCEL / k));
            cap[i] = Math.max(Spec.MIN_SPEED, c);
        }
        double amax2 = Spec.MAX_ACCEL * Spec.MAX_ACCEL;
        v[0] = 0;
        for (int i = 0; i < n; i++) {
            double lat = v[i] * v[i] * Math.abs(pk[i]);
            double at = Math.sqrt(Math.max(0, amax2 - lat * lat));
            v[i + 1] = Math.min(cap[i + 1], Math.sqrt(v[i] * v[i] + 2 * at * ds[i]));
        }
        v[n] = 0;
        for (int i = n - 1; i >= 0; i--) {
            double lat = v[i + 1] * v[i + 1] * Math.abs(pk[i + 1]);
            double at = Math.sqrt(Math.max(0, amax2 - lat * lat));
            v[i] = Math.min(v[i], Math.sqrt(v[i + 1] * v[i + 1] + 2 * at * ds[i]));
        }
        double time = 0;
        for (int i = 0; i < n; i++) {
            double avg = Math.max(0.5 * (v[i] + v[i + 1]), 0.05);
            double turn = Math.abs(Geo.wrap(ph[i + 1] - ph[i])) / Spec.MAX_TURN_RATE;
            time += Math.max(ds[i] / avg, turn);
        }
        return time;
    }

    public double[][] samples(Leg leg) {
        int n = sample(leg);
        double[][] out = new double[n + 1][];
        for (int i = 0; i <= n; i++) out[i] = new double[]{px[i], py[i], ph[i]};
        return out;
    }
}
