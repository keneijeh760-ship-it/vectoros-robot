package com.vectoros.robot.messaging;

import com.vectoros.robot.telemetry.RobotTelemetrySnapshot;

/**
 * Outbound messaging port for fleet-facing robot events.
 * Transport-agnostic — MQTT / Kafka / ROS2 adapters implement this.
 */
public interface RobotEventPublisher {

    void publishStatus(RobotStatusMessage message);

    void publishMission(RobotMissionMessage message);

    void publishBattery(RobotBatteryMessage message);

    void publishPosition(RobotPositionMessage message);

    void publishTelemetry(RobotTelemetrySnapshot snapshot);
}
