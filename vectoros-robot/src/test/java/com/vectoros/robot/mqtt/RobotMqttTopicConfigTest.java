package com.vectoros.robot.mqtt;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RobotMqttTopicConfigTest {

    @Test
    void defaultsResolveRobotIdPlaceholder() {
        RobotMqttTopicConfig topics = RobotMqttTopicConfig.defaults();

        assertThat(topics.statusTopic("r1")).isEqualTo("robot/r1/events/status");
        assertThat(topics.missionTopic("r1")).isEqualTo("robot/r1/events/mission");
        assertThat(topics.batteryTopic("r1")).isEqualTo("robot/r1/events/battery");
        assertThat(topics.positionTopic("r1")).isEqualTo("robot/r1/events/position");
        assertThat(topics.commandsTopic("r1")).isEqualTo("robot/r1/commands");
    }

    @Test
    void customTemplatesAreSupported() {
        RobotMqttTopicConfig topics = new RobotMqttTopicConfig(
                "fleet/{robotId}/status",
                "fleet/{robotId}/mission",
                "fleet/{robotId}/battery",
                "fleet/{robotId}/position",
                "fleet/{robotId}/cmd");

        assertThat(topics.commandsTopic("alpha")).isEqualTo("fleet/alpha/cmd");
    }

    @Test
    void templateMustContainRobotIdPlaceholder() {
        assertThatThrownBy(() -> new RobotMqttTopicConfig(
                "robot/status",
                RobotMqttTopicConfig.DEFAULT_MISSION,
                RobotMqttTopicConfig.DEFAULT_BATTERY,
                RobotMqttTopicConfig.DEFAULT_POSITION,
                RobotMqttTopicConfig.DEFAULT_COMMANDS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("{robotId}");
    }

    @Test
    void blankRobotIdRejected() {
        assertThatThrownBy(() -> RobotMqttTopicConfig.defaults().statusTopic(" "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
