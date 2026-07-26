package com.vectoros.robot.mqtt;

/**
 * Thrown when MQTT payload serialization/deserialization fails.
 */
public final class MqttSerializationException extends RuntimeException {

    public MqttSerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public MqttSerializationException(String message) {
        super(message);
    }
}
