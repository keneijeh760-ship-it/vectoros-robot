package com.vectoros.robot.runtime.hal;

import com.vectoros.robot.runtime.model.Position;

/**
 * Hardware capability for localization / pose sensing.
 */
public interface PositionHardware {

    /**
     * @return latest known position from sensors or simulation
     */
    Position readPosition();

    /**
     * @return latest known heading in degrees
     */
    double readHeading();
}
