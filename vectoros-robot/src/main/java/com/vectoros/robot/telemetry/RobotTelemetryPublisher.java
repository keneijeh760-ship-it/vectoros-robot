package com.vectoros.robot.telemetry;

import com.vectoros.robot.messaging.RobotEventPublisher;

import java.util.Objects;

/**
 * Publishes telemetry snapshots. Transport is delegated entirely to
 * {@link RobotEventPublisher}; this class never touches MQTT.
 */
public final class RobotTelemetryPublisher {

    private final RobotEventPublisher eventPublisher;

    public RobotTelemetryPublisher(RobotEventPublisher eventPublisher) {
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher");
    }

    public void publish(RobotTelemetrySnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        eventPublisher.publishTelemetry(snapshot);
    }
}
