package com.vectoros.robot.runtime.energy;

import java.util.Objects;

/**
 * Deterministic fixed-cost consumption per successful movement step.
 */
public final class FixedStepEnergyConsumptionModel implements EnergyConsumptionModel {

    private final double energyPerStep;

    public FixedStepEnergyConsumptionModel(double energyPerStep) {
        if (!Double.isFinite(energyPerStep) || energyPerStep < 0) {
            throw new IllegalArgumentException("energyPerStep must be a non-negative finite number");
        }
        this.energyPerStep = energyPerStep;
    }

    /**
     * Default: 1.0 percentage point per grid movement step.
     */
    public FixedStepEnergyConsumptionModel() {
        this(1.0);
    }

    public double energyPerStep() {
        return energyPerStep;
    }

    @Override
    public double consumptionForMovementStep(MovementEnergyContext context) {
        Objects.requireNonNull(context, "context");
        return energyPerStep;
    }
}
