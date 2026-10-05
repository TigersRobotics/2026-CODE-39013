package org.firstinspires.ftc.robotcore.external;

// Sim stand-in for the FTC SDK interface, only what our code uses
public interface Telemetry {
    void addData(String caption, Object value);
    void addData(String caption, String format, Object... args);
    void addLine(String lineCaption);
    boolean update();
}
