package sim;

public final class SimClock {
    private static long nanos = 1_000_000_000L;

    public static long nanoTime() {
        return nanos;
    }

    public static void advance(double seconds) {
        nanos += Math.round(seconds * 1e9);
    }

    private SimClock() {}
}
