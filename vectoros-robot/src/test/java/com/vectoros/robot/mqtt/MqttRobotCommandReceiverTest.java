package com.vectoros.robot.mqtt;

import com.vectoros.robot.messaging.AssignMissionCommand;
import com.vectoros.robot.messaging.CancelMissionCommand;
import com.vectoros.robot.messaging.RobotCommand;
import com.vectoros.robot.messaging.RobotCommandReceiver;
import com.vectoros.robot.runtime.mission.MissionStepType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MqttRobotCommandReceiverTest {

    private static final Instant FIXED = Instant.parse("2026-07-26T14:00:00Z");

    private RecordingMqttClientGateway gateway;
    private List<RobotCommand> received;
    private MqttRobotCommandReceiver adapter;

    @BeforeEach
    void setUp() {
        gateway = new RecordingMqttClientGateway();
        received = new ArrayList<>();
        RobotCommandReceiver receiver = received::add;
        adapter = new MqttRobotCommandReceiver(
                "bot-cmd",
                gateway,
                MqttMessageSerializer.createDefault(),
                RobotMqttTopicConfig.defaults(),
                receiver,
                Clock.fixed(FIXED, ZoneOffset.UTC));
    }

    @Test
    void startSubscribesToCommandsTopic() {
        adapter.start();
        assertThat(gateway.subscriptions()).containsKey("robot/bot-cmd/commands");
    }

    @Test
    void deserializesAssignMissionAndForwards() {
        adapter.start();
        String json = """
                {
                  "type": "ASSIGN_MISSION",
                  "missionId": "m-42",
                  "steps": [
                    { "stepId": "n1", "type": "NAVIGATE", "x": 2, "y": 3 },
                    { "stepId": "w1", "type": "WAIT", "waitTicks": 2 }
                  ]
                }
                """;

        gateway.deliver("robot/bot-cmd/commands", json.getBytes(StandardCharsets.UTF_8));

        assertThat(received).hasSize(1);
        assertThat(received.getFirst()).isInstanceOf(AssignMissionCommand.class);
        AssignMissionCommand assign = (AssignMissionCommand) received.getFirst();
        assertThat(assign.mission().missionId()).isEqualTo("m-42");
        assertThat(assign.mission().steps()).hasSize(2);
        assertThat(assign.mission().steps().getFirst().type()).isEqualTo(MissionStepType.NAVIGATE);
        assertThat(assign.mission().steps().get(1).type()).isEqualTo(MissionStepType.WAIT);
        assertThat(assign.mission().createdAt()).isEqualTo(FIXED);
    }

    @Test
    void deserializesCancelMissionAndForwards() {
        adapter.start();
        String json = """
                { "type": "CANCEL_MISSION", "missionId": "m-9" }
                """;

        gateway.deliver("robot/bot-cmd/commands", json.getBytes(StandardCharsets.UTF_8));

        assertThat(received).hasSize(1);
        assertThat(received.getFirst()).isInstanceOf(CancelMissionCommand.class);
        assertThat(((CancelMissionCommand) received.getFirst()).missionId()).isEqualTo("m-9");
    }

    @Test
    void unsupportedTypeThrows() {
        assertThatThrownBy(() -> adapter.onMqttMessage(
                "robot/bot-cmd/commands",
                "{\"type\":\"EXPLODE\"}".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(MqttSerializationException.class)
                .hasMessageContaining("Unsupported command type");
    }
}
