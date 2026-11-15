package org.firstinspires.ftc.teamcode.utils;

import com.qualcomm.robotcore.util.ElapsedTime;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Timer {
    private final static ArrayList<TimedTrigger> timedTriggers = new ArrayList<>();
    private final static ElapsedTime timer = new ElapsedTime();
    private static double PTime = 0;
    private static double DTime = 0;
    public static double getTime(TimeUnit unit){
        return timer.time(unit);
    }

    public static void init(){
        timer.reset();
        PTime = 0;
        DTime = 0;
        timedTriggers.clear();
    }
    public static void update(){
        double time = timer.milliseconds();
        DTime = time - PTime;
        PTime = time;
        updateTriggers();
        TimedTelemetry.addData("Time: ", time);
        TimedTelemetry.addData("DTime: ", DTime());
    }
    public static double DTime(){
        return DTime;
    }
    public static void addTrigger(int timeMillis, Runnable runThis){
        timedTriggers.add(new TimedTrigger(timeMillis, runThis));
    }
    public static int getTriggerCount(){
        return timedTriggers.size();
    }
    private static void updateTriggers(){
        for (TimedTrigger trigger : new ArrayList<>(timedTriggers)) {
            if (trigger.update()) timedTriggers.remove(trigger);
        }
    }

}

class TimedTrigger{
    private double millis;
    private final Runnable runThis;
    public TimedTrigger(int timeMillis, Runnable runThis){
        this.millis = timeMillis;
        this.runThis = runThis;
    }

    public boolean update(){
        this.millis -= Timer.DTime();
        if (millis<=0){
            run();
            return true;
        }
        return false;
    }
    private void run(){
        runThis.run();
    }
}
