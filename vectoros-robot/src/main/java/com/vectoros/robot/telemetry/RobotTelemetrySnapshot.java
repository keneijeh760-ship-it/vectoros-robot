package com.vectoros.robot.telemetry;

import com.vectoros.robot.runtime.mission.MissionStatus;
import com.vectoros.robot.runtime.model.RobotStatus;
import com.vectoros.robot.runtime.navigation.Heading;
import com.vectoros.robot.runtime.world.Coordinate;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Canonical, immutable representation of the robot's current state.
 * Telemetry describes what the robot looks like right now;
 * domain events describe things that happened.
 */
public final class RobotTelemetrySnapshot {

    private final String robotId;
    private final Instant timestamp;
    private final RobotStatus robotStatus;
    private final MissionStatus missionStatus; // null when no mission is active
    private final double batteryPercentage;
    private final Coordinate coordinate;
    private final Heading heading;
    private final RobotTelemetryType type;

    public RobotTelemetrySnapshot(
            String robotId,
            Instant timestamp,
            RobotStatus robotStatus,
            MissionStatus missionStatus,
            double batteryPercentage,
            Coordinate coordinate,
            Heading heading,
            RobotTelemetryType type) {
        if (robotId == null || robotId.isBlank()) {
            throw new IllegalArgumentException("robotId must not be blank");
        }
        if (!Double.isFinite(batteryPercentage) || batteryPercentage < 0.0 || batteryPercentage > 100.0) {
            throw new IllegalArgumentException("batteryPercentage must be between 0 and 100");
        }
        this.robotId = robotId;
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
        this.robotStatus = Objects.requireNonNull(robotStatus, "robotStatus");
        this.missionStatus = missionStatus;
        this.batteryPercentage = batteryPercentage;
        this.coordinate = Objects.requireNonNull(coordinate, "coordinate");
        this.heading = Objects.requireNonNull(heading, "heading");
        this.type = Objects.requireNonNull(type, "type");
    }

    public String robotId() {
        return robotId;
    }

    public Instant timestamp() {
        return timestamp;
    }

    public RobotStatus robotStatus() {
        return robotStatus;
    }

    /**
     * Empty when the robot has no mission.
     */
    public Optional<MissionStatus> missionStatus() {
        return Optional.ofNullable(missionStatus);
    }

    public double batteryPercentage() {
        return batteryPercentage;
    }

    public Coordinate coordinate() {
        return coordinate;
    }

    public Heading heading() {
        return heading;
    }

    public RobotTelemetryType type() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RobotTelemetrySnapshot that)) {
            return false;
        }
        return Double.compare(that.batteryPercentage, batteryPercentage) == 0
                && robotId.equals(that.robotId)
                && timestamp.equals(that.timestamp)
                && robotStatus == that.robotStatus
                && missionStatus == that.missionStatus
                && coordinate.equals(that.coordinate)
                && heading == that.heading
                && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(robotId, timestamp, robotStatus, missionStatus,
                batteryPercentage, coordinate, heading, type);
    }

    @Override
    public String toString() {
        return "RobotTelemetrySnapshot{robotId='" + robotId
                + "', timestamp=" + timestamp
                + ", robotStatus=" + robotStatus
                + ", missionStatus=" + missionStatus
                + ", batteryPercentage=" + batteryPercentage
                + ", coordinate=" + coordinate
                + ", heading=" + heading
                + ", type=" + type + '}';
    }
}
