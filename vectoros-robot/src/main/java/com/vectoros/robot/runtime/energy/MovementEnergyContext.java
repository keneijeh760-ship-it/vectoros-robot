package com.vectoros.robot.runtime.energy;

/**
 * Context for energy consumption calculations.
 * Sprint 05 uses fixed-step consumption; speed/payload/terrain are reserved for later.
 */
public final class MovementEnergyContext {

    private final double speed;
    private final double payloadKg;
    private final String terrain;

    public MovementEnergyContext(double speed, double payloadKg, String terrain) {
        this.speed = speed;
        this.payloadKg = payloadKg;
        this.terrain = terrain == null ? "DEFAULT" : terrain;
    }

    public static MovementEnergyContext simpleStep(double speed) {
        return new MovementEnergyContext(speed, 0.0, "DEFAULT");
    }

    public double speed() {
        return speed;
    }

    public double payloadKg() {
        return payloadKg;
    }

    public String terrain() {
        return terrain;
    }
}
