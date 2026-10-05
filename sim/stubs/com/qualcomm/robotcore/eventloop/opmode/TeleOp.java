package com.qualcomm.robotcore.eventloop.opmode;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// Sim stand-in for the FTC SDK annotation
@Retention(RetentionPolicy.RUNTIME)
public @interface TeleOp {
    String name() default "";
    String group() default "";
}
