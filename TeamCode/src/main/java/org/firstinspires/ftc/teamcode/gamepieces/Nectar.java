package org.firstinspires.ftc.teamcode.gamepieces;

import org.firstinspires.ftc.teamcode.gamepieces.Colours.Colour;

public class Nectar extends GamePiece implements Ball{

    public final Colour colour;
    public Nectar(int x, int y, double cmDistance, Colour colour) {
        super(x, y, cmDistance);
        this.colour=colour;
    }
}
