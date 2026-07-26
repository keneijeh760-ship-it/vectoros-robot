package com.vectoros.robot.messaging;

import java.time.Instant;
import java.util.Objects;

public final class RobotMissionMessage {

    public enum EventType {
        STARTED,
        COMPLETED,
        CANCELLED,
        FAILED
    }

    private final String robotId;
    private final String missionId;
    private final EventType eventType;
    private final Instant occurredAt;

    public RobotMissionMessage(String robotId, String missionId, EventType eventType, Instant occurredAt) {
        this.robotId = requireText(robotId, "robotId");
        this.missionId = requireText(missionId, "missionId");
        this.eventType = Objects.requireNonNull(eventType, "eventType");
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
    }

    public String robotId() {
        return robotId;
    }

    public String missionId() {
        return missionId;
    }

    public EventType eventType() {
        return eventType;
    }

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
