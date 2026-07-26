package com.vectoros.robot.runtime.model;

/**
 * Operational status of a warehouse robot.
 * Transitions are owned exclusively by {@code RobotStateMachine}.
 */
public enum RobotStatus {
    INITIALIZING,
    IDLE,
    TASK_ASSIGNED,
    MOVING_TO_PICKUP,
    LOADING,
    MOVING_TO_DROPOFF,
    UNLOADING,
    RETURNING,
    CHARGING,
    ERROR,
    OFFLINE
}
