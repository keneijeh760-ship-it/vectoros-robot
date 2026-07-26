package com.vectoros.robot.runtime.battery;

import com.vectoros.robot.runtime.hal.BatteryHardware;
import com.vectoros.robot.runtime.model.BatteryState;

import java.util.Objects;

/**
 * Updates battery state through the HAL.
 */
public final class BatteryManager {

    private final BatteryHardware batteryHardware;
    private final double idleDrainPerTick;
    private final double movingDrainPerTick;

    public BatteryManager(
            BatteryHardware batteryHardware,
            double idleDrainPerTick,
            double movingDrainPerTick) {
        this.batteryHardware = Objects.requireNonNull(batteryHardware, "batteryHardware");
        if (idleDrainPerTick < 0 || !Double.isFinite(idleDrainPerTick)) {
            throw new IllegalArgumentException("idleDrainPerTick must be a non-negative finite number");
        }
        if (movingDrainPerTick < 0 || !Double.isFinite(movingDrainPerTick)) {
            throw new IllegalArgumentException("movingDrainPerTick must be a non-negative finite number");
        }
        this.idleDrainPerTick = idleDrainPerTick;
        this.movingDrainPerTick = movingDrainPerTick;
    }

    public BatteryManager(BatteryHardware batteryHardware) {
        this(batteryHardware, 0.01, 0.1);
    }

    /**
     * Apply drain for the current tick and return the updated battery snapshot.
     */
    public BatteryState update(boolean moving) {
        double drain = moving ? movingDrainPerTick : idleDrainPerTick;
        if (drain > 0) {
            batteryHardware.drain(drain);
        }
        return currentState();
    }

    public BatteryState currentState() {
        return new BatteryState(batteryHardware.readPercentage());
    }
}
