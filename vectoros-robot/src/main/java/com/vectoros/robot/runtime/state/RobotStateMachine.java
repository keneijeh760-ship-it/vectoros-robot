package com.vectoros.robot.runtime.state;

import com.vectoros.robot.runtime.model.RobotState;
import com.vectoros.robot.runtime.model.RobotStatus;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Sole authority for changing {@link RobotStatus}.
 * Validates every transition against the declared table before mutating {@link RobotState}.
 */
public final class RobotStateMachine {

    private static final Logger LOGGER = Logger.getLogger(RobotStateMachine.class.getName());

    private static final Map<RobotStatus, Map<RobotStateEvent, RobotStatus>> TRANSITIONS =
            buildTransitionTable();

    private final RobotState state;
    private final Clock clock;

    public RobotStateMachine(RobotState state, Clock clock) {
        this.state = Objects.requireNonNull(state, "state");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public RobotStateMachine(RobotState state) {
        this(state, Clock.systemUTC());
    }

    public RobotStatus currentStatus() {
        return state.status();
    }

    public RobotState state() {
        return state;
    }

    /**
     * @return true if {@code event} is legal from the current status
     */
    public boolean canTransition(RobotStateEvent event) {
        Objects.requireNonNull(event, "event");
        return resolveTarget(state.status(), event).isPresent();
    }

    /**
     * Validates and applies a transition for {@code event}.
     *
     * @throws InvalidRobotStateTransitionException if the transition is not allowed
     */
    public RobotStateTransition transition(RobotStateEvent event) {
        Objects.requireNonNull(event, "event");
        RobotStatus from = state.status();
        RobotStatus to = resolveTarget(from, event)
                .orElseThrow(() -> new InvalidRobotStateTransitionException(from, event));

        Instant occurredAt = clock.instant();
        state.applyStatus(to);

        RobotStateTransition transition = new RobotStateTransition(from, to, event, occurredAt);
        LOGGER.info(() -> "Robot '"
                + state.robotId()
                + "' status transition: "
                + from
                + " -["
                + event
                + "]-> "
                + to);
        return transition;
    }

    /**
     * Applies {@code event} only when it is legal; otherwise returns empty.
     */
    public Optional<RobotStateTransition> tryTransition(RobotStateEvent event) {
        if (!canTransition(event)) {
            return Optional.empty();
        }
        return Optional.of(transition(event));
    }

    /**
     * Advances from the current mission status through the happy path until {@link RobotStatus#IDLE}.
     * Used by the runtime when a Sprint 01 navigate-to-target task finishes before full warehouse
     * workflow components exist.
     */
    public void advanceMissionToIdle() {
        while (currentStatus() != RobotStatus.IDLE) {
            RobotStateEvent next = switch (currentStatus()) {
                case TASK_ASSIGNED -> RobotStateEvent.START_MOVE_TO_PICKUP;
                case MOVING_TO_PICKUP -> RobotStateEvent.ARRIVED_AT_PICKUP;
                case LOADING -> RobotStateEvent.LOADING_COMPLETE;
                case MOVING_TO_DROPOFF -> RobotStateEvent.ARRIVED_AT_DROPOFF;
                case UNLOADING -> RobotStateEvent.UNLOADING_COMPLETE;
                case RETURNING -> RobotStateEvent.RETURN_COMPLETE;
                default -> throw new InvalidRobotStateTransitionException(
                        currentStatus(), RobotStateEvent.RETURN_COMPLETE);
            };
            transition(next);
        }
    }

    /**
     * Brings the robot to {@link RobotStatus#OFFLINE} using only legal transitions.
     */
    public void transitionToOffline() {
        if (currentStatus() == RobotStatus.OFFLINE) {
            return;
        }
        if (currentStatus() == RobotStatus.INITIALIZING) {
            transition(RobotStateEvent.INITIALIZATION_COMPLETE);
        } else if (currentStatus() == RobotStatus.CHARGING) {
            transition(RobotStateEvent.CHARGING_COMPLETE);
        } else if (currentStatus() == RobotStatus.ERROR) {
            transition(RobotStateEvent.ERROR_CLEARED);
        } else if (currentStatus() != RobotStatus.IDLE) {
            transition(RobotStateEvent.FAULT_DETECTED);
            transition(RobotStateEvent.ERROR_CLEARED);
        }
        transition(RobotStateEvent.GO_OFFLINE);
    }

    static Optional<RobotStatus> resolveTarget(RobotStatus from, RobotStateEvent event) {
        if (event == RobotStateEvent.FAULT_DETECTED) {
            if (from == RobotStatus.ERROR) {
                return Optional.empty();
            }
            return Optional.of(RobotStatus.ERROR);
        }
        Map<RobotStateEvent, RobotStatus> fromMap = TRANSITIONS.get(from);
        if (fromMap == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(fromMap.get(event));
    }

    static Map<RobotStatus, Map<RobotStateEvent, RobotStatus>> transitionTable() {
        return TRANSITIONS;
    }

    private static Map<RobotStatus, Map<RobotStateEvent, RobotStatus>> buildTransitionTable() {
        Map<RobotStatus, Map<RobotStateEvent, RobotStatus>> table = new EnumMap<>(RobotStatus.class);

        put(table, RobotStatus.OFFLINE, RobotStateEvent.POWER_ON, RobotStatus.INITIALIZING);
        put(table, RobotStatus.INITIALIZING, RobotStateEvent.INITIALIZATION_COMPLETE, RobotStatus.IDLE);

        put(table, RobotStatus.IDLE, RobotStateEvent.TASK_RECEIVED, RobotStatus.TASK_ASSIGNED);
        put(table, RobotStatus.IDLE, RobotStateEvent.START_CHARGING, RobotStatus.CHARGING);
        put(table, RobotStatus.IDLE, RobotStateEvent.GO_OFFLINE, RobotStatus.OFFLINE);

        put(table, RobotStatus.TASK_ASSIGNED, RobotStateEvent.START_MOVE_TO_PICKUP, RobotStatus.MOVING_TO_PICKUP);
        put(table, RobotStatus.MOVING_TO_PICKUP, RobotStateEvent.ARRIVED_AT_PICKUP, RobotStatus.LOADING);
        put(table, RobotStatus.LOADING, RobotStateEvent.LOADING_COMPLETE, RobotStatus.MOVING_TO_DROPOFF);
        put(table, RobotStatus.MOVING_TO_DROPOFF, RobotStateEvent.ARRIVED_AT_DROPOFF, RobotStatus.UNLOADING);
        put(table, RobotStatus.UNLOADING, RobotStateEvent.UNLOADING_COMPLETE, RobotStatus.RETURNING);
        put(table, RobotStatus.RETURNING, RobotStateEvent.RETURN_COMPLETE, RobotStatus.IDLE);

        put(table, RobotStatus.CHARGING, RobotStateEvent.CHARGING_COMPLETE, RobotStatus.IDLE);
        put(table, RobotStatus.ERROR, RobotStateEvent.ERROR_CLEARED, RobotStatus.IDLE);

        Map<RobotStatus, Map<RobotStateEvent, RobotStatus>> immutable = new EnumMap<>(RobotStatus.class);
        for (Map.Entry<RobotStatus, Map<RobotStateEvent, RobotStatus>> entry : table.entrySet()) {
            immutable.put(entry.getKey(), Collections.unmodifiableMap(entry.getValue()));
        }
        return Collections.unmodifiableMap(immutable);
    }

    private static void put(
            Map<RobotStatus, Map<RobotStateEvent, RobotStatus>> table,
            RobotStatus from,
            RobotStateEvent event,
            RobotStatus to) {
        table.computeIfAbsent(from, ignored -> new EnumMap<>(RobotStateEvent.class)).put(event, to);
    }
}
