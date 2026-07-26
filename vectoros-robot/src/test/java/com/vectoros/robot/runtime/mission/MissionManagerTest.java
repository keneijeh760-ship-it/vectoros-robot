package com.vectoros.robot.runtime.mission;

import com.vectoros.robot.runtime.events.InMemoryRuntimeEventBus;
import com.vectoros.robot.runtime.hal.simulation.SimulationHardwareFactory;
import com.vectoros.robot.runtime.mission.events.MissionCancelledEvent;
import com.vectoros.robot.runtime.mission.events.MissionCompletedEvent;
import com.vectoros.robot.runtime.mission.events.MissionFailedEvent;
import com.vectoros.robot.runtime.mission.events.MissionStartedEvent;
import com.vectoros.robot.runtime.mission.events.MissionStepCompletedEvent;
import com.vectoros.robot.runtime.mission.events.MissionStepStartedEvent;
import com.vectoros.robot.runtime.model.RobotState;
import com.vectoros.robot.runtime.motion.MotionController;
import com.vectoros.robot.runtime.navigation.NavigationEngine;
import com.vectoros.robot.runtime.navigation.planner.AxisAlignedMovementPlanner;
import com.vectoros.robot.runtime.position.PositionTracker;
import com.vectoros.robot.runtime.world.Coordinate;
import com.vectoros.robot.runtime.world.WarehouseWorld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MissionManagerTest {

    private static final Instant FIXED = Instant.parse("2026-07-26T10:00:00Z");

    private InMemoryRuntimeEventBus eventBus;
    private RobotState state;
    private NavigationEngine navigationEngine;
    private MissionManager missionManager;

    @BeforeEach
    void setUp() {
        eventBus = new InMemoryRuntimeEventBus();
        state = RobotState.initial("mission-robot");
        SimulationHardwareFactory hardware = SimulationHardwareFactory.createDefault();
        Clock clock = Clock.fixed(FIXED, ZoneOffset.UTC);
        navigationEngine = new NavigationEngine(
                state,
                new AxisAlignedMovementPlanner(),
                new MotionController(hardware.movementHardware()),
                new PositionTracker(hardware.positionHardware()),
                WarehouseWorld.square(20),
                eventBus,
                clock);
        missionManager = new MissionManager("mission-robot", navigationEngine, WarehouseWorld.square(20), eventBus, clock);
    }

    @Test
    void assignMissionStartsLifecycleAndEmitsStartedEvent() {
        Mission mission = Mission.of(
                "m-1",
                FIXED,
                MissionStep.navigate("n1", new Coordinate(1, 0)));

        missionManager.assignMission(mission);

        assertThat(mission.status()).isEqualTo(MissionStatus.RUNNING);
        assertThat(missionManager.hasActiveMission()).isTrue();
        assertThat(eventBus.historyOfType(MissionStartedEvent.class)).hasSize(1);
    }

    @Test
    void enforcesSingleActiveMission() {
        missionManager.assignMission(Mission.of(
                "m-1", FIXED, MissionStep.navigate("n1", new Coordinate(1, 0))));

        assertThatThrownBy(() -> missionManager.assignMission(Mission.of(
                "m-2", FIXED, MissionStep.navigate("n2", new Coordinate(2, 0)))))
                .isInstanceOf(IllegalMissionStateException.class)
                .hasMessageContaining("while mission 'm-1' is active");
    }

    @Test
    void navigateMissionCompletesWhenDestinationReached() {
        Mission mission = Mission.of(
                "m-nav",
                FIXED,
                MissionStep.navigate("to-2", new Coordinate(2, 0)));
        missionManager.assignMission(mission);

        MissionResult first = missionManager.tick();
        assertThat(first.status()).isEqualTo(MissionResult.Status.IN_PROGRESS);
        assertThat(state.position().x()).isEqualTo(1.0);

        MissionResult second = missionManager.tick();
        assertThat(second.status()).isEqualTo(MissionResult.Status.COMPLETED);
        assertThat(mission.status()).isEqualTo(MissionStatus.COMPLETED);
        assertThat(missionManager.hasActiveMission()).isFalse();
        assertThat(eventBus.historyOfType(MissionCompletedEvent.class)).hasSize(1);
        assertThat(eventBus.historyOfType(MissionStepCompletedEvent.class)).hasSize(1);
    }

    @Test
    void multiStepMissionProgressesThroughWaitAndNavigate() {
        Mission mission = Mission.of(
                "m-multi",
                FIXED,
                MissionStep.waitTicks("pause", 2),
                MissionStep.navigate("go", new Coordinate(1, 0)));
        missionManager.assignMission(mission);

        assertThat(missionManager.tick().status()).isEqualTo(MissionResult.Status.IN_PROGRESS);
        assertThat(eventBus.historyOfType(MissionStepStartedEvent.class)).hasSize(1);

        MissionResult afterWait = missionManager.tick();
        // WAIT completes and NAVIGATE begins + may move in same tick
        assertThat(afterWait.status()).isIn(
                MissionResult.Status.IN_PROGRESS,
                MissionResult.Status.COMPLETED);
        assertThat(eventBus.historyOfType(MissionStepCompletedEvent.class)).isNotEmpty();

        while (missionManager.hasActiveMission()) {
            missionManager.tick();
        }

        assertThat(mission.status()).isEqualTo(MissionStatus.COMPLETED);
        assertThat(state.position().x()).isEqualTo(1.0);
        assertThat(eventBus.historyOfType(MissionStepCompletedEvent.class)).hasSize(2);
    }

    @Test
    void cancelMissionEmitsCancelledAndClearsActive() {
        Mission mission = Mission.of(
                "m-cancel",
                FIXED,
                MissionStep.navigate("far", new Coordinate(10, 0)));
        missionManager.assignMission(mission);
        missionManager.tick();

        missionManager.cancelMission();

        assertThat(mission.status()).isEqualTo(MissionStatus.CANCELLED);
        assertThat(missionManager.hasActiveMission()).isFalse();
        assertThat(navigationEngine.isNavigating()).isFalse();
        assertThat(eventBus.historyOfType(MissionCancelledEvent.class)).hasSize(1);
        assertThat(missionManager.tick().status()).isEqualTo(MissionResult.Status.IDLE);
    }

    @Test
    void navigationFailureFailsMission() {
        NavigationEngine failingNav = new NavigationEngine(
                state,
                (current, heading, destination, speed) ->
                        com.vectoros.robot.runtime.navigation.MovementCommand.moveForward(
                                new Coordinate(50, 0),
                                com.vectoros.robot.runtime.navigation.Heading.EAST,
                                1.0),
                new MotionController(SimulationHardwareFactory.createDefault().movementHardware()),
                new PositionTracker(SimulationHardwareFactory.createDefault().positionHardware()),
                WarehouseWorld.square(10),
                eventBus,
                Clock.fixed(FIXED, ZoneOffset.UTC));
        MissionManager manager = new MissionManager(
                "mission-robot", failingNav, WarehouseWorld.square(10), eventBus, Clock.fixed(FIXED, ZoneOffset.UTC));

        Mission mission = Mission.of(
                "m-fail",
                FIXED,
                MissionStep.navigate("oob", new Coordinate(1, 0)));
        manager.assignMission(mission);

        MissionResult result = manager.tick();

        assertThat(result.status()).isEqualTo(MissionResult.Status.FAILED);
        assertThat(mission.status()).isEqualTo(MissionStatus.FAILED);
        assertThat(eventBus.historyOfType(MissionFailedEvent.class)).hasSize(1);
    }

    @Test
    void unsupportedStepTypeFailsMission() {
        MissionStep unsupported = MissionStep.navigate("x", new Coordinate(0, 0));
        // Build mission with reserved type via reflection-free approach: use a custom step list
        Mission mission = new Mission(
                "m-unsupported",
                java.util.List.of(newUnsupportedPickupStep()),
                FIXED);
        missionManager.assignMission(mission);

        MissionResult result = missionManager.tick();

        assertThat(result.status()).isEqualTo(MissionResult.Status.FAILED);
        assertThat(eventBus.historyOfType(MissionFailedEvent.class).getFirst().reason())
                .contains("PICKUP");
    }

    @Test
    void missionRejectsEmptySteps() {
        assertThatThrownBy(() -> new Mission("empty", java.util.List.of(), FIXED))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static MissionStep newUnsupportedPickupStep() {
        try {
            var constructor = MissionStep.class.getDeclaredConstructor(
                    String.class, MissionStepType.class, Coordinate.class, int.class);
            constructor.setAccessible(true);
            return constructor.newInstance("pickup-1", MissionStepType.PICKUP, null, 0);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
