package org.firstinspires.ftc.teamcode.gamepieces;

import org.firstinspires.ftc.teamcode.gamepieces.Colours.Colour;
public class Hive extends GamePiece {
    public double cmHeight;

    public final Colour colour;

    public final Tilt tilt;
    public Ball[] fill;
    public enum Tilt {
        TOWARDS,
        AWAY
    }
    public Hive(int x, int y, double cmDistance, double cmHeight, Colour colour, Tilt tilt, Ball[] fill) {
        super(x, y, cmDistance);
        this.cmHeight = cmHeight;
        this.colour = colour;
        this.tilt = tilt;
    }


}
