package com.vectoros.robot.runtime.state;

import com.vectoros.robot.runtime.model.RobotStatus;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable record of a validated status change.
 */
public final class RobotStateTransition {

    private final RobotStatus from;
    private final RobotStatus to;
    private final RobotStateEvent event;
    private final Instant occurredAt;

    public RobotStateTransition(
            RobotStatus from,
            RobotStatus to,
            RobotStateEvent event,
            Instant occurredAt) {
        this.from = Objects.requireNonNull(from, "from");
        this.to = Objects.requireNonNull(to, "to");
        this.event = Objects.requireNonNull(event, "event");
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
    }

    public RobotStatus from() {
        return from;
    }

    public RobotStatus to() {
        return to;
    }

    public RobotStateEvent event() {
        return event;
    }

    public Instant occurredAt() {
        return occurredAt;
    }

    @Override
    public String toString() {
        return "RobotStateTransition{"
                + "from=" + from
                + ", to=" + to
                + ", event=" + event
                + ", occurredAt=" + occurredAt
                + '}';
    }
}
