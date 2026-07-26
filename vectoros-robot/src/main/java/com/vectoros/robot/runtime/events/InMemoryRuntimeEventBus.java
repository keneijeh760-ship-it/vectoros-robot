package com.vectoros.robot.runtime.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory event bus for deterministic unit testing and local runtime use.
 */
public final class InMemoryRuntimeEventBus implements RuntimeEventPublisher {

    private final List<RuntimeEventListener> listeners = new CopyOnWriteArrayList<>();
    private final List<RuntimeEvent> history = new ArrayList<>();

    public void addListener(RuntimeEventListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    public void removeListener(RuntimeEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void publish(RuntimeEvent event) {
        Objects.requireNonNull(event, "event");
        history.add(event);
        for (RuntimeEventListener listener : listeners) {
            listener.onEvent(event);
        }
    }

    public List<RuntimeEvent> history() {
        return Collections.unmodifiableList(history);
    }

    public void clearHistory() {
        history.clear();
    }

    public <T extends RuntimeEvent> List<T> historyOfType(Class<T> type) {
        Objects.requireNonNull(type, "type");
        List<T> matched = new ArrayList<>();
        for (RuntimeEvent event : history) {
            if (type.isInstance(event)) {
                matched.add(type.cast(event));
            }
        }
        return Collections.unmodifiableList(matched);
    }
}
