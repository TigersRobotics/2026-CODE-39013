package paths;

import java.util.ArrayList;
import java.util.List;

public final class Segment {
    public final String name;
    public final Leg leg;
    public final Result result;
    public final List<Cem.Candidate> top;
    public final long evaluated;
    public final String settings;

    public Segment(String name, Leg leg, Result result, List<Cem.Candidate> top, long evaluated, String settings) {
        this.name = name;
        this.leg = leg;
        this.result = result;
        this.top = top;
        this.evaluated = evaluated;
        this.settings = settings;
    }

    public static Segment of(String name, Optimizer.Solution s) {
        List<Cem.Candidate> top = new ArrayList<>();
        top.add(s.best);
        for (Cem.Candidate c : s.runs) {
            if (top.size() >= 3) break;
            boolean same = false;
            for (Cem.Candidate t : top) if (Math.abs(t.result.cost - c.result.cost) < 1e-9) same = true;
            if (!same && c.result.ok) top.add(c);
        }
        return new Segment(name, s.best.leg, s.best.result, top, s.evaluated, s.settings);
    }

    public boolean ok() {
        return result.ok;
    }

    public double time() {
        return result.ok ? result.time : Double.POSITIVE_INFINITY;
    }
}
