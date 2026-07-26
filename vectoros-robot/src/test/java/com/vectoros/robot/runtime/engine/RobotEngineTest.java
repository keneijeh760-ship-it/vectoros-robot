package com.vectoros.robot.runtime.engine;

import com.vectoros.robot.runtime.energy.EnergyManager;
import com.vectoros.robot.runtime.energy.FixedStepEnergyConsumptionModel;
import com.vectoros.robot.runtime.energy.events.BatteryDepletedEvent;
import com.vectoros.robot.runtime.energy.events.BatteryLowEvent;
import com.vectoros.robot.runtime.events.InMemoryRuntimeEventBus;
import com.vectoros.robot.runtime.events.PositionChangedEvent;
import com.vectoros.robot.runtime.events.TaskCompletedEvent;
import com.vectoros.robot.runtime.events.TaskStartedEvent;
import com.vectoros.robot.runtime.hal.simulation.SimulationHardwareFactory;
import com.vectoros.robot.runtime.mission.Mission;
import com.vectoros.robot.runtime.mission.MissionManager;
import com.vectoros.robot.runtime.mission.MissionStatus;
import com.vectoros.robot.runtime.mission.MissionStep;
import com.vectoros.robot.runtime.mission.events.MissionCompletedEvent;
import com.vectoros.robot.runtime.mission.events.MissionStartedEvent;
import com.vectoros.robot.runtime.model.Position;
import com.vectoros.robot.runtime.model.RobotState;
import com.vectoros.robot.runtime.model.RobotStatus;
import com.vectoros.robot.runtime.model.RobotTask;
import com.vectoros.robot.runtime.motion.MotionController;
import com.vectoros.robot.runtime.navigation.NavigationEngine;
import com.vectoros.robot.runtime.navigation.events.DestinationReachedEvent;
import com.vectoros.robot.runtime.navigation.events.NavigationStartedEvent;
import com.vectoros.robot.runtime.navigation.planner.AxisAlignedMovementPlanner;
import com.vectoros.robot.runtime.position.PositionTracker;
import com.vectoros.robot.runtime.state.RobotStateMachine;
import com.vectoros.robot.runtime.task.TaskExecutor;
import com.vectoros.robot.runtime.world.Coordinate;
import com.vectoros.robot.runtime.world.WarehouseWorld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RobotEngineTest {

    private InMemoryRuntimeEventBus eventBus;
    private RobotEngine engine;

    @BeforeEach
    void setUp() {
        eventBus = new InMemoryRuntimeEventBus();
        SimulationHardwareFactory hardware = SimulationHardwareFactory.createDefault();
        engine = RobotEngine.create(
                "robot-1",
                hardware.movementHardware(),
                hardware.batteryHardware(),
                hardware.positionHardware(),
                eventBus);
    }

    @Test
    void startTransitionsOfflineThroughInitializingToIdle() {
        engine.start();

        assertThat(engine.isRunning()).isTrue();
        assertThat(engine.state().status()).isEqualTo(RobotStatus.IDLE);
        assertThat(engine.state().lastHeartbeat()).isNotEqualTo(Instant.EPOCH);
    }

    @Test
    void shutdownStopsRuntimeAndMarksOffline() {
        engine.start();
        engine.assignTask(new RobotTask("task-1", new Position(5, 0)));
        engine.tick();

        engine.shutdown();

        assertThat(engine.isRunning()).isFalse();
        assertThat(engine.state().status()).isEqualTo(RobotStatus.OFFLINE);
        assertThat(engine.state().speed()).isZero();
    }

    @Test
    void assignTaskRequiresStartedEngine() {
        assertThatThrownBy(() -> engine.assignTask(new RobotTask("task-1", new Position(1, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void assignTaskDelegatesToMissionManager() {
        engine.start();
        engine.assignTask(new RobotTask("task-1", new Position(3, 0)));

        assertThat(engine.state().status()).isEqualTo(RobotStatus.TASK_ASSIGNED);
        assertThat(engine.missionManager().hasActiveMission()).isTrue();
        assertThat(eventBus.historyOfType(TaskStartedEvent.class)).hasSize(1);
        assertThat(eventBus.historyOfType(MissionStartedEvent.class)).hasSize(1);
    }

    @Test
    void assignMissionExecutesMultiStepThroughNavigation() {
        engine.start();
        Mission mission = Mission.of(
                "mission-1",
                Instant.parse("2026-07-26T12:00:00Z"),
                MissionStep.navigate("to-x", new Coordinate(2, 0)),
                MissionStep.waitTicks("dwell", 1));

        engine.assignMission(mission);

        for (int i = 0; i < 10; i++) {
            engine.tick();
            if (!engine.missionManager().hasActiveMission()) {
                break;
            }
        }

        assertThat(mission.status()).isEqualTo(MissionStatus.COMPLETED);
        assertThat(engine.state().status()).isEqualTo(RobotStatus.IDLE);
        assertThat(engine.state().position().x()).isEqualTo(2.0);
        assertThat(eventBus.historyOfType(MissionCompletedEvent.class)).hasSize(1);
        assertThat(eventBus.historyOfType(TaskCompletedEvent.class)).hasSize(1);
        assertThat(eventBus.historyOfType(DestinationReachedEvent.class)).hasSize(1);
    }

    @Test
    void tickAdvancesToMovingAndEmitsPositionChanged() {
        engine.start();
        engine.assignTask(new RobotTask("task-1", new Position(3, 0)));

        engine.tick();

        assertThat(engine.state().status()).isEqualTo(RobotStatus.MOVING_TO_PICKUP);
        assertThat(engine.state().position().x()).isEqualTo(1.0);
        assertThat(eventBus.historyOfType(PositionChangedEvent.class)).isNotEmpty();
        assertThat(eventBus.historyOfType(NavigationStartedEvent.class)).isNotEmpty();
    }

    @Test
    void tickCompletesTaskWhenDestinationReached() {
        engine.start();
        engine.assignTask(new RobotTask("task-1", new Position(2, 0)));

        for (int i = 0; i < 5; i++) {
            engine.tick();
        }

        assertThat(engine.state().currentTask()).isEmpty();
        assertThat(engine.state().status()).isEqualTo(RobotStatus.IDLE);
        assertThat(eventBus.historyOfType(TaskCompletedEvent.class)).hasSize(1);
        assertThat(eventBus.historyOfType(DestinationReachedEvent.class)).hasSize(1);
        assertThat(engine.state().position().x()).isEqualTo(2.0);
    }

    @Test
    void cancelMissionRestoresIdle() {
        engine.start();
        engine.assignMission(Mission.of(
                "cancel-me",
                Instant.now(),
                MissionStep.navigate("far", new Coordinate(8, 0))));
        engine.tick();

        engine.cancelMission();

        assertThat(engine.missionManager().hasActiveMission()).isFalse();
        assertThat(engine.state().status()).isEqualTo(RobotStatus.IDLE);
        assertThat(engine.state().currentTask()).isEmpty();
    }

    @Test
    void tickIsNoOpWhenNotRunning() {
        engine.tick();
        assertThat(engine.state().position()).isEqualTo(Position.origin());
        assertThat(eventBus.history()).isEmpty();
    }

    @Test
    void successfulMovementConsumesEnergy() {
        engine.start();
        double before = engine.energyManager().currentBattery().percentage();
        engine.assignTask(new RobotTask("task-1", new Position(2, 0)));

        engine.tick(); // MOVE_FORWARD one cell

        assertThat(engine.state().position().x()).isEqualTo(1.0);
        assertThat(engine.energyManager().currentBattery().percentage()).isEqualTo(before - 1.0);
        assertThat(engine.state().battery().percentage()).isEqualTo(before - 1.0);
    }

    @Test
    void noMovementDoesNotConsumeEnergy() {
        engine.start();
        double before = engine.energyManager().currentBattery().percentage();

        engine.tick();
        engine.tick();

        assertThat(engine.energyManager().currentBattery().percentage()).isEqualTo(before);
        assertThat(engine.state().position()).isEqualTo(Position.origin());
    }

    @Test
    void waitStepDoesNotConsumeEnergy() {
        engine.start();
        double before = engine.energyManager().currentBattery().percentage();
        engine.assignMission(Mission.of(
                "wait-only",
                Instant.now(),
                MissionStep.waitTicks("pause", 3)));

        engine.tick();
        engine.tick();

        assertThat(engine.energyManager().currentBattery().percentage()).isEqualTo(before);
    }

    @Test
    void batteryLowEventIsPublishedOnceUntilRecovered() {
        SimulationHardwareFactory hardware =
                SimulationHardwareFactory.create(Position.origin(), 0, 20.05);
        InMemoryRuntimeEventBus bus = new InMemoryRuntimeEventBus();
        RobotEngine lowBatteryEngine = RobotEngine.create(
                "robot-low",
                hardware.movementHardware(),
                hardware.batteryHardware(),
                hardware.positionHardware(),
                bus);

        lowBatteryEngine.start();
        lowBatteryEngine.assignTask(new RobotTask("task-1", new Position(10, 0)));
        lowBatteryEngine.tick();
        lowBatteryEngine.tick();

        assertThat(bus.historyOfType(BatteryLowEvent.class)).hasSize(1);
    }

    @Test
    void engineUsesInjectedCollaborators() {
        Instant fixed = Instant.parse("2026-07-22T18:00:00Z");
        SimulationHardwareFactory hardware = SimulationHardwareFactory.createDefault();
        InMemoryRuntimeEventBus bus = new InMemoryRuntimeEventBus();
        Clock clock = Clock.fixed(fixed, ZoneOffset.UTC);
        RobotState state = RobotState.initial("robot-clock");
        RobotStateMachine stateMachine = new RobotStateMachine(state, clock);
        MotionController motionController = new MotionController(hardware.movementHardware());
        PositionTracker positionTracker = new PositionTracker(hardware.positionHardware());
        NavigationEngine navigationEngine = new NavigationEngine(
                state,
                new AxisAlignedMovementPlanner(),
                motionController,
                positionTracker,
                WarehouseWorld.square(20),
                bus,
                clock);
        MissionManager missionManager = new MissionManager(
                "robot-clock", navigationEngine, WarehouseWorld.square(20), bus, clock);
        EnergyManager energyManager = new EnergyManager(
                "robot-clock",
                hardware.batteryHardware(),
                new FixedStepEnergyConsumptionModel(),
                bus,
                clock);

        RobotEngine deterministic = new RobotEngine(
                state,
                stateMachine,
                missionManager,
                navigationEngine,
                motionController,
                energyManager,
                positionTracker,
                new TaskExecutor(),
                bus,
                clock);

        deterministic.start();

        assertThat(deterministic.state().lastHeartbeat()).isEqualTo(fixed);
        assertThat(deterministic.state().status()).isEqualTo(RobotStatus.IDLE);
        assertThat(deterministic.stateMachine()).isSameAs(stateMachine);
        assertThat(deterministic.navigationEngine()).isSameAs(navigationEngine);
        assertThat(deterministic.missionManager()).isSameAs(missionManager);
        assertThat(deterministic.energyManager()).isSameAs(energyManager);
    }

    @Test
    void emptyBatteryDuringMissionTransitionsToError() {
        SimulationHardwareFactory hardware =
                SimulationHardwareFactory.create(Position.origin(), 0, 0.05);
        InMemoryRuntimeEventBus bus = new InMemoryRuntimeEventBus();
        RobotEngine lowEngine = RobotEngine.create(
                "robot-empty",
                hardware.movementHardware(),
                hardware.batteryHardware(),
                hardware.positionHardware(),
                bus);

        lowEngine.start();
        lowEngine.assignTask(new RobotTask("task-1", new Position(10, 0)));
        lowEngine.tick();

        assertThat(lowEngine.state().status()).isEqualTo(RobotStatus.ERROR);
        assertThat(lowEngine.missionManager().hasActiveMission()).isFalse();
        assertThat(bus.historyOfType(BatteryDepletedEvent.class)).hasSize(1);
    }

    @Test
    void assignTaskOutsideWorldBoundsIsRejected() {
        SimulationHardwareFactory hardware = SimulationHardwareFactory.createDefault();
        InMemoryRuntimeEventBus bus = new InMemoryRuntimeEventBus();
        RobotEngine smallWorldEngine = RobotEngine.create(
                "small",
                hardware.movementHardware(),
                hardware.batteryHardware(),
                hardware.positionHardware(),
                bus,
                WarehouseWorld.square(5));

        smallWorldEngine.start();

        assertThatThrownBy(() -> smallWorldEngine.assignTask(new RobotTask("task-1", new Position(9, 0))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void secondMissionRejectedWhileFirstActive() {
        engine.start();
        engine.assignMission(Mission.of(
                "first",
                Instant.now(),
                MissionStep.navigate("a", new Coordinate(5, 0))));

        assertThatThrownBy(() -> engine.assignMission(Mission.of(
                "second",
                Instant.now(),
                MissionStep.navigate("b", new Coordinate(1, 0)))))
                .isInstanceOf(IllegalStateException.class);
    }
}
