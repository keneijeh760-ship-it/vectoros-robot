package com.vectoros.robot.messaging;

import com.vectoros.robot.mqtt.MqttMessageSerializer;
import com.vectoros.robot.mqtt.MqttRobotCommandReceiver;
import com.vectoros.robot.mqtt.MqttRobotEventPublisher;
import com.vectoros.robot.mqtt.RecordingMqttClientGateway;
import com.vectoros.robot.mqtt.RobotMqttTopicConfig;
import com.vectoros.robot.runtime.engine.RobotEngine;
import com.vectoros.robot.runtime.events.InMemoryRuntimeEventBus;
import com.vectoros.robot.runtime.hal.simulation.SimulationHardwareFactory;
import com.vectoros.robot.runtime.mission.MissionStatus;
import com.vectoros.robot.runtime.world.WarehouseWorld;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class MqttRuntimeBridgeIntegrationTest {

    @Test
    void mqttAssignCommandFlowsThroughReceiverToMissionManager() {
        SimulationHardwareFactory hardware = SimulationHardwareFactory.createDefault();
        InMemoryRobotEventPublisher robotEvents = new InMemoryRobotEventPublisher();
        RobotEngine engine = RobotEngine.create(
                "bridge-robot",
                hardware.movementHardware(),
                hardware.batteryHardware(),
                hardware.positionHardware(),
                new InMemoryRuntimeEventBus(),
                robotEvents,
                WarehouseWorld.square(20));
        engine.start();

        RecordingMqttClientGateway gateway = new RecordingMqttClientGateway();
        MqttRobotEventPublisher mqttPublisher = new MqttRobotEventPublisher(
                "bridge-robot", gateway, MqttMessageSerializer.createDefault(), RobotMqttTopicConfig.defaults());

        // Bridge outbound: replay in-memory events onto MQTT adapter (adapter itself has no logic).
        for (RobotStatusMessage status : robotEvents.statusMessages()) {
            mqttPublisher.publishStatus(status);
        }

        MqttRobotCommandReceiver mqttReceiver = new MqttRobotCommandReceiver(
                "bridge-robot",
                gateway,
                MqttMessageSerializer.createDefault(),
                RobotMqttTopicConfig.defaults(),
                new EngineRobotCommandReceiver(engine),
                Clock.fixed(Instant.parse("2026-07-26T17:00:00Z"), ZoneOffset.UTC));
        mqttReceiver.start();

        String assignJson = """
                {
                  "type": "ASSIGN_MISSION",
                  "missionId": "mqtt-mission",
                  "steps": [ { "stepId": "n1", "type": "NAVIGATE", "x": 1, "y": 0 } ]
                }
                """;
        gateway.deliver("robot/bridge-robot/commands", assignJson.getBytes(StandardCharsets.UTF_8));

        assertThat(engine.missionManager().hasActiveMission()).isTrue();
        assertThat(engine.missionManager().activeMission().orElseThrow().missionId()).isEqualTo("mqtt-mission");
        assertThat(engine.missionManager().activeMission().orElseThrow().status()).isEqualTo(MissionStatus.RUNNING);
        assertThat(robotEvents.missionMessages())
                .extracting(RobotMissionMessage::eventType)
                .contains(RobotMissionMessage.EventType.STARTED);

        String cancelJson = """
                { "type": "CANCEL_MISSION", "missionId": "mqtt-mission" }
                """;
        gateway.deliver("robot/bridge-robot/commands", cancelJson.getBytes(StandardCharsets.UTF_8));

        assertThat(engine.missionManager().hasActiveMission()).isFalse();
        assertThat(robotEvents.missionMessages())
                .extracting(RobotMissionMessage::eventType)
                .contains(RobotMissionMessage.EventType.CANCELLED);

        mqttPublisher.publishMission(robotEvents.missionMessages().getLast());
        assertThat(gateway.published().stream().map(RecordingMqttClientGateway.PublishedMessage::topic))
                .contains("robot/bridge-robot/events/status", "robot/bridge-robot/events/mission");
    }
}
