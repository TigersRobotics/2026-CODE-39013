package paths;

import java.util.SplittableRandom;

public final class LegProblem {
    public final String key;
    public final FieldMap field;
    public final FieldPose start;
    public final FieldPose end;
    public final double[] endBox;
    public final boolean freeHeading;
    public final boolean park;

    private LegProblem(String key, FieldMap field, FieldPose start, FieldPose end, double[] endBox, boolean freeHeading, boolean park) {
        this.key = key;
        this.field = field;
        this.start = start;
        this.end = end;
        this.endBox = endBox;
        this.freeHeading = freeHeading;
        this.park = park;
    }

    public static LegProblem fixed(String key, FieldMap field, FieldPose start, FieldPose end) {
        return new LegProblem(key, field, start, end, null, false, false);
    }

    public static LegProblem park(String key, FieldMap field, FieldPose start) {
        FieldPose guess = field.parkGuess();
        return new LegProblem(key, field, start, new FieldPose(guess.x, guess.y, start.h), field.parkBox(), true, true);
    }

    public int curveDims(Leg.Mode mode) {
        return mode == Leg.Mode.LINEAR ? 4 : 2;
    }

    public int dims(Leg.Mode mode) {
        return curveDims(mode) + (endBox != null ? 2 : 0) + (freeHeading ? 1 : 0);
    }

    public double[] lower(Leg.Mode mode) {
        double[] lo = new double[dims(mode)];
        int k = fillCurve(mode, lo, true);
        if (endBox != null) {
            lo[k++] = 0;
            lo[k++] = 0;
        }
        if (freeHeading) lo[k] = end.h - Math.PI;
        return lo;
    }

    public double[] upper(Leg.Mode mode) {
        double[] hi = new double[dims(mode)];
        int k = fillCurve(mode, hi, false);
        if (endBox != null) {
            hi[k++] = 1;
            hi[k++] = 1;
        }
        if (freeHeading) hi[k] = end.h + Math.PI;
        return hi;
    }

    private int fillCurve(Leg.Mode mode, double[] out, boolean low) {
        if (mode == Leg.Mode.LINEAR) {
            out[0] = low ? -0.3 : 1.3;
            out[1] = low ? -1.0 : 1.0;
            out[2] = low ? -0.3 : 1.3;
            out[3] = low ? -1.0 : 1.0;
            return 4;
        }
        out[0] = low ? 0.08 : 1.4;
        out[1] = low ? 0.08 : 1.4;
        return 2;
    }

    public double[] initialMean(Leg.Mode mode, int variant, SplittableRandom rnd) {
        double[] lo = lower(mode), hi = upper(mode);
        double[] m = new double[dims(mode)];
        if (variant >= 3) {
            for (int i = 0; i < m.length; i++) m[i] = lo[i] + rnd.nextDouble() * (hi[i] - lo[i]);
            return m;
        }
        double side = variant == 1 ? 0.35 : variant == 2 ? -0.35 : 0;
        int k;
        if (mode == Leg.Mode.LINEAR) {
            m[0] = 1.0 / 3;
            m[1] = side;
            m[2] = 1.0 / 3;
            m[3] = side;
            k = 4;
        } else {
            double a = variant == 1 ? 0.6 : variant == 2 ? 0.15 : 1.0 / 3;
            m[0] = a;
            m[1] = a;
            k = 2;
        }
        if (endBox != null) {
            m[k++] = 0.5;
            m[k++] = 0.5;
        }
        if (freeHeading) m[k] = end.h;
        return m;
    }

    public double[] initialStd(Leg.Mode mode) {
        double[] s = new double[dims(mode)];
        int c = curveDims(mode);
        for (int i = 0; i < c; i++) s[i] = 0.25;
        int k = c;
        if (endBox != null) {
            s[k++] = 0.3;
            s[k++] = 0.3;
        }
        if (freeHeading) s[k] = 0.8;
        return s;
    }

    public FieldPose endPose(Leg.Mode mode, double[] p) {
        int k = curveDims(mode);
        double ex = end.x, ey = end.y, eh = end.h;
        if (endBox != null) {
            ex = endBox[0] + p[k++] * (endBox[1] - endBox[0]);
            ey = endBox[2] + p[k++] * (endBox[3] - endBox[2]);
        }
        if (freeHeading) eh = p[k];
        return new FieldPose(ex, ey, eh);
    }

    public Leg decode(Leg.Mode mode, double[] p) {
        FieldPose e = endPose(mode, p);
        double cx = e.x - start.x, cy = e.y - start.y;
        double len = Math.hypot(cx, cy);
        if (mode == Leg.Mode.LINEAR) {
            double c1x = start.x + p[0] * cx - p[1] * cy;
            double c1y = start.y + p[0] * cy + p[1] * cx;
            double c2x = e.x - p[2] * cx - p[3] * cy;
            double c2y = e.y - p[2] * cy + p[3] * cx;
            return new Leg(start, e, c1x, c1y, c2x, c2y, mode, false);
        }
        double sign = mode == Leg.Mode.TANGENT ? 1 : -1;
        double c1x = start.x + sign * p[0] * len * Math.cos(start.h);
        double c1y = start.y + sign * p[0] * len * Math.sin(start.h);
        double c2x = e.x - sign * p[1] * len * Math.cos(e.h);
        double c2y = e.y - sign * p[1] * len * Math.sin(e.h);
        return new Leg(start, e, c1x, c1y, c2x, c2y, mode, false);
    }

    public Result evaluate(Leg leg, Evaluator ev) {
        Result r = ev.evaluate(leg, field);
        if (park) r = r.withExtraViolation(field.parkViolation(leg.end));
        return r;
    }

    public Leg straight(FieldPose endPose) {
        return Leg.line(start, endPose, Leg.Mode.LINEAR);
    }
}
