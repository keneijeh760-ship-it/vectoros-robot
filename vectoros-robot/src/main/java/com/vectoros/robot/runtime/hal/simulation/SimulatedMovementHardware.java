package com.vectoros.robot.runtime.hal.simulation;

import com.vectoros.robot.runtime.hal.MovementHardware;

import java.util.Objects;

/**
 * Simulation adapter for {@link MovementHardware}.
 */
public final class SimulatedMovementHardware implements MovementHardware {

    private final SimulationContext context;

    SimulatedMovementHardware(SimulationContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    @Override
    public void move(double headingDegrees, double speed) {
        context.move(headingDegrees, speed);
    }

    @Override
    public void stop() {
        context.stop();
    }

    @Override
    public double readSpeed() {
        return context.speed();
    }
}
