package com.vectoros.robot.runtime.events;

import com.vectoros.robot.runtime.model.Position;

import java.time.Instant;
import java.util.Objects;

public final class PositionChangedEvent implements RuntimeEvent {

    private final String robotId;
    private final Position previous;
    private final Position current;
    private final Instant occurredAt;

    public PositionChangedEvent(String robotId, Position previous, Position current, Instant occurredAt) {
        if (robotId == null || robotId.isBlank()) {
            throw new IllegalArgumentException("robotId must not be blank");
        }
        this.robotId = robotId;
        this.previous = Objects.requireNonNull(previous, "previous");
        this.current = Objects.requireNonNull(current, "current");
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
    }

    @Override
    public String robotId() {
        return robotId;
    }

    public Position previous() {
        return previous;
    }

    public Position current() {
        return current;
    }

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }
}
