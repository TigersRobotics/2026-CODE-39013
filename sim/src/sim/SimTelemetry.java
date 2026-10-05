package sim;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.ArrayList;
import java.util.List;

/** Collects telemetry like the Driver Station screen would show it */
public class SimTelemetry implements Telemetry {
    private final List<String> pending = new ArrayList<>();
    private List<String> shown = new ArrayList<>();
    private final List<String> history = new ArrayList<>();

    @Override public void addData(String caption, Object value) { add(caption + " " + value); }
    @Override public void addData(String caption, String format, Object... args) { add(caption + ": " + String.format(format, args)); }
    @Override public void addLine(String lineCaption) { add(lineCaption); }

    @Override public boolean update() {
        shown = new ArrayList<>(pending);
        pending.clear();
        return true;
    }

    private void add(String line) {
        pending.add(line);
        history.add(line);
    }

    public List<String> getShown() { return shown; }
    public List<String> getHistory() { return history; }
}
