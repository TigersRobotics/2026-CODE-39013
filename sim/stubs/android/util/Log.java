package android.util;

public final class Log {
    public static int d(String tag, String msg) {
        return 0;
    }

    public static int e(String tag, String msg, Throwable tr) {
        System.out.println(tag + ": " + msg + " " + tr);
        return 0;
    }

    private Log() {}
}
