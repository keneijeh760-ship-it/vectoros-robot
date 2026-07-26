package com.vectoros.robot.runtime.navigation.events;

import com.vectoros.robot.runtime.events.RuntimeEvent;
import com.vectoros.robot.runtime.world.Coordinate;

import java.time.Instant;
import java.util.Objects;

public final class NavigationStartedEvent implements RuntimeEvent {

    private final String robotId;
    private final Coordinate destination;
    private final Instant occurredAt;

    public NavigationStartedEvent(String robotId, Coordinate destination, Instant occurredAt) {
        this.robotId = requireText(robotId, "robotId");
        this.destination = Objects.requireNonNull(destination, "destination");
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
    }

    @Override
    public String robotId() {
        return robotId;
    }

    public Coordinate destination() {
        return destination;
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
