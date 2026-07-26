package com.vectoros.robot.runtime.model;

import com.vectoros.robot.runtime.navigation.Heading;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Single source of truth for robot runtime state.
 * Mutations are package/component-controlled through explicit mutators.
 */
public final class RobotState {

    private final String robotId;
    private RobotStatus status;
    private BatteryState battery;
    private Position position;
    private RobotTask currentTask;
    private Heading heading;
    private double headingDegrees;
    private double speed;
    private Instant lastHeartbeat;

    public RobotState(
            String robotId,
            RobotStatus status,
            BatteryState battery,
            Position position,
            RobotTask currentTask,
            Heading heading,
            double speed,
            Instant lastHeartbeat) {
        if (robotId == null || robotId.isBlank()) {
            throw new IllegalArgumentException("robotId must not be blank");
        }
        this.robotId = robotId;
        this.status = Objects.requireNonNull(status, "status");
        this.battery = Objects.requireNonNull(battery, "battery");
        this.position = Objects.requireNonNull(position, "position");
        this.currentTask = currentTask;
        this.heading = Objects.requireNonNull(heading, "heading");
        this.headingDegrees = heading.degrees();
        this.speed = requireNonNegativeFinite(speed, "speed");
        this.lastHeartbeat = Objects.requireNonNull(lastHeartbeat, "lastHeartbeat");
    }

    public static RobotState initial(String robotId) {
        return new RobotState(
                robotId,
                RobotStatus.OFFLINE,
                BatteryState.full(),
                Position.origin(),
                null,
                Heading.EAST,
                0.0,
                Instant.EPOCH);
    }

    public String robotId() {
        return robotId;
    }

    public RobotStatus status() {
        return status;
    }

    public BatteryState battery() {
        return battery;
    }

    public Position position() {
        return position;
    }

    public Optional<RobotTask> currentTask() {
        return Optional.ofNullable(currentTask);
    }

    public Heading heading() {
        return heading;
    }

    public double headingDegrees() {
        return headingDegrees;
    }

    public double speed() {
        return speed;
    }

    public Instant lastHeartbeat() {
        return lastHeartbeat;
    }

    public boolean isMoving() {
        return speed > 0.0;
    }

    /**
     * Applies a status change.
     * Must only be invoked by {@link com.vectoros.robot.runtime.state.RobotStateMachine}
     * after a transition has been validated.
     */
    public void applyStatus(RobotStatus status) {
        this.status = Objects.requireNonNull(status, "status");
    }

    public void updateBattery(BatteryState battery) {
        this.battery = Objects.requireNonNull(battery, "battery");
    }

    public void updatePosition(Position position) {
        this.position = Objects.requireNonNull(position, "position");
    }

    public void assignTask(RobotTask task) {
        this.currentTask = Objects.requireNonNull(task, "task");
    }

    public void clearTask() {
        this.currentTask = null;
    }

    public void updateHeading(Heading heading) {
        this.heading = Objects.requireNonNull(heading, "heading");
        this.headingDegrees = heading.degrees();
    }

    public void updateHeading(double headingDegrees) {
        this.heading = Heading.fromDegrees(headingDegrees);
        this.headingDegrees = this.heading.degrees();
    }

    public void updateSpeed(double speed) {
        this.speed = requireNonNegativeFinite(speed, "speed");
    }

    public void updateHeartbeat(Instant heartbeat) {
        this.lastHeartbeat = Objects.requireNonNull(heartbeat, "heartbeat");
    }

    private static double requireNonNegativeFinite(double value, String name) {
        if (!Double.isFinite(value) || value < 0) {
            throw new IllegalArgumentException(name + " must be a non-negative finite number");
        }
        return value;
    }
}
