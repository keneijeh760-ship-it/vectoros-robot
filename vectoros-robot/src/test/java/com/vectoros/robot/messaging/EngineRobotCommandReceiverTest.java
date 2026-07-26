package com.vectoros.robot.messaging;

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

import static org.assertj.core.api.Assertions.assertThat;

class EngineRobotCommandReceiverTest {

    private RobotEngine engine;
    private EngineRobotCommandReceiver receiver;

    @BeforeEach
    void setUp() {
        SimulationHardwareFactory hardware = SimulationHardwareFactory.createDefault();
        engine = RobotEngine.create(
                "cmd-robot",
                hardware.movementHardware(),
                hardware.batteryHardware(),
                hardware.positionHardware(),
                new InMemoryRuntimeEventBus(),
                WarehouseWorld.square(20));
        engine.start();
        receiver = new EngineRobotCommandReceiver(engine);
    }

    @Test
    void assignMissionCommandIsForwardedToEngine() {
        Mission mission = Mission.of(
                "assign-via-cmd",
                java.time.Instant.parse("2026-07-26T15:00:00Z"),
                MissionStep.navigate("n1", new Coordinate(1, 0)));

        receiver.receive(new AssignMissionCommand(mission));

        assertThat(engine.missionManager().hasActiveMission()).isTrue();
        assertThat(mission.status()).isEqualTo(MissionStatus.RUNNING);
        assertThat(engine.state().status()).isEqualTo(RobotStatus.TASK_ASSIGNED);
    }

    @Test
    void cancelMissionCommandIsForwardedToEngine() {
        Mission mission = Mission.of(
                "cancel-via-cmd",
                java.time.Instant.parse("2026-07-26T15:00:00Z"),
                MissionStep.navigate("n1", new Coordinate(5, 0)));
        engine.assignMission(mission);

        receiver.receive(new CancelMissionCommand("cancel-via-cmd"));

        assertThat(engine.missionManager().hasActiveMission()).isFalse();
        assertThat(mission.status()).isEqualTo(MissionStatus.CANCELLED);
        assertThat(engine.state().status()).isEqualTo(RobotStatus.IDLE);
    }
}
