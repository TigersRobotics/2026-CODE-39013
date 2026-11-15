package paths;

import java.util.Arrays;
import java.util.SplittableRandom;

public final class Cem {
    private Cem() {}

    public static final class Settings {
        public final String name;
        public final int population;
        public final int iterations;
        public final int variants;
        public final Leg.Mode[] modes;
        public final boolean lineFirst;

        public Settings(String name, int population, int iterations, int variants, Leg.Mode[] modes, boolean lineFirst) {
            this.name = name;
            this.population = population;
            this.iterations = iterations;
            this.variants = variants;
            this.modes = modes;
            this.lineFirst = lineFirst;
        }

        public int elites() {
            return Math.max(3, population / 8);
        }
    }

    public static final Leg.Mode[] ALL_MODES = {Leg.Mode.LINEAR, Leg.Mode.TANGENT, Leg.Mode.REVERSE_TANGENT};
    public static final Settings SCREEN = new Settings("screen", 12, 4, 3, new Leg.Mode[]{Leg.Mode.LINEAR}, true);
    public static final Settings QUICK = new Settings("quick", 24, 6, 1, ALL_MODES, false);
    public static final Settings MEDIUM = new Settings("medium", 40, 12, 3, ALL_MODES, false);
    public static final Settings FULL = new Settings("full", 80, 25, 5, ALL_MODES, false);

    public static final class Candidate {
        public final Leg leg;
        public final Result result;
        public final Leg.Mode mode;
        public final int variant;

        public Candidate(Leg leg, Result result, Leg.Mode mode, int variant) {
            this.leg = leg;
            this.result = result;
            this.mode = mode;
            this.variant = variant;
        }
    }

    public static Candidate run(LegProblem problem, Leg.Mode mode, int variant, Settings s, SplittableRandom rnd, Evaluator ev) {
        int d = problem.dims(mode);
        double[] lo = problem.lower(mode), hi = problem.upper(mode);
        double[] mean = problem.initialMean(mode, variant, rnd);
        double[] std = problem.initialStd(mode);
        double[] minStd = new double[d];
        for (int i = 0; i < d; i++) minStd[i] = 0.002 * (hi[i] - lo[i]);
        int pop = s.population, elite = s.elites();
        double[][] xs = new double[pop][d];
        double[] costs = new double[pop];
        Integer[] order = new Integer[pop];
        Candidate best = null;
        for (int it = 0; it < s.iterations; it++) {
            for (int j = 0; j < pop; j++) {
                double[] x = xs[j];
                for (int i = 0; i < d; i++) {
                    double val = it == 0 && j == 0 ? mean[i] : mean[i] + std[i] * gaussian(rnd);
                    x[i] = Math.max(lo[i], Math.min(hi[i], val));
                }
                Leg leg = problem.decode(mode, x);
                Result r = problem.evaluate(leg, ev);
                costs[j] = r.cost;
                if (best == null || r.cost < best.result.cost) best = new Candidate(leg, r, mode, variant);
            }
            for (int j = 0; j < pop; j++) order[j] = j;
            Arrays.sort(order, (a, b) -> Double.compare(costs[a], costs[b]));
            for (int i = 0; i < d; i++) {
                double m = 0;
                for (int e = 0; e < elite; e++) m += xs[order[e]][i];
                m /= elite;
                double var = 0;
                for (int e = 0; e < elite; e++) {
                    double q = xs[order[e]][i] - m;
                    var += q * q;
                }
                double sd = Math.sqrt(var / elite);
                mean[i] = 0.7 * m + 0.3 * mean[i];
                std[i] = Math.max(minStd[i], 0.7 * sd + 0.3 * std[i]);
            }
        }
        return best;
    }

    private static double gaussian(SplittableRandom rnd) {
        double u1 = rnd.nextDouble(), u2 = rnd.nextDouble();
        if (u1 < 1e-300) u1 = 1e-300;
        return Math.sqrt(-2 * Math.log(u1)) * Math.cos(2 * Math.PI * u2);
    }
}
