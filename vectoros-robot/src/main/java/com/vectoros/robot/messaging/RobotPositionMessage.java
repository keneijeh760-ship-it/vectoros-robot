package com.vectoros.robot.messaging;

import java.time.Instant;
import java.util.Objects;

public final class RobotPositionMessage {

    private final String robotId;
    private final double x;
    private final double y;
    private final String heading;
    private final Instant occurredAt;

    public RobotPositionMessage(String robotId, double x, double y, String heading, Instant occurredAt) {
        this.robotId = requireText(robotId, "robotId");
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException("coordinates must be finite");
        }
        this.x = x;
        this.y = y;
        this.heading = requireText(heading, "heading");
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
    }

    public String robotId() {
        return robotId;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public String heading() {
        return heading;
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
