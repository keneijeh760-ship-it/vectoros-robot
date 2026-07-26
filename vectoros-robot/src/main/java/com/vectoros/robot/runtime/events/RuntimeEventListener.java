package com.vectoros.robot.runtime.events;

/**
 * Listener for internal runtime domain events.
 */
@FunctionalInterface
public interface RuntimeEventListener {

    void onEvent(RuntimeEvent event);
}
