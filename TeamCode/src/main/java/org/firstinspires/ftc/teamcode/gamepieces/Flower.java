package org.firstinspires.ftc.teamcode.gamepieces;

public class Flower extends GamePiece{

    public Ball[] fill;

    public Flower(int x, int y, double cmDistance, Ball[] fill) {
        super(x, y, cmDistance);
        this.fill = fill;
    }
}
