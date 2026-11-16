package org.firstinspires.ftc.robotserver.internal.webserver;

public final class MimeTypesUtil {
    public static String determineMimeType(String path) {
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".json")) return "application/json";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".wasm")) return "application/wasm";
        return "application/octet-stream";
    }

    private MimeTypesUtil() {}
}
