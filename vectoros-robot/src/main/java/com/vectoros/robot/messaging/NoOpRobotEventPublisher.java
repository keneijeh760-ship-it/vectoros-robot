package com.vectoros.robot.messaging;

/**
 * No-op publisher when external messaging is disabled.
 */
public final class NoOpRobotEventPublisher implements RobotEventPublisher {

    public static final NoOpRobotEventPublisher INSTANCE = new NoOpRobotEventPublisher();

    @Override
    public void publishStatus(RobotStatusMessage message) {
        // intentionally empty
    }

    @Override
    public void publishMission(RobotMissionMessage message) {
        // intentionally empty
    }

    @Override
    public void publishBattery(RobotBatteryMessage message) {
        // intentionally empty
    }

    @Override
    public void publishPosition(RobotPositionMessage message) {
        // intentionally empty
    }

    @Override
    public void publishTelemetry(com.vectoros.robot.telemetry.RobotTelemetrySnapshot snapshot) {
        // intentionally empty
    }
}
