package com.vectoros.robot.runtime.mission;

import com.vectoros.robot.messaging.RobotEventPublisher;
import com.vectoros.robot.messaging.RobotMissionMessage;
import com.vectoros.robot.runtime.events.RuntimeEventPublisher;
import com.vectoros.robot.runtime.mission.events.MissionCancelledEvent;
import com.vectoros.robot.runtime.mission.events.MissionCompletedEvent;
import com.vectoros.robot.runtime.mission.events.MissionFailedEvent;
import com.vectoros.robot.runtime.mission.events.MissionStartedEvent;
import com.vectoros.robot.runtime.mission.events.MissionStepCompletedEvent;
import com.vectoros.robot.runtime.mission.events.MissionStepStartedEvent;
import com.vectoros.robot.runtime.navigation.NavigationEngine;
import com.vectoros.robot.runtime.navigation.NavigationResult;
import com.vectoros.robot.runtime.world.Coordinate;
import com.vectoros.robot.runtime.world.WarehouseWorld;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Highest-level coordinator for mission execution inside the robot runtime.
 * Delegates movement to {@link NavigationEngine}; does not talk to HAL or MQTT.
 */
public final class MissionManager {

    private final String robotId;
    private final NavigationEngine navigationEngine;
    private final WarehouseWorld world;
    private final RuntimeEventPublisher eventPublisher;
    private final RobotEventPublisher robotEventPublisher;
    private final Clock clock;

    private Mission activeMission;
    private boolean stepStarted;
    private boolean navigationStarted;
    private int waitTicksRemaining;

    public MissionManager(
            String robotId,
            NavigationEngine navigationEngine,
            WarehouseWorld world,
            RuntimeEventPublisher eventPublisher,
            RobotEventPublisher robotEventPublisher,
            Clock clock) {
        if (robotId == null || robotId.isBlank()) {
            throw new IllegalArgumentException("robotId must not be blank");
        }
        this.robotId = robotId;
        this.navigationEngine = Objects.requireNonNull(navigationEngine, "navigationEngine");
        this.world = Objects.requireNonNull(world, "world");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher");
        this.robotEventPublisher = Objects.requireNonNull(robotEventPublisher, "robotEventPublisher");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * Accept and start a mission. Enforces a single active mission.
     */
    public void assignMission(Mission mission) {
        Objects.requireNonNull(mission, "mission");
        if (hasActiveMission()) {
            throw new IllegalMissionStateException(
                    "Cannot assign mission '" + mission.missionId()
                            + "' while mission '" + activeMission.missionId() + "' is active");
        }
        if (mission.status() != MissionStatus.CREATED && mission.status() != MissionStatus.QUEUED) {
            throw new IllegalMissionStateException(
                    "Mission '" + mission.missionId() + "' is not assignable from status " + mission.status());
        }
        validateSteps(mission);

        if (mission.status() == MissionStatus.CREATED) {
            mission.markQueued();
        }
        mission.markRunning();
        this.activeMission = mission;
        resetStepExecutionState();

        Instant now = clock.instant();
        eventPublisher.publish(new MissionStartedEvent(robotId, mission.missionId(), now));
        robotEventPublisher.publishMission(new RobotMissionMessage(
                robotId, mission.missionId(), RobotMissionMessage.EventType.STARTED, now));
    }

    public void cancelMission() {
        if (!hasActiveMission()) {
            return;
        }
        Instant now = clock.instant();
        Mission mission = activeMission;
        navigationEngine.cancelNavigation();
        mission.markCancelled(now);
        clearActiveMission();
        eventPublisher.publish(new MissionCancelledEvent(robotId, mission.missionId(), now));
        robotEventPublisher.publishMission(new RobotMissionMessage(
                robotId, mission.missionId(), RobotMissionMessage.EventType.CANCELLED, now));
    }

    public boolean hasActiveMission() {
        return activeMission != null && activeMission.isActive();
    }

    public Optional<Mission> activeMission() {
        return Optional.ofNullable(activeMission);
    }

    /**
     * Execute one mission control cycle.
     */
    public MissionResult tick() {
        if (!hasActiveMission()) {
            return MissionResult.idle();
        }

        Instant now = clock.instant();
        Mission mission = activeMission;
        MissionStep step = mission.currentStep()
                .orElseThrow(() -> new IllegalMissionStateException("Running mission has no current step"));

        if (!stepStarted) {
            beginStep(mission, step, now);
        }

        return switch (step.type()) {
            case NAVIGATE -> tickNavigate(mission, step, now);
            case WAIT -> tickWait(mission, step, now);
            case PICKUP, DROPOFF, DOCK, CHARGE -> failMission(
                    mission,
                    "Mission step type " + step.type() + " is not implemented in Sprint 04",
                    now);
        };
    }

    private MissionResult tickNavigate(Mission mission, MissionStep step, Instant now) {
        Coordinate target = step.target()
                .orElseThrow(() -> new IllegalMissionStateException("NAVIGATE step missing target"));

        if (!navigationStarted) {
            navigationEngine.startNavigation(target);
            navigationStarted = true;
        }

        NavigationResult navigationResult = navigationEngine.tick();
        return switch (navigationResult.status()) {
            case IN_PROGRESS -> MissionResult.inProgress(mission, step);
            case DESTINATION_REACHED -> completeCurrentStep(mission, step, now);
            case FAILED -> failMission(
                    mission,
                    navigationResult.message().orElse("Navigation failed"),
                    now);
            case IDLE -> failMission(mission, "Navigation inactive during NAVIGATE step", now);
        };
    }

    private MissionResult tickWait(Mission mission, MissionStep step, Instant now) {
        if (waitTicksRemaining > 0) {
            waitTicksRemaining--;
        }
        if (waitTicksRemaining > 0) {
            return MissionResult.inProgress(mission, step);
        }
        return completeCurrentStep(mission, step, now);
    }

    private void beginStep(Mission mission, MissionStep step, Instant now) {
        stepStarted = true;
        navigationStarted = false;
        waitTicksRemaining = step.type() == MissionStepType.WAIT ? step.waitTicks() : 0;
        eventPublisher.publish(new MissionStepStartedEvent(
                robotId, mission.missionId(), step.stepId(), step.type(), now));
    }

    private MissionResult completeCurrentStep(Mission mission, MissionStep step, Instant now) {
        eventPublisher.publish(new MissionStepCompletedEvent(
                robotId, mission.missionId(), step.stepId(), step.type(), now));
        mission.advanceStep();
        resetStepExecutionState();

        if (!mission.hasMoreSteps()) {
            mission.markCompleted(now);
            clearActiveMission();
            eventPublisher.publish(new MissionCompletedEvent(robotId, mission.missionId(), now));
            robotEventPublisher.publishMission(new RobotMissionMessage(
                    robotId, mission.missionId(), RobotMissionMessage.EventType.COMPLETED, now));
            return MissionResult.completed(mission);
        }

        return tick();
    }

    private MissionResult failMission(Mission mission, String reason, Instant now) {
        navigationEngine.cancelNavigation();
        mission.markFailed(now);
        clearActiveMission();
        eventPublisher.publish(new MissionFailedEvent(robotId, mission.missionId(), reason, now));
        robotEventPublisher.publishMission(new RobotMissionMessage(
                robotId, mission.missionId(), RobotMissionMessage.EventType.FAILED, now));
        return MissionResult.failed(mission, reason);
    }

    private void resetStepExecutionState() {
        stepStarted = false;
        navigationStarted = false;
        waitTicksRemaining = 0;
    }

    private void clearActiveMission() {
        activeMission = null;
        resetStepExecutionState();
    }

    private void validateSteps(Mission mission) {
        for (MissionStep step : mission.steps()) {
            switch (step.type()) {
                case NAVIGATE -> {
                    Coordinate target = step.target().orElseThrow(() -> new IllegalArgumentException(
                            "NAVIGATE step '" + step.stepId() + "' requires a target coordinate"));
                    world.requireContains(target);
                }
                case WAIT -> {
                    if (step.waitTicks() <= 0) {
                        throw new IllegalArgumentException(
                                "WAIT step '" + step.stepId() + "' requires positive waitTicks");
                    }
                }
                case PICKUP, DROPOFF, DOCK, CHARGE -> {
                    // Reserved extension points — rejected at runtime by tick(), allowed in model.
                }
            }
        }
    }
}
