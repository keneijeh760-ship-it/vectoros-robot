package com.vectoros.robot.runtime.mission;

/**
 * Unit of work within a mission.
 * {@link #NAVIGATE} and {@link #WAIT} are implemented in Sprint 04.
 * Remaining values are reserved extension points.
 */
public enum MissionStepType {
    NAVIGATE,
    WAIT,
    PICKUP,
    DROPOFF,
    DOCK,
    CHARGE
}
