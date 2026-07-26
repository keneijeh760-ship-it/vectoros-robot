package com.vectoros.robot.runtime.hal.simulation;

import com.vectoros.robot.runtime.hal.BatteryHardware;

import java.util.Objects;

/**
 * Simulation adapter for {@link BatteryHardware}.
 */
public final class SimulatedBatteryHardware implements BatteryHardware {

    private final SimulationContext context;

    SimulatedBatteryHardware(SimulationContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    @Override
    public double readPercentage() {
        return context.batteryPercentage();
    }

    @Override
    public void drain(double amount) {
        context.drain(amount);
    }

    @Override
    public void charge(double amount) {
        context.charge(amount);
    }
}
