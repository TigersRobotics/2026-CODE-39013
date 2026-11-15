package paths;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public final class Main {
    private Main() {}

    public static void main(String[] args) throws Exception {
        boolean quickRun = args.length > 0 && args[0].equals("quick");
        Path root = Paths.get(args.length > 1 ? args[1] : ".").toAbsolutePath().normalize();
        long t0 = System.nanoTime();

        FieldMap redField = FieldMap.red();
        Planner planner = new Planner(redField, quickRun);
        Routine red = planner.run();
        long searchEvals = planner.searchEvaluations();
        long finalEvals = red.evaluated();

        Routine blue = red.transformed(FieldMap.blue(), true);
        Routine mirrored = red.transformed(FieldMap.blue(), false);
        List<Routine.Plan> redPlans = red.plans();
        List<Routine.Plan> bluePlans = blue.plans();
        double seconds = (System.nanoTime() - t0) / 1e9;

        Report report = new Report(planner, red, blue, mirrored, redPlans, bluePlans, searchEvals, finalEvals,
                Evaluator.TOTAL.get(), seconds, quickRun);
        report.writeAll(root);
        report.printSummary();
    }
}
