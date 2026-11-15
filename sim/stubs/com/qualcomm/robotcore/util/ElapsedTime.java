package com.qualcomm.robotcore.util;

import java.util.concurrent.TimeUnit;

import sim.SimClock;

public class ElapsedTime {
    private long nsStartTime;

    public ElapsedTime() {
        reset();
    }

    public void reset() {
        nsStartTime = SimClock.nanoTime();
    }

    public long nanoseconds() {
        return SimClock.nanoTime() - nsStartTime;
    }

    public double milliseconds() {
        return nanoseconds() / 1e6;
    }

    public double seconds() {
        return nanoseconds() / 1e9;
    }

    public double time() {
        return seconds();
    }

    public long time(TimeUnit unit) {
        return unit.convert(nanoseconds(), TimeUnit.NANOSECONDS);
    }
}
