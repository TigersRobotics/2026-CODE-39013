package paths;

public final class Geo {
    private Geo() {}

    public static double wrap(double a) {
        double r = a % (2 * Math.PI);
        if (r >= Math.PI) r -= 2 * Math.PI;
        if (r < -Math.PI) r += 2 * Math.PI;
        return r;
    }

    public static double reach(double h) {
        return Spec.HALF_SIZE * (Math.abs(Math.cos(h)) + Math.abs(Math.sin(h)));
    }

    public static double wallClearance(double x, double y, double h) {
        return FieldMap.HALF - Math.max(Math.abs(x), Math.abs(y)) - reach(h);
    }

    public static double circleClearance(double x, double y, double h, double cx, double cy, double r) {
        double c = Math.cos(h), s = Math.sin(h);
        return circleClearance(x, y, c, s, cx, cy, r);
    }

    public static double circleClearance(double x, double y, double c, double s, double cx, double cy, double r) {
        double dx = cx - x, dy = cy - y;
        double f = Math.abs(dx * c + dy * s);
        double l = Math.abs(-dx * s + dy * c);
        double a = Spec.HALF_SIZE;
        if (f <= a && l <= a) return -Math.min(a - f, a - l) - r;
        double ef = Math.max(0, f - a), el = Math.max(0, l - a);
        return Math.sqrt(ef * ef + el * el) - r;
    }

    public static double rectGap(double x, double y, double h, double[] rect) {
        double c = Math.cos(h), s = Math.sin(h);
        double reach = reach(h);
        double gap = Math.max(rect[0] - (x + reach), (x - reach) - rect[2]);
        gap = Math.max(gap, Math.max(rect[1] - (y + reach), (y - reach) - rect[3]));
        double[] xs = {rect[0], rect[2], rect[2], rect[0]};
        double[] ys = {rect[1], rect[1], rect[3], rect[3]};
        double[][] axes = {{c, s}, {-s, c}};
        for (double[] ax : axes) {
            double center = x * ax[0] + y * ax[1];
            double lo = Double.MAX_VALUE, hi = -Double.MAX_VALUE;
            for (int i = 0; i < 4; i++) {
                double p = xs[i] * ax[0] + ys[i] * ax[1];
                lo = Math.min(lo, p);
                hi = Math.max(hi, p);
            }
            gap = Math.max(gap, Math.max(lo - (center + Spec.HALF_SIZE), (center - Spec.HALF_SIZE) - hi));
        }
        return gap;
    }

    public static double segmentDistance(double ax, double ay, double bx, double by, double px, double py) {
        double vx = bx - ax, vy = by - ay;
        double len2 = vx * vx + vy * vy;
        double t = len2 == 0 ? 0 : ((px - ax) * vx + (py - ay) * vy) / len2;
        t = Math.max(0, Math.min(1, t));
        return Math.hypot(ax + t * vx - px, ay + t * vy - py);
    }

    public static boolean axisAligned(double h) {
        double q = h / (Math.PI / 2);
        return Math.abs(q - Math.rint(q)) < 1e-6;
    }
}
