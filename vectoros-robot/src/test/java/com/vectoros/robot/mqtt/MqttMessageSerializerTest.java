package com.vectoros.robot.mqtt;

import com.vectoros.robot.messaging.RobotBatteryMessage;
import com.vectoros.robot.messaging.RobotMissionMessage;
import com.vectoros.robot.messaging.RobotPositionMessage;
import com.vectoros.robot.messaging.RobotStatusMessage;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MqttMessageSerializerTest {

    private final MqttMessageSerializer serializer = MqttMessageSerializer.createDefault();
    private static final Instant NOW = Instant.parse("2026-07-26T12:00:00Z");

    @Test
    void serializesStatusMessageAsJson() {
        byte[] bytes = serializer.serialize(new RobotStatusMessage("r1", "IDLE", NOW));
        String json = new String(bytes, StandardCharsets.UTF_8);

        assertThat(json).contains("\"robotId\":\"r1\"");
        assertThat(json).contains("\"status\":\"IDLE\"");
        assertThat(json).contains("2026-07-26T12:00:00Z");
    }

    @Test
    void roundTripsCommandPayload() {
        RobotCommandPayload payload = new RobotCommandPayload();
        payload.setType("ASSIGN_MISSION");
        payload.setMissionId("m-1");
        RobotCommandPayload.MissionStepPayload step = new RobotCommandPayload.MissionStepPayload();
        step.setStepId("s1");
        step.setType("NAVIGATE");
        step.setX(3);
        step.setY(4);
        payload.getSteps().add(step);

        byte[] bytes = serializer.serialize(payload);
        RobotCommandPayload decoded = serializer.deserialize(bytes, RobotCommandPayload.class);

        assertThat(decoded.getType()).isEqualTo("ASSIGN_MISSION");
        assertThat(decoded.getMissionId()).isEqualTo("m-1");
        assertThat(decoded.getSteps()).hasSize(1);
        assertThat(decoded.getSteps().getFirst().getX()).isEqualTo(3);
        assertThat(decoded.getSteps().getFirst().getY()).isEqualTo(4);
    }

    @Test
    void serializesMissionBatteryAndPositionMessages() {
        assertThat(new String(serializer.serialize(
                new RobotMissionMessage("r1", "m1", RobotMissionMessage.EventType.COMPLETED, NOW)),
                StandardCharsets.UTF_8))
                .contains("\"eventType\":\"COMPLETED\"");

        assertThat(new String(serializer.serialize(
                new RobotBatteryMessage("r1", 88.5, "NORMAL", NOW)), StandardCharsets.UTF_8))
                .contains("\"percentage\":88.5");

        assertThat(new String(serializer.serialize(
                new RobotPositionMessage("r1", 1.0, 2.0, "NORTH", NOW)), StandardCharsets.UTF_8))
                .contains("\"heading\":\"NORTH\"");
    }

    @Test
    void deserializeInvalidJsonThrows() {
        assertThatThrownBy(() -> serializer.deserialize("not-json".getBytes(StandardCharsets.UTF_8), RobotCommandPayload.class))
                .isInstanceOf(MqttSerializationException.class);
    }
}
