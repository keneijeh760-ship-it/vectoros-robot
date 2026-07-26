package com.vectoros.robot.runtime.navigation;

import com.vectoros.robot.runtime.events.InMemoryRuntimeEventBus;
import com.vectoros.robot.runtime.hal.simulation.SimulationHardwareFactory;
import com.vectoros.robot.runtime.model.BatteryState;
import com.vectoros.robot.runtime.model.Position;
import com.vectoros.robot.runtime.model.RobotState;
import com.vectoros.robot.runtime.model.RobotStatus;
import com.vectoros.robot.runtime.motion.MotionController;
import com.vectoros.robot.runtime.navigation.events.DestinationReachedEvent;
import com.vectoros.robot.runtime.navigation.events.MovementStepCompletedEvent;
import com.vectoros.robot.runtime.navigation.events.NavigationFailedEvent;
import com.vectoros.robot.runtime.navigation.events.NavigationStartedEvent;
import com.vectoros.robot.runtime.navigation.planner.AxisAlignedMovementPlanner;
import com.vectoros.robot.runtime.navigation.planner.MovementPlanner;
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

class NavigationEngineTest {

    private InMemoryRuntimeEventBus eventBus;
    private RobotState state;
    private NavigationEngine navigationEngine;

    @BeforeEach
    void setUp() {
        eventBus = new InMemoryRuntimeEventBus();
        state = RobotState.initial("nav-robot");
        SimulationHardwareFactory hardware = SimulationHardwareFactory.createDefault();
        Clock clock = Clock.fixed(Instant.parse("2026-07-22T22:00:00Z"), ZoneOffset.UTC);
        navigationEngine = new NavigationEngine(
                state,
                new AxisAlignedMovementPlanner(),
                new MotionController(hardware.movementHardware()),
                new PositionTracker(hardware.positionHardware()),
                WarehouseWorld.square(10),
                eventBus,
                clock);
    }

    @Test
    void startNavigationEmitsEvent() {
        navigationEngine.startNavigation(new Coordinate(2, 0));

        assertThat(navigationEngine.isNavigating()).isTrue();
        assertThat(eventBus.historyOfType(NavigationStartedEvent.class)).hasSize(1);
    }

    @Test
    void rejectsDestinationOutsideBounds() {
        assertThatThrownBy(() -> navigationEngine.startNavigation(new Coordinate(20, 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("outside warehouse bounds");
    }

    @Test
    void tickMovesOneCellPerTickUntilDestinationReached() {
        navigationEngine.startNavigation(new Coordinate(2, 0));

        NavigationResult first = navigationEngine.tick();
        assertThat(first.status()).isEqualTo(NavigationResult.Status.IN_PROGRESS);
        assertThat(state.position().x()).isEqualTo(1.0);

        NavigationResult second = navigationEngine.tick();
        assertThat(second.status()).isEqualTo(NavigationResult.Status.DESTINATION_REACHED);
        assertThat(state.position().x()).isEqualTo(2.0);
        assertThat(navigationEngine.isNavigating()).isFalse();
        assertThat(eventBus.historyOfType(DestinationReachedEvent.class)).hasSize(1);
        assertThat(eventBus.historyOfType(MovementStepCompletedEvent.class)).hasSize(2);
    }

    @Test
    void tickTurnsThenMovesForNorthDestination() {
        navigationEngine.startNavigation(new Coordinate(0, 1));

        NavigationResult turn = navigationEngine.tick();
        assertThat(turn.status()).isEqualTo(NavigationResult.Status.IN_PROGRESS);
        assertThat(state.heading()).isEqualTo(Heading.NORTH);
        assertThat(state.position()).isEqualTo(Position.origin());

        NavigationResult move = navigationEngine.tick();
        assertThat(move.status()).isEqualTo(NavigationResult.Status.DESTINATION_REACHED);
        assertThat(state.position().y()).isEqualTo(1.0);
    }

    @Test
    void failsWhenPlannerRequestsOutOfBoundsStep() {
        MovementPlanner unsafePlanner = (current, currentHeading, destination, defaultMoveSpeed) ->
                MovementCommand.moveForward(new Coordinate(10, 0), Heading.EAST, 1.0);

        SimulationHardwareFactory hardware = SimulationHardwareFactory.createDefault();
        InMemoryRuntimeEventBus bus = new InMemoryRuntimeEventBus();
        NavigationEngine engine = new NavigationEngine(
                RobotState.initial("unsafe"),
                unsafePlanner,
                new MotionController(hardware.movementHardware()),
                new PositionTracker(hardware.positionHardware()),
                WarehouseWorld.square(10),
                bus,
                Clock.systemUTC());

        engine.startNavigation(new Coordinate(1, 0));
        NavigationResult result = engine.tick();

        assertThat(result.status()).isEqualTo(NavigationResult.Status.FAILED);
        assertThat(bus.historyOfType(NavigationFailedEvent.class)).hasSize(1);
        assertThat(engine.isNavigating()).isFalse();
    }

    @Test
    void cancelStopsNavigation() {
        navigationEngine.startNavigation(new Coordinate(5, 0));
        navigationEngine.cancelNavigation();

        assertThat(navigationEngine.isNavigating()).isFalse();
        assertThat(navigationEngine.tick().status()).isEqualTo(NavigationResult.Status.IDLE);
    }

    @Test
    void alreadyAtDestinationCompletesImmediately() {
        RobotState atDest = new RobotState(
                "at-dest",
                RobotStatus.IDLE,
                BatteryState.full(),
                new Position(3, 0),
                null,
                Heading.EAST,
                0.0,
                Instant.EPOCH);
        SimulationHardwareFactory hardware =
                SimulationHardwareFactory.create(new Position(3, 0), 0.0, 100.0);
        InMemoryRuntimeEventBus bus = new InMemoryRuntimeEventBus();
        NavigationEngine engine = new NavigationEngine(
                atDest,
                new AxisAlignedMovementPlanner(),
                new MotionController(hardware.movementHardware()),
                new PositionTracker(hardware.positionHardware()),
                WarehouseWorld.square(10),
                bus,
                Clock.systemUTC());

        engine.startNavigation(new Coordinate(3, 0));
        NavigationResult result = engine.tick();

        assertThat(result.status()).isEqualTo(NavigationResult.Status.DESTINATION_REACHED);
        assertThat(bus.historyOfType(DestinationReachedEvent.class)).hasSize(1);
    }
}
