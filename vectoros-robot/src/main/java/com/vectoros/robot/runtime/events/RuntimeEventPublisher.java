package com.vectoros.robot.runtime.events;

/**
 * Publishes internal runtime domain events.
 * Implementations must not perform MQTT or network I/O in Sprint 01.
 */
public interface RuntimeEventPublisher {

    void publish(RuntimeEvent event);
}
