package com.vectoros.robot.runtime.energy.events;

import com.vectoros.robot.runtime.energy.BatteryModel;
import com.vectoros.robot.runtime.events.RuntimeEvent;

import java.time.Instant;
import java.util.Objects;

public final class BatteryLowEvent implements RuntimeEvent {

    private final String robotId;
    private final BatteryModel battery;
    private final Instant occurredAt;

    public BatteryLowEvent(String robotId, BatteryModel battery, Instant occurredAt) {
        this.robotId = requireText(robotId, "robotId");
        this.battery = Objects.requireNonNull(battery, "battery");
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
    }

    @Override
    public String robotId() {
        return robotId;
    }

    public BatteryModel battery() {
        return battery;
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
