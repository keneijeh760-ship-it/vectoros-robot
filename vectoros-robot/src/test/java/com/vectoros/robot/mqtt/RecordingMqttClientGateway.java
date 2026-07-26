package com.vectoros.robot.mqtt;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Test double that records publishes and delivers subscriptions in-process.
 */
public final class RecordingMqttClientGateway implements MqttClientGateway {

    public record PublishedMessage(String topic, byte[] payload) {
        public String payloadAsUtf8() {
            return new String(payload, StandardCharsets.UTF_8);
        }
    }

    private final List<PublishedMessage> published = new ArrayList<>();
    private final Map<String, MqttMessageListener> subscriptions = new LinkedHashMap<>();

    @Override
    public void publish(String topic, byte[] payload) {
        published.add(new PublishedMessage(
                Objects.requireNonNull(topic, "topic"),
                Objects.requireNonNull(payload, "payload").clone()));
    }

    @Override
    public void subscribe(String topic, MqttMessageListener listener) {
        subscriptions.put(
                Objects.requireNonNull(topic, "topic"),
                Objects.requireNonNull(listener, "listener"));
    }

    public List<PublishedMessage> published() {
        return Collections.unmodifiableList(published);
    }

    public Map<String, MqttMessageListener> subscriptions() {
        return Collections.unmodifiableMap(subscriptions);
    }

    public void deliver(String topic, byte[] payload) {
        MqttMessageListener listener = subscriptions.get(topic);
        if (listener == null) {
            throw new IllegalStateException("No subscription for topic: " + topic);
        }
        listener.onMessage(topic, payload);
    }

    public void clear() {
        published.clear();
    }
}
