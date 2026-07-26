package com.vectoros.robot.runtime.mission;

/**
 * Lifecycle status of a mission.
 */
public enum MissionStatus {
    CREATED,
    QUEUED,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED
}
