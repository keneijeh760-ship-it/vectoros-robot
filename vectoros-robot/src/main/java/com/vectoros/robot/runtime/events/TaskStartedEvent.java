package com.vectoros.robot.runtime.events;

import java.time.Instant;
import java.util.Objects;

public final class TaskStartedEvent implements RuntimeEvent {

    private final String robotId;
    private final String taskId;
    private final Instant occurredAt;

    public TaskStartedEvent(String robotId, String taskId, Instant occurredAt) {
        this.robotId = requireText(robotId, "robotId");
        this.taskId = requireText(taskId, "taskId");
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
    }

    @Override
    public String robotId() {
        return robotId;
    }

    public String taskId() {
        return taskId;
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
