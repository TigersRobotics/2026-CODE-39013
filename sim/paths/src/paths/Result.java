package paths;

public final class Result {
    public static final double INFEASIBLE = 1000;

    public final boolean ok;
    public final double time;
    public final double cost;
    public final double length;
    public final double minClearance;
    public final double violation;

    public Result(boolean ok, double time, double cost, double length, double minClearance, double violation) {
        this.ok = ok;
        this.time = time;
        this.cost = cost;
        this.length = length;
        this.minClearance = minClearance;
        this.violation = violation;
    }

    public static Result fail(double violation, double length, double minClearance) {
        return new Result(false, Double.POSITIVE_INFINITY, INFEASIBLE + 10 * violation, length, minClearance, violation);
    }

    public Result withExtraViolation(double extra) {
        if (extra <= 0) return this;
        double v = violation + extra;
        return new Result(false, Double.POSITIVE_INFINITY, INFEASIBLE + 10 * v, length, minClearance, v);
    }
}
