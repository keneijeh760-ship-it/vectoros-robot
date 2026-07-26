package com.vectoros.robot.runtime.mission.events;

import com.vectoros.robot.runtime.events.RuntimeEvent;

import java.time.Instant;
import java.util.Objects;

public final class MissionFailedEvent implements RuntimeEvent {

    private final String robotId;
    private final String missionId;
    private final String reason;
    private final Instant occurredAt;

    public MissionFailedEvent(String robotId, String missionId, String reason, Instant occurredAt) {
        this.robotId = requireText(robotId, "robotId");
        this.missionId = requireText(missionId, "missionId");
        this.reason = requireText(reason, "reason");
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
    }

    @Override
    public String robotId() {
        return robotId;
    }

    public String missionId() {
        return missionId;
    }

    public String reason() {
        return reason;
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
