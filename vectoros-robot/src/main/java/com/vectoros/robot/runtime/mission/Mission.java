package com.vectoros.robot.runtime.mission;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Ordered robot objective. Step list and identity are immutable;
 * lifecycle fields are updated only through controlled mutators used by {@link MissionManager}.
 */
public final class Mission {

    private final String missionId;
    private final List<MissionStep> steps;
    private final Instant createdAt;

    private MissionStatus status;
    private Instant completedAt;
    private int currentStepIndex;

    public Mission(String missionId, List<MissionStep> steps, Instant createdAt) {
        if (missionId == null || missionId.isBlank()) {
            throw new IllegalArgumentException("missionId must not be blank");
        }
        Objects.requireNonNull(steps, "steps");
        if (steps.isEmpty()) {
            throw new IllegalArgumentException("mission must contain at least one step");
        }
        this.missionId = missionId;
        this.steps = List.copyOf(steps);
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.status = MissionStatus.CREATED;
        this.completedAt = null;
        this.currentStepIndex = 0;
    }

    public static Mission of(String missionId, Instant createdAt, MissionStep... steps) {
        List<MissionStep> list = new ArrayList<>();
        Collections.addAll(list, steps);
        return new Mission(missionId, list, createdAt);
    }

    public String missionId() {
        return missionId;
    }

    public List<MissionStep> steps() {
        return steps;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public MissionStatus status() {
        return status;
    }

    public Optional<Instant> completedAt() {
        return Optional.ofNullable(completedAt);
    }

    public int currentStepIndex() {
        return currentStepIndex;
    }

    public Optional<MissionStep> currentStep() {
        if (currentStepIndex < 0 || currentStepIndex >= steps.size()) {
            return Optional.empty();
        }
        return Optional.of(steps.get(currentStepIndex));
    }

    public boolean hasMoreSteps() {
        return currentStepIndex < steps.size();
    }

    public boolean isTerminal() {
        return status == MissionStatus.COMPLETED
                || status == MissionStatus.FAILED
                || status == MissionStatus.CANCELLED;
    }

    public boolean isActive() {
        return status == MissionStatus.RUNNING || status == MissionStatus.QUEUED;
    }

    void markQueued() {
        requireStatus(MissionStatus.CREATED);
        this.status = MissionStatus.QUEUED;
    }

    void markRunning() {
        if (status != MissionStatus.CREATED && status != MissionStatus.QUEUED) {
            throw new IllegalStateException("Cannot start mission from status " + status);
        }
        this.status = MissionStatus.RUNNING;
        this.currentStepIndex = 0;
    }

    void advanceStep() {
        requireStatus(MissionStatus.RUNNING);
        currentStepIndex++;
    }

    void markCompleted(Instant at) {
        requireStatus(MissionStatus.RUNNING);
        this.status = MissionStatus.COMPLETED;
        this.completedAt = Objects.requireNonNull(at, "at");
    }

    void markFailed(Instant at) {
        if (isTerminal()) {
            throw new IllegalStateException("Mission already terminal: " + status);
        }
        this.status = MissionStatus.FAILED;
        this.completedAt = Objects.requireNonNull(at, "at");
    }

    void markCancelled(Instant at) {
        if (isTerminal()) {
            throw new IllegalStateException("Mission already terminal: " + status);
        }
        this.status = MissionStatus.CANCELLED;
        this.completedAt = Objects.requireNonNull(at, "at");
    }

    private void requireStatus(MissionStatus expected) {
        if (status != expected) {
            throw new IllegalStateException(
                    "Expected mission status " + expected + " but was " + status);
        }
    }
}
