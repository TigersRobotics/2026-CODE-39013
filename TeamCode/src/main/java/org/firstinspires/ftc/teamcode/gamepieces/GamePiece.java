package org.firstinspires.ftc.teamcode.gamepieces;


/**
The main gamepiece class, which is what the auton will store
 */
public class GamePiece {
    public static enum Colour {
        BLUE,
        RED
    }

    /**
     * The x and y coordinates of the GamePiece in the camera(not the robot/field)
     */
    public final int x, y;
    public final double cmDistance;
    public GamePiece(int x, int y, double cmDistance) {
        this.x = x;
        this.y = y;
        this.cmDistance = cmDistance;
    }

}

/**
A sorter thing so that you cna make an array that only fits ball types(nectar and pollen)
*/
interface Ball {}

class Hive extends GamePiece {
    public double cmHeight;

    public final Colour colour;

    public final Tilt tilt;

    public final double angle;

    public Ball[] fill;
    public enum Tilt {
        TOWARDS,
        AWAY
    }
    public Hive(int x, int y, double cmDistance, double cmHeight, Colour colour, Tilt tilt, Ball[] fill, double angle) {
        super(x, y, cmDistance);
        this.cmHeight = cmHeight;
        this.colour = colour;
        this.tilt = tilt;
        this.angle = angle;
        this.fill = fill;
    }


}

class Nectar extends GamePiece implements Ball{

    public final Colour colour;
    public Nectar(int x, int y, double cmDistance, Colour colour) {
        super(x, y, cmDistance);
        this.colour=colour;
    }
}

class Flower extends GamePiece{

    public Ball[] fill;

    public Flower(int x, int y, double cmDistance, Ball[] fill) {
        super(x, y, cmDistance);
        this.fill = fill;
    }
}

class Pollen extends GamePiece implements Ball{
    public Pollen(int x, int y, double cmDistance) {
        super(x, y, cmDistance);
    }

}

