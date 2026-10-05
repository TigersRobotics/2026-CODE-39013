package sim;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.List;

/** Draws the field, robot, and Driver Station telemetry */
public class FieldRenderer {
    public static final int SCALE = 5;
    public static final int FIELD_PX = (int) (SimRobot.FIELD_SIZE * SCALE);
    public static final int PANEL_PX = 340;
    public static final int WIDTH = FIELD_PX + PANEL_PX;
    public static final int HEIGHT = FIELD_PX;

    private static final Color TILE = new Color(0x3a3d42);
    private static final Color SEAM = new Color(0x4a4e55);
    private static final Color RED = new Color(0xd94848);
    private static final Color BLUE = new Color(0x3d7be0);
    private static final Color PANEL = new Color(0x1d1f23);
    private static final Color TEXT = new Color(0xe6e6e6);
    private static final Color DIM = new Color(0x9aa0a8);

    public static void draw(Graphics2D g, SimSession session, String[] help) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawField(g);
        drawRobot(g, session.robot);
        drawPanel(g, session, help);
    }

    private static int px(double fieldX) { return (int) Math.round((fieldX + SimRobot.FIELD_SIZE / 2) * SCALE); }
    private static int py(double fieldY) { return (int) Math.round((SimRobot.FIELD_SIZE / 2 - fieldY) * SCALE); }

    private static void drawField(Graphics2D g) {
        g.setColor(TILE);
        g.fillRect(0, 0, FIELD_PX, FIELD_PX);
        g.setColor(SEAM);
        for (int i = 1; i < 6; i++) {
            int p = i * 24 * SCALE;
            g.drawLine(p, 0, p, FIELD_PX);
            g.drawLine(0, p, FIELD_PX, p);
        }

        // red ALLIANCE AREA is on the left from the audience, audience is at the bottom
        g.setStroke(new BasicStroke(6));
        g.setColor(RED);
        g.drawLine(2, 0, 2, FIELD_PX);
        g.setColor(BLUE);
        g.drawLine(FIELD_PX - 3, 0, FIELD_PX - 3, FIELD_PX);

        // HIVE frame base
        g.setStroke(new BasicStroke(3));
        int hx = px(-SimRobot.HIVE_WIDTH / 2), hy = py(SimRobot.HIVE_DEPTH / 2);
        int hw = (int) (SimRobot.HIVE_WIDTH * SCALE), hh = (int) (SimRobot.HIVE_DEPTH * SCALE);
        g.setColor(new Color(0x2a2c30));
        g.fillRect(hx, hy, hw, hh);
        g.setColor(new Color(0xc9a227));
        g.drawRect(hx, hy, hw, hh);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g.drawString("HIVE", px(0) - 18, py(0) + 5);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(DIM);
        g.drawString("audience side", px(0) - 36, FIELD_PX - 8);
    }

    private static void drawRobot(Graphics2D g, SimRobot robot) {
        AffineTransform saved = g.getTransform();
        g.translate(px(robot.x), py(robot.y));
        g.rotate(robot.heading);

        int half = (int) (SimRobot.ROBOT_SIZE / 2 * SCALE);
        g.setColor(new Color(0x60656e));
        g.fillRoundRect(-half, -half, half * 2, half * 2, 10, 10);
        g.setColor(new Color(0xf2c230));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(-half, -half, half * 2, half * 2, 10, 10);

        // wheels, green pushes forward and red pushes back
        int ww = 14, wh = 26;
        drawWheel(g, -half - 2, -half + 4, ww, wh, robot.wheelFL());
        drawWheel(g, half - ww + 2, -half + 4, ww, wh, robot.wheelFR());
        drawWheel(g, -half - 2, half - wh - 4, ww, wh, robot.wheelBL());
        drawWheel(g, half - ww + 2, half - wh - 4, ww, wh, robot.wheelBR());

        // front arrow
        Path2D arrow = new Path2D.Double();
        arrow.moveTo(0, -half + 6);
        arrow.lineTo(-10, -half + 22);
        arrow.lineTo(10, -half + 22);
        arrow.closePath();
        g.setColor(TEXT);
        g.fill(arrow);

        // turret
        g.rotate(robot.turretAngle());
        g.setColor(new Color(0x2b2e33));
        g.fillOval(-18, -18, 36, 36);
        g.setColor(new Color(0xf28c28));
        g.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(0, 0, 0, -34);

        g.setTransform(saved);
    }

    private static void drawWheel(Graphics2D g, int x, int y, int w, int h, double power) {
        int level = (int) (Math.min(1, Math.abs(power)) * 200);
        g.setColor(power >= 0 ? new Color(40, 55 + level, 60) : new Color(55 + level, 45, 45));
        g.fillRoundRect(x, y, w, h, 4, 4);
    }

    private static void drawPanel(Graphics2D g, SimSession session, String[] help) {
        int x0 = FIELD_PX;
        g.setColor(PANEL);
        g.fillRect(x0, 0, PANEL_PX, HEIGHT);

        int y = 30;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g.setColor(TEXT);
        g.drawString("Team 39013 Robot Sim", x0 + 16, y);
        y += 20;
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(DIM);
        g.drawString(String.format("ControllerTest   t = %.1f s", session.time), x0 + 16, y);

        y += 30;
        y = section(g, "Driver Station telemetry", x0, y);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        g.setColor(TEXT);
        List<String> lines = session.telemetry.getShown();
        for (String line : lines) {
            g.drawString(line, x0 + 16, y);
            y += 17;
        }

        y += 16;
        y = section(g, "Robot", x0, y);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        g.setColor(TEXT);
        SimRobot r = session.robot;
        g.drawString(String.format("pos  x %6.1f  y %6.1f in", r.x, r.y), x0 + 16, y); y += 17;
        g.drawString(String.format("heading %6.1f deg", Math.toDegrees(r.heading)), x0 + 16, y); y += 17;
        double speed = Math.hypot(r.forwardVel, r.strafeVel);
        g.drawString(String.format("speed   %6.1f in/s", speed), x0 + 16, y); y += 17;
        g.drawString(String.format("turret servo %.2f", r.turretLeft.getPosition()), x0 + 16, y); y += 17;
        if (r.turretLeft.getLastRequested() < 0 || r.turretLeft.getLastRequested() > 1) {
            g.setColor(new Color(0xf28c28));
            g.drawString(String.format("  asked for %.2f, clamped", r.turretLeft.getLastRequested()), x0 + 16, y);
            y += 17;
        }

        y += 16;
        y = section(g, "Controls", x0, y);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT);
        for (String line : help) {
            g.drawString(line, x0 + 16, y);
            y += 17;
        }
    }

    private static int section(Graphics2D g, String title, int x0, int y) {
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(DIM);
        g.drawString(title.toUpperCase(), x0 + 16, y);
        return y + 20;
    }
}
