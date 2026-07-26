package com.vectoros.robot.runtime.events;

import java.time.Instant;

/**
 * Marker for internal runtime domain events.
 * These are not MQTT payloads.
 */
public interface RuntimeEvent {

    String robotId();

    Instant occurredAt();
}
