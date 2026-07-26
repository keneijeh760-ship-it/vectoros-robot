package com.vectoros.robot.mqtt;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * JSON serializer for MQTT payloads. No business logic.
 */
public final class MqttMessageSerializer {

    private final ObjectMapper objectMapper;

    public MqttMessageSerializer(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    public static MqttMessageSerializer createDefault() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // Domain message types use private fields + accessor methods (robotId()), not JavaBean getters.
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
        return new MqttMessageSerializer(mapper);
    }

    public byte[] serialize(Object value) {
        try {
            return objectMapper.writeValueAsBytes(value);
        } catch (JsonProcessingException ex) {
            throw new MqttSerializationException(
                    "Failed to serialize MQTT payload: " + value.getClass().getSimpleName(), ex);
        }
    }

    public <T> T deserialize(byte[] payload, Class<T> type) {
        try {
            return objectMapper.readValue(payload, type);
        } catch (IOException ex) {
            String preview = payload == null ? "null" : new String(payload, StandardCharsets.UTF_8);
            throw new MqttSerializationException(
                    "Failed to deserialize MQTT payload into " + type.getSimpleName() + ": " + preview, ex);
        }
    }
}
