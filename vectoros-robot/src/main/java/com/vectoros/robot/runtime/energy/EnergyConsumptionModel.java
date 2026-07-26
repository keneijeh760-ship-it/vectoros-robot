package com.vectoros.robot.runtime.energy;

/**
 * Determines how much energy a successful movement step consumes.
 * Implementations are replaceable for speed/payload/terrain-aware models later.
 */
public interface EnergyConsumptionModel {

    /**
     * @return energy to consume in percentage points for one successful movement step
     */
    double consumptionForMovementStep(MovementEnergyContext context);
}
