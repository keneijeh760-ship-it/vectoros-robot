package com.vectoros.robot.runtime.navigation;

/**
 * High-level motion primitive requested by navigation.
 * Extend with new values without changing {@code RobotEngine}.
 */
public enum MovementType {
    MOVE_FORWARD,
    TURN_LEFT,
    TURN_RIGHT,
    STOP
}
