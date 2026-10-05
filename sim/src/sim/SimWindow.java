package sim;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/** Drive the robot with the keyboard, keys stand in for the gamepad */
public class SimWindow extends JPanel {
    private static final String[] HELP = {
            "W / S       left stick up / down (drive)",
            "A / D       left stick left / right (strafe)",
            "Left/Right  right stick x (turn)",
            "E           right bumper (faster)",
            "Q           left bumper (slower)",
            "J / L       gamepad 2 left stick x (turret)",
            "R           reset robot",
    };

    private final Set<Integer> keys = new HashSet<>();
    private SimSession session;

    public SimWindow() {
        session = newSession();
        setPreferredSize(new Dimension(FieldRenderer.WIDTH, FieldRenderer.HEIGHT));
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                keys.add(e.getKeyCode());
                if (e.getKeyCode() == KeyEvent.VK_R) session = newSession();
            }
            @Override public void keyReleased(KeyEvent e) { keys.remove(e.getKeyCode()); }
        });
        new Timer((int) (SimSession.LOOP_TIME * 1000), e -> {
            readKeys();
            session.tick();
            repaint();
        }).start();
    }

    private static SimSession newSession() {
        return new SimSession(new SimRobot(-24, -54, 0));
    }

    private float axis(int negative, int positive) {
        return (keys.contains(positive) ? 1 : 0) - (keys.contains(negative) ? 1 : 0);
    }

    private void readKeys() {
        // stick y is negative when pushed up, same as a real gamepad
        session.gamepad1.left_stick_y = axis(KeyEvent.VK_W, KeyEvent.VK_S);
        session.gamepad1.left_stick_x = axis(KeyEvent.VK_A, KeyEvent.VK_D);
        session.gamepad1.right_stick_x = axis(KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT);
        session.gamepad1.right_bumper = keys.contains(KeyEvent.VK_E);
        session.gamepad1.left_bumper = keys.contains(KeyEvent.VK_Q);
        session.gamepad2.left_stick_x = axis(KeyEvent.VK_J, KeyEvent.VK_L);
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        FieldRenderer.draw((Graphics2D) g, session, HELP);
    }

    /** Saves a picture of a session, used to check the drawing without opening a window */
    public static void snapshot(SimSession session, File file) throws IOException {
        BufferedImage image = new BufferedImage(FieldRenderer.WIDTH, FieldRenderer.HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        FieldRenderer.draw(g, session, HELP);
        g.dispose();
        ImageIO.write(image, "png", file);
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 2 && args[0].equals("--snapshot")) {
            // scripted drive: strafe right, drive forward, slow down, turn, aim turret
            SimSession s = newSession();
            s.gamepad1.left_stick_x = 1;
            s.run(0.5);
            s.gamepad1.left_stick_x = 0;
            s.gamepad1.left_stick_y = -1;
            s.run(0.4);
            s.tap(() -> s.gamepad1.left_bumper = true, () -> s.gamepad1.left_bumper = false);
            s.gamepad1.right_stick_x = 0.6f;
            s.gamepad2.left_stick_x = 0.25f;
            s.run(0.3);
            snapshot(s, new File(args[1]));
            return;
        }

        JFrame frame = new JFrame("Team 39013 Robot Sim");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(new SimWindow());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
