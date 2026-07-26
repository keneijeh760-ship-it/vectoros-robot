package com.vectoros.robot.mqtt;

import com.vectoros.robot.messaging.AssignMissionCommand;
import com.vectoros.robot.messaging.CancelMissionCommand;
import com.vectoros.robot.messaging.RobotCommand;
import com.vectoros.robot.messaging.RobotCommandReceiver;
import com.vectoros.robot.runtime.mission.Mission;
import com.vectoros.robot.runtime.mission.MissionStep;
import com.vectoros.robot.runtime.world.Coordinate;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * MQTT adapter for inbound commands. Deserialize + forward only.
 */
public final class MqttRobotCommandReceiver {

    public static final String TYPE_ASSIGN_MISSION = "ASSIGN_MISSION";
    public static final String TYPE_CANCEL_MISSION = "CANCEL_MISSION";

    private final String robotId;
    private final MqttClientGateway mqttClient;
    private final MqttMessageSerializer serializer;
    private final RobotMqttTopicConfig topics;
    private final RobotCommandReceiver commandReceiver;
    private final Clock clock;
    private boolean subscribed;

    public MqttRobotCommandReceiver(
            String robotId,
            MqttClientGateway mqttClient,
            MqttMessageSerializer serializer,
            RobotMqttTopicConfig topics,
            RobotCommandReceiver commandReceiver,
            Clock clock) {
        if (robotId == null || robotId.isBlank()) {
            throw new IllegalArgumentException("robotId must not be blank");
        }
        this.robotId = robotId;
        this.mqttClient = Objects.requireNonNull(mqttClient, "mqttClient");
        this.serializer = Objects.requireNonNull(serializer, "serializer");
        this.topics = Objects.requireNonNull(topics, "topics");
        this.commandReceiver = Objects.requireNonNull(commandReceiver, "commandReceiver");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public MqttRobotCommandReceiver(
            String robotId,
            MqttClientGateway mqttClient,
            RobotCommandReceiver commandReceiver) {
        this(
                robotId,
                mqttClient,
                MqttMessageSerializer.createDefault(),
                RobotMqttTopicConfig.defaults(),
                commandReceiver,
                Clock.systemUTC());
    }

    /**
     * Subscribe to the robot command topic.
     */
    public void start() {
        if (subscribed) {
            return;
        }
        mqttClient.subscribe(topics.commandsTopic(robotId), this::onMqttMessage);
        subscribed = true;
    }

    /**
     * Visible for tests — process a raw payload as if received from MQTT.
     */
    public void onMqttMessage(String topic, byte[] payload) {
        RobotCommandPayload commandPayload = serializer.deserialize(payload, RobotCommandPayload.class);
        RobotCommand command = toCommand(commandPayload);
        commandReceiver.receive(command);
    }

    private RobotCommand toCommand(RobotCommandPayload payload) {
        if (payload.getType() == null || payload.getType().isBlank()) {
            throw new MqttSerializationException("Command payload missing type");
        }
        String type = payload.getType().trim().toUpperCase(Locale.ROOT);
        return switch (type) {
            case TYPE_ASSIGN_MISSION -> new AssignMissionCommand(toMission(payload));
            case TYPE_CANCEL_MISSION -> new CancelMissionCommand(payload.getMissionId());
            default -> throw new MqttSerializationException("Unsupported command type: " + payload.getType());
        };
    }

    private Mission toMission(RobotCommandPayload payload) {
        if (payload.getMissionId() == null || payload.getMissionId().isBlank()) {
            throw new MqttSerializationException("ASSIGN_MISSION requires missionId");
        }
        if (payload.getSteps() == null || payload.getSteps().isEmpty()) {
            throw new MqttSerializationException("ASSIGN_MISSION requires steps");
        }
        List<MissionStep> steps = new ArrayList<>();
        for (RobotCommandPayload.MissionStepPayload stepPayload : payload.getSteps()) {
            steps.add(toStep(stepPayload));
        }
        return new Mission(payload.getMissionId(), steps, clock.instant());
    }

    private static MissionStep toStep(RobotCommandPayload.MissionStepPayload payload) {
        if (payload.getStepId() == null || payload.getStepId().isBlank()) {
            throw new MqttSerializationException("Mission step missing stepId");
        }
        if (payload.getType() == null || payload.getType().isBlank()) {
            throw new MqttSerializationException("Mission step missing type");
        }
        String type = payload.getType().trim().toUpperCase(Locale.ROOT);
        return switch (type) {
            case "NAVIGATE" -> {
                if (payload.getX() == null || payload.getY() == null) {
                    throw new MqttSerializationException("NAVIGATE step requires x and y");
                }
                yield MissionStep.navigate(payload.getStepId(), new Coordinate(payload.getX(), payload.getY()));
            }
            case "WAIT" -> {
                if (payload.getWaitTicks() == null || payload.getWaitTicks() <= 0) {
                    throw new MqttSerializationException("WAIT step requires positive waitTicks");
                }
                yield MissionStep.waitTicks(payload.getStepId(), payload.getWaitTicks());
            }
            default -> throw new MqttSerializationException("Unsupported mission step type: " + payload.getType());
        };
    }
}
