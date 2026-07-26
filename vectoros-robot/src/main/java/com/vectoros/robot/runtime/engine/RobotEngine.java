package com.vectoros.robot.runtime.engine;

import com.vectoros.robot.messaging.NoOpRobotEventPublisher;
import com.vectoros.robot.messaging.RobotEventPublisher;
import com.vectoros.robot.messaging.RobotPositionMessage;
import com.vectoros.robot.messaging.RobotStatusMessage;
import com.vectoros.robot.runtime.energy.EnergyManager;
import com.vectoros.robot.runtime.energy.FixedStepEnergyConsumptionModel;
import com.vectoros.robot.runtime.events.PositionChangedEvent;
import com.vectoros.robot.runtime.events.RuntimeEventPublisher;
import com.vectoros.robot.runtime.events.TaskCompletedEvent;
import com.vectoros.robot.runtime.events.TaskStartedEvent;
import com.vectoros.robot.runtime.hal.BatteryHardware;
import com.vectoros.robot.runtime.hal.MovementHardware;
import com.vectoros.robot.runtime.hal.PositionHardware;
import com.vectoros.robot.runtime.mission.Mission;
import com.vectoros.robot.runtime.mission.MissionManager;
import com.vectoros.robot.runtime.mission.MissionResult;
import com.vectoros.robot.runtime.mission.MissionStep;
import com.vectoros.robot.runtime.motion.MotionController;
import com.vectoros.robot.runtime.model.Position;
import com.vectoros.robot.runtime.model.RobotState;
import com.vectoros.robot.runtime.model.RobotStatus;
import com.vectoros.robot.runtime.model.RobotTask;
import com.vectoros.robot.runtime.navigation.Heading;
import com.vectoros.robot.runtime.navigation.NavigationEngine;
import com.vectoros.robot.runtime.navigation.planner.AxisAlignedMovementPlanner;
import com.vectoros.robot.runtime.position.PositionTracker;
import com.vectoros.robot.runtime.state.RobotStateEvent;
import com.vectoros.robot.runtime.state.RobotStateMachine;
import com.vectoros.robot.runtime.task.TaskExecutor;
import com.vectoros.robot.runtime.world.Coordinate;
import com.vectoros.robot.runtime.world.WarehouseWorld;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

/**
 * Coordinates runtime components and owns the deterministic tick loop.
 * Mission execution is delegated to {@link MissionManager}.
 * Energy updates are delegated exclusively to {@link EnergyManager}.
 * Status changes are delegated exclusively to {@link RobotStateMachine}.
 * Fleet-facing events go through {@link RobotEventPublisher} (never MQTT directly).
 */
public final class RobotEngine {

    private final RobotState state;
    private final RobotStateMachine stateMachine;
    private final MissionManager missionManager;
    private final NavigationEngine navigationEngine;
    private final MotionController motionController;
    private final EnergyManager energyManager;
    private final PositionTracker positionTracker;
    private final TaskExecutor taskExecutor;
    private final RuntimeEventPublisher eventPublisher;
    private final RobotEventPublisher robotEventPublisher;
    private final Clock clock;

    private boolean running;
    private RobotStatus lastPublishedStatus;

    public RobotEngine(
            RobotState state,
            RobotStateMachine stateMachine,
            MissionManager missionManager,
            NavigationEngine navigationEngine,
            MotionController motionController,
            EnergyManager energyManager,
            PositionTracker positionTracker,
            TaskExecutor taskExecutor,
            RuntimeEventPublisher eventPublisher,
            RobotEventPublisher robotEventPublisher,
            Clock clock) {
        this.state = Objects.requireNonNull(state, "state");
        this.stateMachine = Objects.requireNonNull(stateMachine, "stateMachine");
        if (stateMachine.state() != state) {
            throw new IllegalArgumentException("stateMachine must wrap the same RobotState instance");
        }
        this.missionManager = Objects.requireNonNull(missionManager, "missionManager");
        this.navigationEngine = Objects.requireNonNull(navigationEngine, "navigationEngine");
        this.motionController = Objects.requireNonNull(motionController, "motionController");
        this.energyManager = Objects.requireNonNull(energyManager, "energyManager");
        this.positionTracker = Objects.requireNonNull(positionTracker, "positionTracker");
        this.taskExecutor = Objects.requireNonNull(taskExecutor, "taskExecutor");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher");
        this.robotEventPublisher = Objects.requireNonNull(robotEventPublisher, "robotEventPublisher");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public static RobotEngine create(
            String robotId,
            MovementHardware movementHardware,
            BatteryHardware batteryHardware,
            PositionHardware positionHardware,
            RuntimeEventPublisher eventPublisher) {
        return create(
                robotId,
                movementHardware,
                batteryHardware,
                positionHardware,
                eventPublisher,
                NoOpRobotEventPublisher.INSTANCE,
                WarehouseWorld.square(50));
    }

    public static RobotEngine create(
            String robotId,
            MovementHardware movementHardware,
            BatteryHardware batteryHardware,
            PositionHardware positionHardware,
            RuntimeEventPublisher eventPublisher,
            WarehouseWorld world) {
        return create(
                robotId,
                movementHardware,
                batteryHardware,
                positionHardware,
                eventPublisher,
                NoOpRobotEventPublisher.INSTANCE,
                world);
    }

    public static RobotEngine create(
            String robotId,
            MovementHardware movementHardware,
            BatteryHardware batteryHardware,
            PositionHardware positionHardware,
            RuntimeEventPublisher eventPublisher,
            RobotEventPublisher robotEventPublisher) {
        return create(
                robotId,
                movementHardware,
                batteryHardware,
                positionHardware,
                eventPublisher,
                robotEventPublisher,
                WarehouseWorld.square(50));
    }

    public static RobotEngine create(
            String robotId,
            MovementHardware movementHardware,
            BatteryHardware batteryHardware,
            PositionHardware positionHardware,
            RuntimeEventPublisher eventPublisher,
            RobotEventPublisher robotEventPublisher,
            WarehouseWorld world) {
        RobotState state = RobotState.initial(robotId);
        Clock clock = Clock.systemUTC();
        MotionController motionController = new MotionController(movementHardware);
        PositionTracker positionTracker = new PositionTracker(positionHardware);
        NavigationEngine navigationEngine = new NavigationEngine(
                state,
                new AxisAlignedMovementPlanner(),
                motionController,
                positionTracker,
                world,
                eventPublisher,
                clock);
        MissionManager missionManager = new MissionManager(
                robotId, navigationEngine, world, eventPublisher, robotEventPublisher, clock);
        EnergyManager energyManager = new EnergyManager(
                robotId,
                batteryHardware,
                new FixedStepEnergyConsumptionModel(),
                eventPublisher,
                robotEventPublisher,
                clock);
        return new RobotEngine(
                state,
                new RobotStateMachine(state, clock),
                missionManager,
                navigationEngine,
                motionController,
                energyManager,
                positionTracker,
                new TaskExecutor(),
                eventPublisher,
                robotEventPublisher,
                clock);
    }

    public void start() {
        if (running) {
            return;
        }
        running = true;
        Instant now = clock.instant();
        state.updateHeartbeat(now);

        if (stateMachine.currentStatus() == RobotStatus.OFFLINE) {
            stateMachine.transition(RobotStateEvent.POWER_ON);
            stateMachine.transition(RobotStateEvent.INITIALIZATION_COMPLETE);
        } else if (stateMachine.currentStatus() == RobotStatus.ERROR) {
            stateMachine.transition(RobotStateEvent.ERROR_CLEARED);
        } else if (stateMachine.currentStatus() == RobotStatus.INITIALIZING) {
            stateMachine.transition(RobotStateEvent.INITIALIZATION_COMPLETE);
        }

        syncPoseFromHardware();
        syncBatteryFromEnergyManager();
        publishStatusIfChanged(now);
    }

    public void shutdown() {
        missionManager.cancelMission();
        navigationEngine.cancelNavigation();
        motionController.stop();
        state.updateSpeed(0.0);
        stateMachine.transitionToOffline();
        running = false;
        Instant now = clock.instant();
        state.updateHeartbeat(now);
        publishStatusIfChanged(now);
    }

    public void assignMission(Mission mission) {
        Objects.requireNonNull(mission, "mission");
        if (!running) {
            throw new IllegalStateException("Cannot assign mission while runtime is not started");
        }
        if (energyManager.currentBattery().isDepleted()) {
            throw new IllegalStateException("Cannot assign mission while battery is empty");
        }

        missionManager.assignMission(mission);
        Position taskTarget = mission.steps().stream()
                .filter(step -> step.target().isPresent())
                .findFirst()
                .flatMap(MissionStep::target)
                .map(Coordinate::toPosition)
                .orElse(state.position());
        RobotTask task = new RobotTask(mission.missionId(), taskTarget);
        state.assignTask(task);
        stateMachine.transition(RobotStateEvent.TASK_RECEIVED);

        Instant now = clock.instant();
        state.updateHeartbeat(now);
        eventPublisher.publish(new TaskStartedEvent(state.robotId(), mission.missionId(), now));
        publishStatusIfChanged(now);
    }

    public void assignTask(RobotTask task) {
        Objects.requireNonNull(task, "task");
        Mission mission = Mission.of(
                task.taskId(),
                clock.instant(),
                MissionStep.navigate("navigate-" + task.taskId(), Coordinate.fromPosition(task.target())));
        assignMission(mission);
    }

    public void tick() {
        if (!running) {
            return;
        }

        Instant now = clock.instant();
        state.updateHeartbeat(now);

        Position previousPosition = state.position();

        if (missionManager.hasActiveMission()
                && stateMachine.currentStatus() == RobotStatus.TASK_ASSIGNED
                && !energyManager.currentBattery().isDepleted()) {
            stateMachine.transition(RobotStateEvent.START_MOVE_TO_PICKUP);
        }

        MissionResult missionResult = MissionResult.idle();
        if (missionManager.hasActiveMission() && !energyManager.currentBattery().isDepleted()) {
            missionResult = missionManager.tick();
        } else if (!missionManager.hasActiveMission() && !navigationEngine.isNavigating()) {
            motionController.stop();
        }

        syncPoseFromHardware();

        boolean movementOccurred = !previousPosition.equals(state.position());
        if (movementOccurred) {
            energyManager.consumeEnergyForMovementStep(state.speed());
            syncBatteryFromEnergyManager();
            publishPosition(now);
            eventPublisher.publish(
                    new PositionChangedEvent(state.robotId(), previousPosition, state.position(), now));
        }

        applyMissionResult(missionResult, now);

        if (energyManager.currentBattery().isDepleted() && isMissionStatus(stateMachine.currentStatus())) {
            missionManager.cancelMission();
            navigationEngine.cancelNavigation();
            motionController.stop();
            state.updateSpeed(0.0);
            state.clearTask();
            stateMachine.transition(RobotStateEvent.FAULT_DETECTED);
        }

        publishStatusIfChanged(now);
    }

    public void cancelMission() {
        missionManager.cancelMission();
        navigationEngine.cancelNavigation();
        motionController.stop();
        state.updateSpeed(0.0);
        state.clearTask();
        if (isMissionStatus(stateMachine.currentStatus())) {
            stateMachine.transition(RobotStateEvent.FAULT_DETECTED);
            stateMachine.transition(RobotStateEvent.ERROR_CLEARED);
        }
        publishStatusIfChanged(clock.instant());
    }

    private void applyMissionResult(MissionResult missionResult, Instant now) {
        switch (missionResult.status()) {
            case COMPLETED -> {
                String missionId = missionResult.mission()
                        .map(Mission::missionId)
                        .orElseGet(() -> state.currentTask().map(RobotTask::taskId).orElse("unknown"));
                motionController.stop();
                state.updateSpeed(0.0);
                state.clearTask();
                stateMachine.advanceMissionToIdle();
                eventPublisher.publish(new TaskCompletedEvent(state.robotId(), missionId, now));
            }
            case FAILED -> {
                motionController.stop();
                state.updateSpeed(0.0);
                state.clearTask();
                if (isMissionStatus(stateMachine.currentStatus())) {
                    stateMachine.transition(RobotStateEvent.FAULT_DETECTED);
                }
            }
            case IDLE, IN_PROGRESS, STEP_COMPLETED, CANCELLED -> {
                // no-op
            }
        }
    }

    public boolean isRunning() {
        return running;
    }

    public RobotState state() {
        return state;
    }

    public RobotStateMachine stateMachine() {
        return stateMachine;
    }

    public MissionManager missionManager() {
        return missionManager;
    }

    public NavigationEngine navigationEngine() {
        return navigationEngine;
    }

    public EnergyManager energyManager() {
        return energyManager;
    }

    public RobotEventPublisher robotEventPublisher() {
        return robotEventPublisher;
    }

    private void publishStatusIfChanged(Instant now) {
        RobotStatus current = stateMachine.currentStatus();
        if (current == lastPublishedStatus) {
            return;
        }
        lastPublishedStatus = current;
        robotEventPublisher.publishStatus(new RobotStatusMessage(
                state.robotId(), current.name(), now));
    }

    private void publishPosition(Instant now) {
        Position position = state.position();
        robotEventPublisher.publishPosition(new RobotPositionMessage(
                state.robotId(),
                position.x(),
                position.y(),
                state.heading().name(),
                now));
    }

    private static boolean isMissionStatus(RobotStatus status) {
        return status == RobotStatus.TASK_ASSIGNED
                || status == RobotStatus.MOVING_TO_PICKUP
                || status == RobotStatus.LOADING
                || status == RobotStatus.MOVING_TO_DROPOFF
                || status == RobotStatus.UNLOADING
                || status == RobotStatus.RETURNING;
    }

    private void syncPoseFromHardware() {
        Position position = positionTracker.currentPosition();
        double headingDegrees = positionTracker.currentHeadingDegrees();
        double speed = motionController.readSpeed();
        state.updatePosition(position);
        state.updateHeading(Heading.fromDegrees(headingDegrees));
        state.updateSpeed(speed);
    }

    private void syncBatteryFromEnergyManager() {
        state.updateBattery(energyManager.currentBattery().toBatteryState());
    }
}
