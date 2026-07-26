package com.vectoros.robot.runtime.navigation.events;

import com.vectoros.robot.runtime.events.RuntimeEvent;
import com.vectoros.robot.runtime.navigation.MovementCommand;

import java.time.Instant;
import java.util.Objects;

public final class MovementStepCompletedEvent implements RuntimeEvent {

    private final String robotId;
    private final MovementCommand command;
    private final Instant occurredAt;

    public MovementStepCompletedEvent(String robotId, MovementCommand command, Instant occurredAt) {
        this.robotId = requireText(robotId, "robotId");
        this.command = Objects.requireNonNull(command, "command");
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
    }

    @Override
    public String robotId() {
        return robotId;
    }

    public MovementCommand command() {
        return command;
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
