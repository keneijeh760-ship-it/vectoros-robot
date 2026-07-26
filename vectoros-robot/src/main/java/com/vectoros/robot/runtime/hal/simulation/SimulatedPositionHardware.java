package com.vectoros.robot.runtime.hal.simulation;

import com.vectoros.robot.runtime.hal.PositionHardware;
import com.vectoros.robot.runtime.model.Position;

import java.util.Objects;

/**
 * Simulation adapter for {@link PositionHardware}.
 */
public final class SimulatedPositionHardware implements PositionHardware {

    private final SimulationContext context;

    SimulatedPositionHardware(SimulationContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    @Override
    public Position readPosition() {
        return context.position();
    }

    @Override
    public double readHeading() {
        return context.headingDegrees();
    }
}
