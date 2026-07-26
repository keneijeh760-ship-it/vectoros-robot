package com.vectoros.robot.runtime.hal;

/**
 * Hardware capability for commanding locomotion.
 * Implementations may drive simulated kinematics or real motor controllers.
 */
public interface MovementHardware {

    /**
     * Command motion at the given heading and speed for the next control interval.
     *
     * @param headingDegrees direction of travel (0° = +X, 90° = +Y)
     * @param speed          non-negative speed in distance units per tick
     */
    void move(double headingDegrees, double speed);

    /**
     * Stop all motion.
     */
    void stop();

    /**
     * @return current commanded/actual speed from the hardware layer
     */
    double readSpeed();
}
