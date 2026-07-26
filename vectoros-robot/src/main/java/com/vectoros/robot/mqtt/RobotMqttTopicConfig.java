package com.vectoros.robot.mqtt;

import java.util.Objects;

/**
 * Configurable MQTT topic templates. Placeholders use {@code {robotId}}.
 */
public final class RobotMqttTopicConfig {

    public static final String DEFAULT_STATUS = "robot/{robotId}/events/status";
    public static final String DEFAULT_MISSION = "robot/{robotId}/events/mission";
    public static final String DEFAULT_BATTERY = "robot/{robotId}/events/battery";
    public static final String DEFAULT_POSITION = "robot/{robotId}/events/position";
    public static final String DEFAULT_COMMANDS = "robot/{robotId}/commands";

    private final String statusTemplate;
    private final String missionTemplate;
    private final String batteryTemplate;
    private final String positionTemplate;
    private final String commandsTemplate;

    public RobotMqttTopicConfig(
            String statusTemplate,
            String missionTemplate,
            String batteryTemplate,
            String positionTemplate,
            String commandsTemplate) {
        this.statusTemplate = requireTemplate(statusTemplate, "statusTemplate");
        this.missionTemplate = requireTemplate(missionTemplate, "missionTemplate");
        this.batteryTemplate = requireTemplate(batteryTemplate, "batteryTemplate");
        this.positionTemplate = requireTemplate(positionTemplate, "positionTemplate");
        this.commandsTemplate = requireTemplate(commandsTemplate, "commandsTemplate");
    }

    public static RobotMqttTopicConfig defaults() {
        return new RobotMqttTopicConfig(
                DEFAULT_STATUS,
                DEFAULT_MISSION,
                DEFAULT_BATTERY,
                DEFAULT_POSITION,
                DEFAULT_COMMANDS);
    }

    public String statusTopic(String robotId) {
        return resolve(statusTemplate, robotId);
    }

    public String missionTopic(String robotId) {
        return resolve(missionTemplate, robotId);
    }

    public String batteryTopic(String robotId) {
        return resolve(batteryTemplate, robotId);
    }

    public String positionTopic(String robotId) {
        return resolve(positionTemplate, robotId);
    }

    public String commandsTopic(String robotId) {
        return resolve(commandsTemplate, robotId);
    }

    private static String resolve(String template, String robotId) {
        if (robotId == null || robotId.isBlank()) {
            throw new IllegalArgumentException("robotId must not be blank");
        }
        return template.replace("{robotId}", robotId);
    }

    private static String requireTemplate(String template, String name) {
        Objects.requireNonNull(template, name);
        if (!template.contains("{robotId}")) {
            throw new IllegalArgumentException(name + " must contain {robotId} placeholder");
        }
        return template;
    }
}
