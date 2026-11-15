package org.firstinspires.ftc.teamcode.utils;

import androidx.annotation.NonNull;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.ArrayList;

public class TimedTelemetry {
    public static ArrayList<String> getTelemetryListAsStrings(){
        ArrayList<String> r = new ArrayList<>();
        for (TelemetryLine t: telemetryList) r.add(t.toString());
        return r;
    }

    public static void resetAll (){
        telemetryList.clear();
    }
    static Telemetry telemetry;
    private static final ArrayList<TelemetryLine> telemetryList= new ArrayList<>();

    public static void init(Telemetry telemetry){
        TimedTelemetry.telemetry = telemetry;
        resetAll();
    }

    public static void update(){
        if (telemetry == null) {
            resetAll();
            return;
        }
        telemetryList.removeIf(line -> !line.update());
    }

    public static void stop(){
        resetAll();
        telemetry = null;
    }

    private static void add(TelemetryLine line){
        if (telemetry != null) telemetryList.add(line);
    }

    public static void addLine(String caption, int timeMillis, boolean forever){
        add(new TelemetryLine(timeMillis, caption).setForever(forever));
    }
    public static void addLine(String captions, int timeMillis){
        addLine(captions, timeMillis, false);
    }
    public static void addLineForever(String captions){
        addLine(captions, 10000, true);
    }
    public static void addLine(String caption){
        addLine(caption, 1, false);
    }

    public static void addData(String caption, Object value, int timeMillis, boolean forever){
        add(new TelemetryLine(timeMillis, caption, value).setForever(forever));
    }

    public static void addDataForever(String caption, Object value){
        addData(caption, value, 10000, true);
    }

    public static void addData(String caption, Object value, int timeMillis){
        addData(caption, value, timeMillis, false);
    }

    public static void addData(String caption, Object value){
        addData(caption, value, 1, false);
    }

    public static void addData(String caption, String format, int timeMillis, boolean forever, Object... args){
        add(new TelemetryLine(timeMillis, caption, format, args).setForever(forever));
    }
    public static void addDataForever(String captions, String format, Object... args){
        addData(captions, format, 1000, true, args);
    }
    public static void addData(String caption, String format, int timeMillis, Object... args){
        addData(caption, format, timeMillis, false, args);
    }

}

class TelemetryLine {

    boolean forever = false;
    double timeMillis;
    String caption;
    String format;
    Object value = null;
    Object[] args;
    enum LineType{
        STRING,
        CAPTIONED_OBJECT,
        FORMATED_OBJECT_LIST

    }
    LineType type;

    public TelemetryLine(int timeMillis, String caption) {
        this.timeMillis = timeMillis;
        this.caption = caption;
        type = LineType.STRING;
    }

    public TelemetryLine(int timeMillis, String caption, Object value) {
        this.timeMillis = timeMillis;
        this.caption = caption;
        this.value = value;
        type= LineType.CAPTIONED_OBJECT;
    }
    public TelemetryLine(int timeMillis, String caption, String format, Object... args) {
        this.timeMillis = timeMillis;
        this.caption = caption;
        this.format = format;
        this.args = args;
        type = LineType.FORMATED_OBJECT_LIST;
    }

    public TelemetryLine setForever(boolean forever){
        if (forever) this.timeMillis = 10000;
        this.forever = forever;
        return this;
    }

    public boolean update(){
        if (!forever&&Timer.DTime() >0) timeMillis -= Timer.DTime();
        if(type == LineType.STRING){
            TimedTelemetry.telemetry.addLine(caption);
        } else if (type == LineType.CAPTIONED_OBJECT) {
            TimedTelemetry.telemetry.addData(caption, value);
        } else if (type==LineType.FORMATED_OBJECT_LIST){
            TimedTelemetry.telemetry.addData(caption, format, args);
        }
        return !(timeMillis <= 0);
    }

    @NonNull
    @Override
    public String toString(){
        return this.caption+" Time: " + this.timeMillis;
    }
}
