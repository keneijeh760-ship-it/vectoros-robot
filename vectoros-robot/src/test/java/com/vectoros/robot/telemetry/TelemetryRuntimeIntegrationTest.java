package com.vectoros.robot.telemetry;

import com.vectoros.robot.messaging.InMemoryRobotEventPublisher;
import com.vectoros.robot.runtime.engine.RobotEngine;
import com.vectoros.robot.runtime.events.InMemoryRuntimeEventBus;
import com.vectoros.robot.runtime.hal.simulation.SimulationHardwareFactory;
import com.vectoros.robot.runtime.mission.Mission;
import com.vectoros.robot.runtime.mission.MissionStatus;
import com.vectoros.robot.runtime.mission.MissionStep;
import com.vectoros.robot.runtime.model.RobotStatus;
import com.vectoros.robot.runtime.world.Coordinate;
import com.vectoros.robot.runtime.world.WarehouseWorld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TelemetryRuntimeIntegrationTest {

    private static final Instant NOW = Instant.parse("2026-07-26T15:00:00Z");

    private InMemoryRobotEventPublisher robotEvents;
    private RobotEngine engine;
    private RobotTelemetryPublisher telemetryPublisher;

    @BeforeEach
    void setUp() {
        robotEvents = new InMemoryRobotEventPublisher();
        SimulationHardwareFactory hardware = SimulationHardwareFactory.createDefault();
        engine = RobotEngine.create(
                "tel-runtime",
                hardware.movementHardware(),
                hardware.batteryHardware(),
                hardware.positionHardware(),
                new InMemoryRuntimeEventBus(),
                robotEvents,
                WarehouseWorld.square(20));
        telemetryPublisher = new RobotTelemetryPublisher(robotEvents);
    }

    @Test
    void idleRuntimeProducesSnapshotWithoutMission() {
        engine.start();

        RobotTelemetrySnapshot snapshot =
                TelemetryMapper.fromEngine(engine, RobotTelemetryType.PERIODIC, NOW);

        assertThat(snapshot.robotId()).isEqualTo("tel-runtime");
        assertThat(snapshot.robotStatus()).isEqualTo(RobotStatus.IDLE);
        assertThat(snapshot.missionStatus()).isEmpty();
        assertThat(snapshot.batteryPercentage()).isEqualTo(100.0);
        assertThat(snapshot.coordinate()).isEqualTo(Coordinate.origin());
    }

    @Test
    void runningMissionReflectedInSnapshot() {
        engine.start();
        engine.assignMission(Mission.of(
                "tel-mission",
                NOW,
                MissionStep.navigate("n1", new Coordinate(3, 0))));
        engine.tick();

        RobotTelemetrySnapshot snapshot =
                TelemetryMapper.fromEngine(engine, RobotTelemetryType.ON_CHANGE, NOW);

        assertThat(snapshot.missionStatus()).contains(MissionStatus.RUNNING);
        assertThat(snapshot.robotStatus()).isEqualTo(RobotStatus.MOVING_TO_PICKUP);
        assertThat(snapshot.batteryPercentage()).isLessThan(100.0);
    }

    @Test
    void snapshotPublishedThroughMessagingPort() {
        engine.start();

        RobotTelemetrySnapshot snapshot =
                TelemetryMapper.fromEngine(engine, RobotTelemetryType.MANUAL, NOW);
        telemetryPublisher.publish(snapshot);

        assertThat(robotEvents.telemetrySnapshots()).containsExactly(snapshot);
    }

    @Test
    void snapshotTracksMovementAcrossTicks() {
        engine.start();
        engine.assignMission(Mission.of(
                "tel-move",
                NOW,
                MissionStep.navigate("n1", new Coordinate(2, 0))));

        for (int i = 0; i < 5 && engine.missionManager().hasActiveMission(); i++) {
            engine.tick();
        }

        RobotTelemetrySnapshot snapshot =
                TelemetryMapper.fromEngine(engine, RobotTelemetryType.PERIODIC, NOW);

        assertThat(snapshot.coordinate()).isEqualTo(new Coordinate(2, 0));
        assertThat(snapshot.missionStatus()).isEmpty();
        assertThat(snapshot.robotStatus()).isEqualTo(RobotStatus.IDLE);
    }
}
