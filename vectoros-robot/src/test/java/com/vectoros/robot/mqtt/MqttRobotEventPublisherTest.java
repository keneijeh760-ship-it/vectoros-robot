package com.vectoros.robot.mqtt;

import com.vectoros.robot.messaging.RobotBatteryMessage;
import com.vectoros.robot.messaging.RobotMissionMessage;
import com.vectoros.robot.messaging.RobotPositionMessage;
import com.vectoros.robot.messaging.RobotStatusMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class MqttRobotEventPublisherTest {

    private static final Instant NOW = Instant.parse("2026-07-26T13:00:00Z");

    private RecordingMqttClientGateway gateway;
    private MqttRobotEventPublisher publisher;

    @BeforeEach
    void setUp() {
        gateway = new RecordingMqttClientGateway();
        publisher = new MqttRobotEventPublisher(
                "bot-1",
                gateway,
                MqttMessageSerializer.createDefault(),
                RobotMqttTopicConfig.defaults());
    }

    @Test
    void publishesEachEventTypeToConfiguredTopic() {
        publisher.publishStatus(new RobotStatusMessage("bot-1", "IDLE", NOW));
        publisher.publishMission(new RobotMissionMessage(
                "bot-1", "m-1", RobotMissionMessage.EventType.STARTED, NOW));
        publisher.publishBattery(new RobotBatteryMessage("bot-1", 90.0, "NORMAL", NOW));
        publisher.publishPosition(new RobotPositionMessage("bot-1", 1, 2, "EAST", NOW));

        assertThat(gateway.published()).hasSize(4);
        assertThat(gateway.published().get(0).topic()).isEqualTo("robot/bot-1/events/status");
        assertThat(gateway.published().get(1).topic()).isEqualTo("robot/bot-1/events/mission");
        assertThat(gateway.published().get(2).topic()).isEqualTo("robot/bot-1/events/battery");
        assertThat(gateway.published().get(3).topic()).isEqualTo("robot/bot-1/events/position");

        assertThat(gateway.published().get(0).payloadAsUtf8())
                .contains("\"status\":\"IDLE\"")
                .doesNotContain("business");
        assertThat(new String(gateway.published().get(1).payload(), StandardCharsets.UTF_8))
                .contains("\"eventType\":\"STARTED\"");
    }
}
