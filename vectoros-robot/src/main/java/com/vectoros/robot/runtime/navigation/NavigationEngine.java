package com.vectoros.robot.runtime.navigation;

import com.vectoros.robot.runtime.events.RuntimeEventPublisher;
import com.vectoros.robot.runtime.model.RobotState;
import com.vectoros.robot.runtime.motion.MotionController;
import com.vectoros.robot.runtime.navigation.events.DestinationReachedEvent;
import com.vectoros.robot.runtime.navigation.events.MovementStepCompletedEvent;
import com.vectoros.robot.runtime.navigation.events.NavigationFailedEvent;
import com.vectoros.robot.runtime.navigation.events.NavigationStartedEvent;
import com.vectoros.robot.runtime.navigation.planner.MovementPlanner;
import com.vectoros.robot.runtime.position.PositionTracker;
import com.vectoros.robot.runtime.world.Coordinate;
import com.vectoros.robot.runtime.world.WarehouseWorld;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Owns navigation toward a destination for one robot.
 * Designed for orchestration by RobotEngine today and MissionManager later.
 * Never accesses HAL directly.
 */
public final class NavigationEngine {

    private final RobotState state;
    private final MovementPlanner planner;
    private final MotionController motionController;
    private final PositionTracker positionTracker;
    private final WarehouseWorld world;
    private final RuntimeEventPublisher eventPublisher;
    private final Clock clock;
    private final double stepSpeed;

    private Coordinate destination;
    private boolean active;

    public NavigationEngine(
            RobotState state,
            MovementPlanner planner,
            MotionController motionController,
            PositionTracker positionTracker,
            WarehouseWorld world,
            RuntimeEventPublisher eventPublisher,
            Clock clock,
            double stepSpeed) {
        this.state = Objects.requireNonNull(state, "state");
        this.planner = Objects.requireNonNull(planner, "planner");
        this.motionController = Objects.requireNonNull(motionController, "motionController");
        this.positionTracker = Objects.requireNonNull(positionTracker, "positionTracker");
        this.world = Objects.requireNonNull(world, "world");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher");
        this.clock = Objects.requireNonNull(clock, "clock");
        if (stepSpeed <= 0 || !Double.isFinite(stepSpeed)) {
            throw new IllegalArgumentException("stepSpeed must be a positive finite number");
        }
        this.stepSpeed = stepSpeed;
    }

    public NavigationEngine(
            RobotState state,
            MovementPlanner planner,
            MotionController motionController,
            PositionTracker positionTracker,
            WarehouseWorld world,
            RuntimeEventPublisher eventPublisher,
            Clock clock) {
        this(state, planner, motionController, positionTracker, world, eventPublisher, clock, 1.0);
    }

    /**
     * Begin navigating to {@code destination}. Suitable for MissionManager orchestration.
     */
    public void startNavigation(Coordinate destination) {
        Objects.requireNonNull(destination, "destination");
        world.requireContains(destination);
        world.requireContains(Coordinate.fromPosition(state.position()));

        this.destination = destination;
        this.active = true;
        Instant now = clock.instant();
        eventPublisher.publish(new NavigationStartedEvent(state.robotId(), destination, now));
    }

    public void cancelNavigation() {
        if (!active) {
            return;
        }
        active = false;
        destination = null;
        motionController.stop();
        state.updateSpeed(0.0);
    }

    public boolean isNavigating() {
        return active;
    }

    public Optional<Coordinate> destination() {
        return Optional.ofNullable(destination);
    }

    /**
     * Execute one deterministic navigation step.
     */
    public NavigationResult tick() {
        if (!active || destination == null) {
            return NavigationResult.idle();
        }

        Instant now = clock.instant();
        syncPoseFromHardware();

        Coordinate current = Coordinate.fromPosition(state.position());
        if (!world.contains(current)) {
            return fail("Robot is outside warehouse bounds at " + current, now);
        }

        if (current.equals(destination)) {
            return complete(now);
        }

        Heading heading = state.heading();
        MovementCommand command = planner.nextCommand(current, heading, destination, stepSpeed);

        if (command.movementType() == MovementType.STOP) {
            return complete(now);
        }

        if (command.movementType() == MovementType.MOVE_FORWARD) {
            Coordinate next = command.targetCoordinate()
                    .orElseThrow(() -> new IllegalStateException("MOVE_FORWARD missing targetCoordinate"));
            if (!world.contains(next)) {
                return fail("Next cell " + next + " is outside warehouse bounds", now);
            }
        }

        motionController.execute(command);
        syncPoseFromHardware();
        eventPublisher.publish(new MovementStepCompletedEvent(state.robotId(), command, now));

        Coordinate after = Coordinate.fromPosition(state.position());
        if (after.equals(destination)) {
            return complete(now);
        }

        return NavigationResult.inProgress(command);
    }

    private NavigationResult complete(Instant now) {
        motionController.stop();
        state.updateSpeed(0.0);
        Coordinate reached = destination;
        active = false;
        destination = null;
        eventPublisher.publish(new DestinationReachedEvent(state.robotId(), reached, now));
        return NavigationResult.destinationReached();
    }

    private NavigationResult fail(String reason, Instant now) {
        motionController.stop();
        state.updateSpeed(0.0);
        active = false;
        destination = null;
        eventPublisher.publish(new NavigationFailedEvent(state.robotId(), reason, now));
        return NavigationResult.failed(reason);
    }

    private void syncPoseFromHardware() {
        state.updatePosition(positionTracker.currentPosition());
        state.updateHeading(Heading.fromDegrees(positionTracker.currentHeadingDegrees()));
        state.updateSpeed(motionController.readSpeed());
    }
}
