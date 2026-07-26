package com.vectoros.robot.runtime.navigation.events;

import com.vectoros.robot.runtime.events.RuntimeEvent;

import java.time.Instant;
import java.util.Objects;

public final class NavigationFailedEvent implements RuntimeEvent {

    private final String robotId;
    private final String reason;
    private final Instant occurredAt;

    public NavigationFailedEvent(String robotId, String reason, Instant occurredAt) {
        this.robotId = requireText(robotId, "robotId");
        this.reason = requireText(reason, "reason");
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
    }

    @Override
    public String robotId() {
        return robotId;
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
