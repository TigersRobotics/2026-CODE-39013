package paths;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SplittableRandom;

public final class Optimizer {
    private Optimizer() {}

    public static final class Solution {
        public final LegProblem problem;
        public final Cem.Candidate best;
        public final List<Cem.Candidate> runs;
        public final long evaluated;
        public final String settings;

        Solution(LegProblem problem, Cem.Candidate best, List<Cem.Candidate> runs, long evaluated, String settings) {
            this.problem = problem;
            this.best = best;
            this.runs = runs;
            this.evaluated = evaluated;
            this.settings = settings;
        }

        public boolean ok() {
            return best != null && best.result.ok;
        }

        public double time() {
            return ok() ? best.result.time : Double.POSITIVE_INFINITY;
        }

        public Leg leg() {
            return best.leg;
        }
    }

    public static long seed(String key) {
        long h = 0xcbf29ce484222325L ^ Spec.SEED;
        for (int i = 0; i < key.length(); i++) {
            h ^= key.charAt(i);
            h *= 0x100000001b3L;
        }
        return h;
    }

    public static Solution solve(LegProblem problem, Cem.Settings s) {
        Evaluator ev = Evaluator.get();
        long before = ev.count;
        List<Cem.Candidate> runs = new ArrayList<>();
        if (s.lineFirst) {
            Leg line = problem.straight(problem.end);
            Result r = problem.evaluate(line, ev);
            if (r.ok) {
                Cem.Candidate c = new Cem.Candidate(line, r, Leg.Mode.LINEAR, -1);
                runs.add(c);
                return new Solution(problem, c, runs, ev.count - before, s.name);
            }
        }
        for (Leg.Mode mode : s.modes) {
            for (int variant = 0; variant < s.variants; variant++) {
                SplittableRandom rnd = new SplittableRandom(seed(problem.key + "|" + s.name + "|" + mode + "|" + variant));
                runs.add(Cem.run(problem, mode, variant, s, rnd, ev));
            }
        }
        runs.sort(Comparator.comparingDouble(c -> c.result.cost));
        Cem.Candidate best = runs.get(0);
        Leg line = problem.straight(best.leg.end);
        Result lr = problem.evaluate(line, ev);
        if (lr.ok && (!best.result.ok || lr.cost <= best.result.cost * 1.003 + 0.003)) {
            best = new Cem.Candidate(line, lr, Leg.Mode.LINEAR, -1);
        }
        if (problem.freeHeading && best.leg.mode == Leg.Mode.LINEAR) {
            double snapped = Math.toRadians(5 * Math.round(Math.toDegrees(best.leg.end.h) / 5));
            for (double h : new double[]{problem.start.h, snapped}) {
                Leg tidy = best.leg.withEndHeading(h);
                Result tr = problem.evaluate(tidy, ev);
                if (tr.ok && tr.cost <= best.result.cost * 1.003 + 0.003) {
                    best = new Cem.Candidate(tidy, tr, best.mode, best.variant);
                    break;
                }
            }
        }
        Leg rounded = best.leg.rounded();
        Result rr = problem.evaluate(rounded, ev);
        if (rr.ok || !best.result.ok) best = new Cem.Candidate(rounded, rr, best.mode, best.variant);
        return new Solution(problem, best, runs, ev.count - before, s.name);
    }
}
