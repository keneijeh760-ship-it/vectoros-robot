package com.vectoros.robot.telemetry;

import com.vectoros.robot.runtime.mission.MissionStatus;
import com.vectoros.robot.runtime.model.RobotStatus;
import com.vectoros.robot.runtime.navigation.Heading;
import com.vectoros.robot.runtime.world.Coordinate;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RobotTelemetrySnapshotTest {

    private static final Instant NOW = Instant.parse("2026-07-26T12:00:00Z");

    @Test
    void createsSnapshotWithAllFields() {
        RobotTelemetrySnapshot snapshot = new RobotTelemetrySnapshot(
                "robot-1",
                NOW,
                RobotStatus.MOVING_TO_PICKUP,
                MissionStatus.RUNNING,
                87.5,
                new Coordinate(3, 4),
                Heading.NORTH,
                RobotTelemetryType.ON_CHANGE);

        assertThat(snapshot.robotId()).isEqualTo("robot-1");
        assertThat(snapshot.timestamp()).isEqualTo(NOW);
        assertThat(snapshot.robotStatus()).isEqualTo(RobotStatus.MOVING_TO_PICKUP);
        assertThat(snapshot.missionStatus()).contains(MissionStatus.RUNNING);
        assertThat(snapshot.batteryPercentage()).isEqualTo(87.5);
        assertThat(snapshot.coordinate()).isEqualTo(new Coordinate(3, 4));
        assertThat(snapshot.heading()).isEqualTo(Heading.NORTH);
        assertThat(snapshot.type()).isEqualTo(RobotTelemetryType.ON_CHANGE);
    }

    @Test
    void missionStatusIsEmptyWhenNoMission() {
        RobotTelemetrySnapshot snapshot = snapshotWithMissionStatus(null);
        assertThat(snapshot.missionStatus()).isEmpty();
    }

    @Test
    void rejectsBlankRobotId() {
        assertThatThrownBy(() -> new RobotTelemetrySnapshot(
                " ", NOW, RobotStatus.IDLE, null, 50.0,
                Coordinate.origin(), Heading.EAST, RobotTelemetryType.MANUAL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("robotId");
    }

    @Test
    void rejectsBatteryPercentageOutOfRange() {
        assertThatThrownBy(() -> snapshotWithBattery(-1.0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> snapshotWithBattery(100.1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> snapshotWithBattery(Double.NaN))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNullRequiredFields() {
        assertThatThrownBy(() -> new RobotTelemetrySnapshot(
                "r", null, RobotStatus.IDLE, null, 50.0,
                Coordinate.origin(), Heading.EAST, RobotTelemetryType.MANUAL))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new RobotTelemetrySnapshot(
                "r", NOW, null, null, 50.0,
                Coordinate.origin(), Heading.EAST, RobotTelemetryType.MANUAL))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new RobotTelemetrySnapshot(
                "r", NOW, RobotStatus.IDLE, null, 50.0,
                null, Heading.EAST, RobotTelemetryType.MANUAL))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new RobotTelemetrySnapshot(
                "r", NOW, RobotStatus.IDLE, null, 50.0,
                Coordinate.origin(), null, RobotTelemetryType.MANUAL))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new RobotTelemetrySnapshot(
                "r", NOW, RobotStatus.IDLE, null, 50.0,
                Coordinate.origin(), Heading.EAST, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void equalsAndHashCodeUseAllFields() {
        RobotTelemetrySnapshot a = snapshotWithMissionStatus(MissionStatus.RUNNING);
        RobotTelemetrySnapshot b = snapshotWithMissionStatus(MissionStatus.RUNNING);
        RobotTelemetrySnapshot c = snapshotWithMissionStatus(MissionStatus.COMPLETED);

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
    }

    private static RobotTelemetrySnapshot snapshotWithMissionStatus(MissionStatus missionStatus) {
        return new RobotTelemetrySnapshot(
                "robot-1", NOW, RobotStatus.IDLE, missionStatus, 50.0,
                Coordinate.origin(), Heading.EAST, RobotTelemetryType.PERIODIC);
    }

    private static RobotTelemetrySnapshot snapshotWithBattery(double percentage) {
        return new RobotTelemetrySnapshot(
                "robot-1", NOW, RobotStatus.IDLE, null, percentage,
                Coordinate.origin(), Heading.EAST, RobotTelemetryType.PERIODIC);
    }
}
