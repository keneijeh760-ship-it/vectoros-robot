package com.vectoros.robot.runtime.events;

import com.vectoros.robot.runtime.model.BatteryState;

import java.time.Instant;
import java.util.Objects;

public final class BatteryLowEvent implements RuntimeEvent {

    private final String robotId;
    private final BatteryState battery;
    private final Instant occurredAt;

    public BatteryLowEvent(String robotId, BatteryState battery, Instant occurredAt) {
        if (robotId == null || robotId.isBlank()) {
            throw new IllegalArgumentException("robotId must not be blank");
        }
        this.robotId = robotId;
        this.battery = Objects.requireNonNull(battery, "battery");
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
    }

    @Override
    public String robotId() {
        return robotId;
    }

    public BatteryState battery() {
        return battery;
    }

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }
}
