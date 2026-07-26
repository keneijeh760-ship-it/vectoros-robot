package com.vectoros.robot.runtime.mission;

import java.util.Objects;
import java.util.Optional;

/**
 * Outcome of one {@link MissionManager} tick.
 */
public final class MissionResult {

    public enum Status {
        IDLE,
        IN_PROGRESS,
        STEP_COMPLETED,
        COMPLETED,
        FAILED,
        CANCELLED
    }

    private final Status status;
    private final Mission mission;
    private final MissionStep step;
    private final String message;

    public MissionResult(Status status, Mission mission, MissionStep step, String message) {
        this.status = Objects.requireNonNull(status, "status");
        this.mission = mission;
        this.step = step;
        this.message = message;
    }

    public static MissionResult idle() {
        return new MissionResult(Status.IDLE, null, null, null);
    }

    public static MissionResult inProgress(Mission mission, MissionStep step) {
        return new MissionResult(Status.IN_PROGRESS, mission, step, null);
    }

    public static MissionResult stepCompleted(Mission mission, MissionStep step) {
        return new MissionResult(Status.STEP_COMPLETED, mission, step, null);
    }

    public static MissionResult completed(Mission mission) {
        return new MissionResult(Status.COMPLETED, mission, null, null);
    }

    public static MissionResult failed(Mission mission, String message) {
        return new MissionResult(Status.FAILED, mission, null, message);
    }

    public static MissionResult cancelled(Mission mission) {
        return new MissionResult(Status.CANCELLED, mission, null, null);
    }

    public Status status() {
        return status;
    }

    public Optional<Mission> mission() {
        return Optional.ofNullable(mission);
    }

    public Optional<MissionStep> step() {
        return Optional.ofNullable(step);
    }

    public Optional<String> message() {
        return Optional.ofNullable(message);
    }
}
