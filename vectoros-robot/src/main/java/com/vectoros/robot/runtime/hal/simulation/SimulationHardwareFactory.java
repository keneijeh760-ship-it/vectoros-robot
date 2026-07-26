package com.vectoros.robot.runtime.hal.simulation;

import com.vectoros.robot.runtime.hal.BatteryHardware;
import com.vectoros.robot.runtime.hal.MovementHardware;
import com.vectoros.robot.runtime.hal.PositionHardware;
import com.vectoros.robot.runtime.model.Position;

/**
 * Factory that wires a coherent simulation HAL bundle sharing one {@link SimulationContext}.
 * Future firmware factories can expose the same three HAL interfaces without this type.
 */
public final class SimulationHardwareFactory {

    private final MovementHardware movementHardware;
    private final BatteryHardware batteryHardware;
    private final PositionHardware positionHardware;

    private SimulationHardwareFactory(SimulationContext context) {
        this.movementHardware = new SimulatedMovementHardware(context);
        this.batteryHardware = new SimulatedBatteryHardware(context);
        this.positionHardware = new SimulatedPositionHardware(context);
    }

    public static SimulationHardwareFactory createDefault() {
        return new SimulationHardwareFactory(SimulationContext.defaults());
    }

    public static SimulationHardwareFactory create(Position initialPosition, double headingDegrees, double batteryPercentage) {
        return new SimulationHardwareFactory(
                new SimulationContext(initialPosition, headingDegrees, batteryPercentage));
    }

    public MovementHardware movementHardware() {
        return movementHardware;
    }

    public BatteryHardware batteryHardware() {
        return batteryHardware;
    }

    public PositionHardware positionHardware() {
        return positionHardware;
    }
}
