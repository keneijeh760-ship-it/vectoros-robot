package com.vectoros.robot.runtime.mission.events;

import com.vectoros.robot.runtime.events.RuntimeEvent;
import com.vectoros.robot.runtime.mission.MissionStepType;

import java.time.Instant;
import java.util.Objects;

public final class MissionStepCompletedEvent implements RuntimeEvent {

    private final String robotId;
    private final String missionId;
    private final String stepId;
    private final MissionStepType stepType;
    private final Instant occurredAt;

    public MissionStepCompletedEvent(
            String robotId,
            String missionId,
            String stepId,
            MissionStepType stepType,
            Instant occurredAt) {
        this.robotId = requireText(robotId, "robotId");
        this.missionId = requireText(missionId, "missionId");
        this.stepId = requireText(stepId, "stepId");
        this.stepType = Objects.requireNonNull(stepType, "stepType");
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
    }

    @Override
    public String robotId() {
        return robotId;
    }

    public String missionId() {
        return missionId;
    }

    public String stepId() {
        return stepId;
    }

    public MissionStepType stepType() {
        return stepType;
    }

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
