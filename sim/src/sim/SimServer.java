package sim;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import org.firstinspires.ftc.teamcode.constants.RobotSpecs;
import org.firstinspires.ftc.teamcode.robotControllers.FlywheelController;
import org.firstinspires.ftc.teamcode.robotControllers.UptakeServoController;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SimServer {
    public static final int PORT = 8039;
    private static final long INPUT_TIMEOUT_MS = 500;

    private final Path web;
    private SimSession session;
    private boolean red = true;
    private final float[][] input = new float[2][];
    private long lastInput;

    public SimServer(Path web) {
        this.web = web;
        reset();
    }

    private synchronized void reset() {
        if (session != null) session.stop();
        session = new SimSession(SimRobot.atStart(red));
    }

    private synchronized void tick() {
        boolean fresh = System.currentTimeMillis() - lastInput < INPUT_TIMEOUT_MS;
        apply(session.gamepad1, fresh ? input[0] : null);
        apply(session.gamepad2, fresh ? input[1] : null);
        session.tick();
    }

    private static void apply(Gamepad g, float[] v) {
        if (v == null) v = new float[24];
        g.left_stick_x = v[0];
        g.left_stick_y = v[1];
        g.right_stick_x = v[2];
        g.right_stick_y = v[3];
        g.left_trigger = v[4];
        g.right_trigger = v[5];
        g.a = v[6] > 0;
        g.b = v[7] > 0;
        g.x = v[8] > 0;
        g.y = v[9] > 0;
        g.left_bumper = v[10] > 0;
        g.right_bumper = v[11] > 0;
        g.back = v[14] > 0;
        g.start = v[15] > 0;
        g.left_stick_button = v[16] > 0;
        g.right_stick_button = v[17] > 0;
        g.dpad_up = v[18] > 0;
        g.dpad_down = v[19] > 0;
        g.dpad_left = v[20] > 0;
        g.dpad_right = v[21] > 0;
        g.guide = v[22] > 0;
        g.touchpad = v[23] > 0;
    }

    private static float[] parse(String s) {
        if (s == null || s.isEmpty()) return null;
        String[] parts = s.split(",");
        float[] v = new float[24];
        for (int i = 0; i < Math.min(parts.length, v.length); i++) {
            try {
                v[i] = Float.parseFloat(parts[i]);
            } catch (NumberFormatException e) {
                v[i] = 0;
            }
        }
        return v;
    }

    private synchronized String handleInput(String body) {
        String[] lines = body.split("\n", -1);
        input[0] = parse(lines.length > 0 ? lines[0] : null);
        input[1] = parse(lines.length > 1 ? lines[1] : null);
        lastInput = System.currentTimeMillis();
        return stateJson();
    }

    private String stateJson() {
        SimRobot r = session.robot;
        LLResult view = r.limelight.view();
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        sb.append(String.format(Locale.US, "\"t\":%.2f,\"x\":%.3f,\"y\":%.3f,\"heading\":%.4f,", session.time, r.x, r.y, r.heading));
        sb.append(String.format(Locale.US, "\"speed\":%.2f,", Math.hypot(r.forwardVel, r.strafeVel)));
        sb.append(String.format(Locale.US, "\"wheels\":[%.3f,%.3f,%.3f,%.3f],", r.wheelFL(), r.wheelFR(), r.wheelBL(), r.wheelBR()));
        sb.append(String.format(Locale.US, "\"turret\":%.4f,\"turretServo\":%.3f,\"turretAsked\":%.3f,",
                r.turretAngle(), r.turretLeft.getPosition(), r.turretLeft.getLastRequested()));
        sb.append(String.format(Locale.US, "\"flywheel\":%.0f,\"flywheelTarget\":%.0f,\"flywheelReady\":%b,",
                r.flywheel.getVelocity(), r.flywheel.getTargetVelocity(), FlywheelController.isAtSpeed()));
        sb.append(String.format(Locale.US, "\"intake\":%.2f,\"uptake\":%.2f,", r.intake.getPower(), r.uptake.getPower()));
        sb.append(String.format(Locale.US, "\"kickerUp\":%b,\"kickerBusy\":%b,",
                r.kicker.getPosition() > RobotSpecs.UPTAKE_SERVO_DEFAULT_ANGLE + 1e-6, UptakeServoController.isBusy()));
        sb.append(String.format(Locale.US, "\"hood\":%.1f,", r.hood.getPosition() * RobotSpecs.HOOD_SERVO_MAX_ANGLE));
        sb.append(String.format(Locale.US, "\"llValid\":%b,\"llTx\":%.2f,\"target\":[%.2f,%.2f],",
                view.isValid(), view.getTx(), r.targetX, r.targetY));
        sb.append("\"red\":").append(red).append(',');
        sb.append(String.format(Locale.US, "\"robot\":[%.2f,%.2f],", SimRobot.ROBOT_WIDTH, SimRobot.ROBOT_LENGTH));
        sb.append("\"telemetry\":[");
        boolean first = true;
        for (String line : session.telemetry.getShown()) {
            if (!first) sb.append(',');
            first = false;
            sb.append('"').append(line.replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
        }
        sb.append("]}");
        return sb.toString();
    }

    private void send(HttpExchange ex, int code, String type, byte[] body) throws IOException {
        ex.getResponseHeaders().set("Content-Type", type);
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.sendResponseHeaders(code, body.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(body);
        }
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", PORT), 0);
        server.createContext("/", ex -> {
            String path = ex.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            Path file = web.resolve(path.substring(1)).normalize();
            if (!file.startsWith(web) || !Files.isRegularFile(file)) {
                send(ex, 404, "text/plain", "not found".getBytes(StandardCharsets.UTF_8));
                return;
            }
            String type = path.endsWith(".html") ? "text/html; charset=utf-8" : "application/octet-stream";
            send(ex, 200, type, Files.readAllBytes(file));
        });
        server.createContext("/input", ex -> {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            send(ex, 200, "application/json", handleInput(body).getBytes(StandardCharsets.UTF_8));
        });
        server.createContext("/reset", ex -> {
            String q = ex.getRequestURI().getQuery();
            if (q != null && q.contains("alliance=blue")) red = false;
            if (q != null && q.contains("alliance=red")) red = true;
            reset();
            send(ex, 200, "text/plain", "ok".getBytes(StandardCharsets.UTF_8));
        });
        server.start();

        long periodMs = Math.round(SimSession.LOOP_TIME * 1000);
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                tick();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }, 0, periodMs, TimeUnit.MILLISECONDS);

        System.out.println("Sim running at http://localhost:" + PORT + "  (Ctrl+C to stop)");
    }

    public static void main(String[] args) throws IOException {
        new SimServer(Path.of("web").toAbsolutePath().normalize()).start();
    }
}
