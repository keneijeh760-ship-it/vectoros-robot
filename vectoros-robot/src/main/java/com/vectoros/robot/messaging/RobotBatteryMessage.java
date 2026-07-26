package com.vectoros.robot.messaging;

import java.time.Instant;
import java.util.Objects;

public final class RobotBatteryMessage {

    private final String robotId;
    private final double percentage;
    private final String batteryStatus;
    private final Instant occurredAt;

    public RobotBatteryMessage(String robotId, double percentage, String batteryStatus, Instant occurredAt) {
        this.robotId = requireText(robotId, "robotId");
        if (!Double.isFinite(percentage)) {
            throw new IllegalArgumentException("percentage must be finite");
        }
        this.percentage = percentage;
        this.batteryStatus = requireText(batteryStatus, "batteryStatus");
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
    }

    public String robotId() {
        return robotId;
    }

    public double percentage() {
        return percentage;
    }

    public String batteryStatus() {
        return batteryStatus;
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
