package com.vectoros.robot.messaging;

import com.vectoros.robot.runtime.engine.RobotEngine;
import com.vectoros.robot.runtime.events.InMemoryRuntimeEventBus;
import com.vectoros.robot.runtime.hal.simulation.SimulationHardwareFactory;
import com.vectoros.robot.runtime.mission.Mission;
import com.vectoros.robot.runtime.mission.MissionStep;
import com.vectoros.robot.runtime.model.Position;
import com.vectoros.robot.runtime.model.RobotTask;
import com.vectoros.robot.runtime.world.Coordinate;
import com.vectoros.robot.runtime.world.WarehouseWorld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RuntimeMessagingIntegrationTest {

    private InMemoryRobotEventPublisher robotEvents;
    private RobotEngine engine;

    @BeforeEach
    void setUp() {
        robotEvents = new InMemoryRobotEventPublisher();
        SimulationHardwareFactory hardware = SimulationHardwareFactory.createDefault();
        engine = RobotEngine.create(
                "msg-robot",
                hardware.movementHardware(),
                hardware.batteryHardware(),
                hardware.positionHardware(),
                new InMemoryRuntimeEventBus(),
                robotEvents,
                WarehouseWorld.square(20));
    }

    @Test
    void startPublishesStatusUpdate() {
        engine.start();

        assertThat(robotEvents.statusMessages()).isNotEmpty();
        assertThat(robotEvents.statusMessages().getLast().status()).isEqualTo("IDLE");
        assertThat(robotEvents.statusMessages().getLast().robotId()).isEqualTo("msg-robot");
    }

    @Test
    void missionLifecyclePublishesMissionEvents() {
        engine.start();
        robotEvents.clear();

        Mission mission = Mission.of(
                "m-int",
                java.time.Instant.parse("2026-07-26T16:00:00Z"),
                MissionStep.navigate("n1", new Coordinate(1, 0)));
        engine.assignMission(mission);

        assertThat(robotEvents.missionMessages())
                .extracting(RobotMissionMessage::eventType)
                .contains(RobotMissionMessage.EventType.STARTED);

        for (int i = 0; i < 5 && engine.missionManager().hasActiveMission(); i++) {
            engine.tick();
        }

        assertThat(robotEvents.missionMessages())
                .extracting(RobotMissionMessage::eventType)
                .contains(RobotMissionMessage.EventType.COMPLETED);
        assertThat(robotEvents.batteryMessages()).isNotEmpty();
        assertThat(robotEvents.positionMessages()).isNotEmpty();
        assertThat(robotEvents.statusMessages()).isNotEmpty();
    }

    @Test
    void cancelPublishesCancelledMissionEvent() {
        engine.start();
        engine.assignTask(new RobotTask("task-cancel", new Position(3, 0)));
        robotEvents.clear();

        engine.cancelMission();

        assertThat(robotEvents.missionMessages())
                .extracting(RobotMissionMessage::eventType)
                .contains(RobotMissionMessage.EventType.CANCELLED);
    }

    @Test
    void shutdownPublishesOfflineStatus() {
        engine.start();
        robotEvents.clear();

        engine.shutdown();

        assertThat(robotEvents.statusMessages().getLast().status()).isEqualTo("OFFLINE");
    }
}
