package com.vectoros.robot.telemetry;

import com.vectoros.robot.runtime.mission.MissionStatus;
import com.vectoros.robot.runtime.model.BatteryState;
import com.vectoros.robot.runtime.model.Position;
import com.vectoros.robot.runtime.model.RobotState;
import com.vectoros.robot.runtime.model.RobotStatus;
import com.vectoros.robot.runtime.navigation.Heading;
import com.vectoros.robot.runtime.world.Coordinate;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TelemetryMapperTest {

    private static final Instant NOW = Instant.parse("2026-07-26T13:00:00Z");

    @Test
    void mapsEveryFieldFromState() {
        RobotState state = RobotState.initial("map-robot");
        state.applyStatus(RobotStatus.MOVING_TO_PICKUP);
        state.updateBattery(new BatteryState(64.0));
        state.updatePosition(new Position(5, 7));
        state.updateHeading(Heading.NORTH);

        RobotTelemetrySnapshot snapshot = TelemetryMapper.fromState(
                state, MissionStatus.RUNNING, RobotTelemetryType.PERIODIC, NOW);

        assertThat(snapshot.robotId()).isEqualTo("map-robot");
        assertThat(snapshot.timestamp()).isEqualTo(NOW);
        assertThat(snapshot.robotStatus()).isEqualTo(RobotStatus.MOVING_TO_PICKUP);
        assertThat(snapshot.missionStatus()).contains(MissionStatus.RUNNING);
        assertThat(snapshot.batteryPercentage()).isEqualTo(64.0);
        assertThat(snapshot.coordinate()).isEqualTo(new Coordinate(5, 7));
        assertThat(snapshot.heading()).isEqualTo(Heading.NORTH);
        assertThat(snapshot.type()).isEqualTo(RobotTelemetryType.PERIODIC);
    }

    @Test
    void missionStatusEmptyWhenNoneProvided() {
        RobotState state = RobotState.initial("map-robot");

        RobotTelemetrySnapshot snapshot = TelemetryMapper.fromState(
                state, null, RobotTelemetryType.MANUAL, NOW);

        assertThat(snapshot.missionStatus()).isEmpty();
    }

    @Test
    void continuousPositionRoundsToNearestCoordinate() {
        RobotState state = RobotState.initial("map-robot");
        state.updatePosition(new Position(2.6, 3.4));

        RobotTelemetrySnapshot snapshot = TelemetryMapper.fromState(
                state, null, RobotTelemetryType.PERIODIC, NOW);

        assertThat(snapshot.coordinate()).isEqualTo(new Coordinate(3, 3));
    }
}
