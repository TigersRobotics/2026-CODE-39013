package tuning;

import android.content.Context;
import android.content.res.AssetManager;

import com.pedropathing.tuning.autotune.Procedure;
import com.pedropathing.tuning.autotune.Tuner;
import com.pedropathing.tuning.autotune.TunerRegistrar;
import com.qualcomm.ftccommon.FtcEventLoop;

import dev.frozenmilk.sinister.sdk.apphooks.OnCreateEventLoop;

import org.firstinspires.ftc.teamcode.pedro.Tuning;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import sim.SimClock;

public class TunerSim {
    public static final String WEBSITE_AAR = ".deps/downloads/pedro-tuning.aar";
    public static final String URL = "http://localhost:10158";

    public static void main(String[] args) throws Exception {
        start();
        System.out.println("Pedro AutoTune is running on the simulated robot: " + URL);
        Thread.currentThread().join();
    }

    public static TuningRobot start() throws Exception {
        SimClock.realTime = true;
        TuningRobot robot = new TuningRobot();
        runPhysics(robot);

        for (Method method : Tuning.class.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Tuner.class)) {
                TunerRegistrar.register(method.getAnnotation(Tuner.class).name(), (Procedure) method.invoke(null));
            }
        }

        ZipFile aar = new ZipFile(WEBSITE_AAR);
        AssetManager assets = new AssetManager() {
            @Override
            public InputStream open(String fileName) throws IOException {
                ZipEntry entry = aar.getEntry("assets/" + fileName);
                if (entry == null) throw new FileNotFoundException(fileName);
                return aar.getInputStream(entry);
            }
        };
        Context context = new Context() {
            @Override
            public AssetManager getAssets() { return assets; }
        };

        Field hook = Class.forName("com.pedropathing.tuning.autotune.Hooks").getDeclaredField("onCreateEventLoop");
        hook.setAccessible(true);
        ((OnCreateEventLoop) hook.get(null)).onCreateEventLoop(context, new FtcEventLoop(new SimOpModeManager(robot)));
        return robot;
    }

    public static void runPhysics(TuningRobot robot) {
        Thread physics = new Thread(() -> {
            long last = System.nanoTime();
            while (true) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    return;
                }
                long now = System.nanoTime();
                double dt = Math.min(0.05, (now - last) / 1e9);
                last = now;
                synchronized (robot) {
                    while (dt > 0) {
                        robot.step(Math.min(dt, 0.001));
                        dt -= 0.001;
                    }
                }
            }
        }, "Physics");
        physics.setDaemon(true);
        physics.start();
    }
}
