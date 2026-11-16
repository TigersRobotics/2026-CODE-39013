package org.firstinspires.ftc.robotcore.internal.opmode;

public class OpModeMeta {
    public enum Flavor { AUTONOMOUS, TELEOP, UTILITY }

    public final String name;
    public final Flavor flavor;

    private OpModeMeta(String name, Flavor flavor) {
        this.name = name;
        this.flavor = flavor;
    }

    public static class Builder {
        private String name = "";
        private Flavor flavor = Flavor.TELEOP;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setFlavor(Flavor flavor) {
            this.flavor = flavor;
            return this;
        }

        public OpModeMeta build() { return new OpModeMeta(name, flavor); }
    }
}
