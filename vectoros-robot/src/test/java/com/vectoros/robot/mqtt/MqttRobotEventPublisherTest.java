package com.vectoros.robot.mqtt;

import com.vectoros.robot.messaging.RobotBatteryMessage;
import com.vectoros.robot.messaging.RobotMissionMessage;
import com.vectoros.robot.messaging.RobotPositionMessage;
import com.vectoros.robot.messaging.RobotStatusMessage;
import com.vectoros.robot.runtime.mission.MissionStatus;
import com.vectoros.robot.runtime.model.RobotStatus;
import com.vectoros.robot.runtime.navigation.Heading;
import com.vectoros.robot.runtime.world.Coordinate;
import com.vectoros.robot.telemetry.RobotTelemetrySnapshot;
import com.vectoros.robot.telemetry.RobotTelemetryType;
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

    @Test
    void publishesTelemetrySnapshotToTelemetryTopic() {
        publisher.publishTelemetry(new RobotTelemetrySnapshot(
                "bot-1",
                NOW,
                RobotStatus.MOVING_TO_PICKUP,
                MissionStatus.RUNNING,
                77.0,
                new Coordinate(4, 5),
                Heading.NORTH,
                RobotTelemetryType.PERIODIC));

        assertThat(gateway.published()).hasSize(1);
        assertThat(gateway.published().getFirst().topic()).isEqualTo("robot/bot-1/events/telemetry");
        assertThat(gateway.published().getFirst().payloadAsUtf8())
                .contains("\"robotId\":\"bot-1\"")
                .contains("\"robotStatus\":\"MOVING_TO_PICKUP\"")
                .contains("\"missionStatus\":\"RUNNING\"")
                .contains("\"batteryPercentage\":77.0")
                .contains("\"heading\":\"NORTH\"")
                .contains("\"type\":\"PERIODIC\"");
    }
}
