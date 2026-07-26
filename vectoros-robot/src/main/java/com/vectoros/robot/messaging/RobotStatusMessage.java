package com.vectoros.robot.messaging;

import java.time.Instant;
import java.util.Objects;

public final class RobotStatusMessage {

    private final String robotId;
    private final String status;
    private final Instant occurredAt;

    public RobotStatusMessage(String robotId, String status, Instant occurredAt) {
        this.robotId = requireText(robotId, "robotId");
        this.status = requireText(status, "status");
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
    }

    public String robotId() {
        return robotId;
    }

    public String status() {
        return status;
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
