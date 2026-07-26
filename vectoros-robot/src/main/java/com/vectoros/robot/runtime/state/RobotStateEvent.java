package com.vectoros.robot.runtime.state;

/**
 * Domain triggers that request a robot status transition.
 * New events can be added without changing the core machine loop.
 */
public enum RobotStateEvent {
    POWER_ON,
    INITIALIZATION_COMPLETE,
    TASK_RECEIVED,
    START_MOVE_TO_PICKUP,
    ARRIVED_AT_PICKUP,
    LOADING_COMPLETE,
    ARRIVED_AT_DROPOFF,
    UNLOADING_COMPLETE,
    RETURN_COMPLETE,
    START_CHARGING,
    CHARGING_COMPLETE,
    FAULT_DETECTED,
    ERROR_CLEARED,
    GO_OFFLINE
}
