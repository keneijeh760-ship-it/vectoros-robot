package com.vectoros.robot.telemetry;

import com.vectoros.robot.runtime.engine.RobotEngine;
import com.vectoros.robot.runtime.mission.Mission;
import com.vectoros.robot.runtime.mission.MissionStatus;
import com.vectoros.robot.runtime.model.RobotState;
import com.vectoros.robot.runtime.world.Coordinate;

import java.time.Instant;
import java.util.Objects;

/**
 * Maps runtime state into {@link RobotTelemetrySnapshot}.
 * Pure translation — no business logic.
 */
public final class TelemetryMapper {

    private TelemetryMapper() {
    }

    /**
     * Build a snapshot from an explicit state + mission status pair.
     *
     * @param missionStatus current mission status, or {@code null} when no mission exists
     */
    public static RobotTelemetrySnapshot fromState(
            RobotState state,
            MissionStatus missionStatus,
            RobotTelemetryType type,
            Instant timestamp) {
        Objects.requireNonNull(state, "state");
        return new RobotTelemetrySnapshot(
                state.robotId(),
                timestamp,
                state.status(),
                missionStatus,
                state.battery().percentage(),
                Coordinate.fromPosition(state.position()),
                state.heading(),
                type);
    }

    /**
     * Build a snapshot from a live engine, reading the active mission status if present.
     */
    public static RobotTelemetrySnapshot fromEngine(
            RobotEngine engine,
            RobotTelemetryType type,
            Instant timestamp) {
        Objects.requireNonNull(engine, "engine");
        MissionStatus missionStatus = engine.missionManager()
                .activeMission()
                .map(Mission::status)
                .orElse(null);
        return fromState(engine.state(), missionStatus, type, timestamp);
    }
}
