package com.vectoros.robot.messaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * In-memory publisher for tests and local runs without a broker.
 */
public final class InMemoryRobotEventPublisher implements RobotEventPublisher {

    private final List<RobotStatusMessage> statusMessages = new ArrayList<>();
    private final List<RobotMissionMessage> missionMessages = new ArrayList<>();
    private final List<RobotBatteryMessage> batteryMessages = new ArrayList<>();
    private final List<RobotPositionMessage> positionMessages = new ArrayList<>();

    @Override
    public void publishStatus(RobotStatusMessage message) {
        statusMessages.add(Objects.requireNonNull(message, "message"));
    }

    @Override
    public void publishMission(RobotMissionMessage message) {
        missionMessages.add(Objects.requireNonNull(message, "message"));
    }

    @Override
    public void publishBattery(RobotBatteryMessage message) {
        batteryMessages.add(Objects.requireNonNull(message, "message"));
    }

    @Override
    public void publishPosition(RobotPositionMessage message) {
        positionMessages.add(Objects.requireNonNull(message, "message"));
    }

    public List<RobotStatusMessage> statusMessages() {
        return Collections.unmodifiableList(statusMessages);
    }

    public List<RobotMissionMessage> missionMessages() {
        return Collections.unmodifiableList(missionMessages);
    }

    public List<RobotBatteryMessage> batteryMessages() {
        return Collections.unmodifiableList(batteryMessages);
    }

    public List<RobotPositionMessage> positionMessages() {
        return Collections.unmodifiableList(positionMessages);
    }

    public void clear() {
        statusMessages.clear();
        missionMessages.clear();
        batteryMessages.clear();
        positionMessages.clear();
    }
}
