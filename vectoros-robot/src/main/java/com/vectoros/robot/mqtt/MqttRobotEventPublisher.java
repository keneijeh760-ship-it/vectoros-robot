package com.vectoros.robot.mqtt;

import com.vectoros.robot.messaging.RobotBatteryMessage;
import com.vectoros.robot.messaging.RobotEventPublisher;
import com.vectoros.robot.messaging.RobotMissionMessage;
import com.vectoros.robot.messaging.RobotPositionMessage;
import com.vectoros.robot.messaging.RobotStatusMessage;
import com.vectoros.robot.telemetry.RobotTelemetrySnapshot;

import java.util.Objects;

/**
 * MQTT adapter for outbound robot events. Serialize + publish only.
 */
public final class MqttRobotEventPublisher implements RobotEventPublisher {

    private final String robotId;
    private final MqttClientGateway mqttClient;
    private final MqttMessageSerializer serializer;
    private final RobotMqttTopicConfig topics;

    public MqttRobotEventPublisher(
            String robotId,
            MqttClientGateway mqttClient,
            MqttMessageSerializer serializer,
            RobotMqttTopicConfig topics) {
        if (robotId == null || robotId.isBlank()) {
            throw new IllegalArgumentException("robotId must not be blank");
        }
        this.robotId = robotId;
        this.mqttClient = Objects.requireNonNull(mqttClient, "mqttClient");
        this.serializer = Objects.requireNonNull(serializer, "serializer");
        this.topics = Objects.requireNonNull(topics, "topics");
    }

    public MqttRobotEventPublisher(String robotId, MqttClientGateway mqttClient) {
        this(robotId, mqttClient, MqttMessageSerializer.createDefault(), RobotMqttTopicConfig.defaults());
    }

    @Override
    public void publishStatus(RobotStatusMessage message) {
        Objects.requireNonNull(message, "message");
        mqttClient.publish(topics.statusTopic(robotId), serializer.serialize(message));
    }

    @Override
    public void publishMission(RobotMissionMessage message) {
        Objects.requireNonNull(message, "message");
        mqttClient.publish(topics.missionTopic(robotId), serializer.serialize(message));
    }

    @Override
    public void publishBattery(RobotBatteryMessage message) {
        Objects.requireNonNull(message, "message");
        mqttClient.publish(topics.batteryTopic(robotId), serializer.serialize(message));
    }

    @Override
    public void publishPosition(RobotPositionMessage message) {
        Objects.requireNonNull(message, "message");
        mqttClient.publish(topics.positionTopic(robotId), serializer.serialize(message));
    }

    @Override
    public void publishTelemetry(RobotTelemetrySnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        mqttClient.publish(topics.telemetryTopic(robotId), serializer.serialize(snapshot));
    }
}
