package org.firstinspires.ftc.teamcode.controller;


import com.qualcomm.robotcore.hardware.Gamepad;

import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public class Binding {
    public interface Axis extends ToDoubleFunction<Gamepad> {}
    public interface Button extends Predicate<Gamepad> {}
}
