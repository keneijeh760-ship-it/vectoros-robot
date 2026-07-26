package com.vectoros.robot.runtime.position;

import com.vectoros.robot.runtime.hal.PositionHardware;
import com.vectoros.robot.runtime.model.Position;

import java.util.Objects;

/**
 * Reads pose from the HAL and exposes it to the runtime.
 */
public final class PositionTracker {

    private final PositionHardware positionHardware;

    public PositionTracker(PositionHardware positionHardware) {
        this.positionHardware = Objects.requireNonNull(positionHardware, "positionHardware");
    }

    public Position currentPosition() {
        return positionHardware.readPosition();
    }

    public double currentHeadingDegrees() {
        return positionHardware.readHeading();
    }
}
