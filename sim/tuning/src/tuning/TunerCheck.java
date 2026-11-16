package tuning;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.constants.HardwareNames;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TunerCheck {
    public static final String[] TESTS = {"HOLD", "LINE", "CURVED", "INTERPOLATION_CURVED", "LOCALIZATION", "DRIVING", "POSE"};
    public static final long TEST_RUN_MS = 6000;

    private static final Gson GSON = new Gson();
    private static TuningRobot robot;
    private static int failures;

    public static void main(String[] args) throws Exception {
        robot = TunerSim.start();
        HttpClient http = HttpClient.newHttpClient();
        String list = http.send(HttpRequest.newBuilder(URI.create(TunerSim.URL + "/api/tuners")).build(),
                HttpResponse.BodyHandlers.ofString()).body();
        JsonArray tuners = GSON.fromJson(list, JsonArray.class);

        for (String name : new String[]{"Pinpoint Tuner", "Mecanum Tuner", "Foresight Tuner", "Tests"}) {
            for (JsonElement tuner : tuners) {
                if (!tuner.getAsJsonObject().get("name").getAsString().equals(name)) continue;
                String id = tuner.getAsJsonObject().get("id").getAsString();
                if (name.equals("Tests")) {
                    for (String test : TESTS) run(http, id, name, test);
                } else {
                    run(http, id, name, null);
                }
            }
        }

        System.out.println();
        System.out.println(failures == 0 ? "All procedures completed" : failures + " procedure(s) failed");
        System.exit(failures == 0 ? 0 : 1);
    }

    private static void run(HttpClient http, String id, String name, String test) throws Exception {
        System.out.println();
        System.out.println("===== " + name + (test == null ? "" : " (" + test + ")") + " =====");
        long start = System.currentTimeMillis();
        BlockingQueue<JsonObject> inbox = new LinkedBlockingQueue<>();
        WebSocket socket = http.newWebSocketBuilder().buildAsync(URI.create("ws://localhost:12649"), new WebSocket.Listener() {
            private final StringBuilder text = new StringBuilder();

            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                text.append(data);
                if (last) {
                    inbox.add(GSON.fromJson(text.toString(), JsonObject.class));
                    text.setLength(0);
                }
                webSocket.request(1);
                return null;
            }
        }).join();
        send(socket, "{\"type\":\"init\",\"id\":\"" + id + "\"}");

        String spun = "none";
        while (true) {
            JsonObject message = inbox.poll(180, TimeUnit.SECONDS);
            if (message == null) {
                System.out.println("  FAIL: no message for 3 minutes");
                failures++;
                break;
            }
            String type = message.get("type").getAsString();
            if (type.equals("confirm")) {
                System.out.println("  step: " + message.get("title").getAsString());
                send(socket, "{\"type\":\"confirm\",\"requestId\":" + message.get("requestId").getAsLong() + "}");
            } else if (type.equals("inputs")) {
                JsonObject values = new JsonObject();
                for (JsonElement field : message.getAsJsonObject("inputs").getAsJsonArray("fields")) {
                    JsonObject f = field.getAsJsonObject();
                    JsonElement value = answer(f, test, spun);
                    System.out.println("  input: " + f.get("name").getAsString() + " = " + value);
                    values.add(f.get("id").getAsString(), value);
                }
                JsonObject reply = new JsonObject();
                reply.addProperty("type", "inputs");
                reply.addProperty("requestId", message.get("requestId").getAsLong());
                reply.add("values", values);
                send(socket, reply.toString());
            } else if (type.equals("opModeRunning")) {
                if (message.get("canStop").getAsBoolean()) {
                    spun = waitThenLook(test != null);
                    send(socket, "{\"type\":\"stopOpMode\",\"requestId\":" + message.get("requestId").getAsLong() + "}");
                }
            } else if (type.equals("complete")) {
                System.out.printf("  done in %.1f s%n", (System.currentTimeMillis() - start) / 1000.0);
                for (Map.Entry<String, JsonElement> result : message.getAsJsonObject("results").entrySet()) {
                    System.out.println("  result: " + result.getKey() + " = " + result.getValue().getAsString());
                }
                JsonElement code = message.get("resultCode");
                if (code != null && code.isJsonObject()) {
                    for (Map.Entry<String, JsonElement> c : code.getAsJsonObject().entrySet()) {
                        System.out.println("  code (" + c.getKey() + "):");
                        System.out.println(c.getValue().getAsString());
                    }
                }
                break;
            } else if (type.equals("error")) {
                System.out.println("  FAIL: " + message.get("message").getAsString());
                failures++;
                break;
            }
        }
        socket.sendClose(WebSocket.NORMAL_CLOSURE, "done").join();
        Thread.sleep(500);
    }

    private static String waitThenLook(boolean followerTest) throws InterruptedException {
        Thread.sleep(followerTest ? TEST_RUN_MS : 1000);
        while (robot.isBeingPushed()) Thread.sleep(100);
        synchronized (robot) {
            if (followerTest) {
                System.out.printf("  after %d s the pinpoint reads x %.1f y %.1f heading %.0f deg%n", TEST_RUN_MS / 1000,
                        robot.pods.getPosX(DistanceUnit.INCH), robot.pods.getPosY(DistanceUnit.INCH), robot.pods.getHeading(AngleUnit.DEGREES));
            }
            return robot.spinningWheel();
        }
    }

    private static JsonElement answer(JsonObject field, String test, String spun) {
        String name = field.get("name").getAsString();
        switch (name) {
            case "HardwareMap Name": return GSON.toJsonTree(HardwareNames.ODOMETRY_NAME);
            case "Odometry Pod Type": return GSON.toJsonTree("SWING_ARM");
            case "Front Left Name": return GSON.toJsonTree(HardwareNames.FRONT_LEFT_MOTOR_NAME);
            case "Front Right Name": return GSON.toJsonTree(HardwareNames.FRONT_RIGHT_MOTOR_NAME);
            case "Back Left Name": return GSON.toJsonTree(HardwareNames.BACK_LEFT_MOTOR_NAME);
            case "Back Right Name": return GSON.toJsonTree(HardwareNames.BACK_RIGHT_MOTOR_NAME);
            case "Which way did the motor spin?": return GSON.toJsonTree(spun.endsWith("REVERSE") ? "REVERSE" : "FORWARD");
            case "Test": return GSON.toJsonTree(test);
        }
        if (name.startsWith("Did the ")) {
            String wheel = name.substring("Did the ".length(), name.indexOf(" motor"));
            return GSON.toJsonTree(spun.toLowerCase().startsWith(wheel));
        }
        return field.get("defaultValue");
    }

    private static void send(WebSocket socket, String text) {
        socket.sendText(text, true).join();
    }
}
