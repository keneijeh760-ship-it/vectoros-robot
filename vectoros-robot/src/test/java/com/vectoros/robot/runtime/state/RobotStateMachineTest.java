package com.vectoros.robot.runtime.state;

import com.vectoros.robot.runtime.model.RobotState;
import com.vectoros.robot.runtime.model.RobotStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RobotStateMachineTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-07-22T21:00:00Z");

    private RobotState state;
    private RobotStateMachine machine;

    @BeforeEach
    void setUp() {
        state = RobotState.initial("robot-sm");
        machine = new RobotStateMachine(state, Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC));
    }

    @Test
    void powerOnAndInitializeReachIdle() {
        RobotStateTransition powerOn = machine.transition(RobotStateEvent.POWER_ON);
        RobotStateTransition init = machine.transition(RobotStateEvent.INITIALIZATION_COMPLETE);

        assertThat(powerOn.from()).isEqualTo(RobotStatus.OFFLINE);
        assertThat(powerOn.to()).isEqualTo(RobotStatus.INITIALIZING);
        assertThat(powerOn.event()).isEqualTo(RobotStateEvent.POWER_ON);
        assertThat(powerOn.occurredAt()).isEqualTo(FIXED_INSTANT);

        assertThat(init.from()).isEqualTo(RobotStatus.INITIALIZING);
        assertThat(init.to()).isEqualTo(RobotStatus.IDLE);
        assertThat(machine.currentStatus()).isEqualTo(RobotStatus.IDLE);
        assertThat(state.status()).isEqualTo(RobotStatus.IDLE);
    }

    @ParameterizedTest(name = "{0} -[{1}]-> {2}")
    @CsvSource({
            "IDLE, TASK_RECEIVED, TASK_ASSIGNED",
            "TASK_ASSIGNED, START_MOVE_TO_PICKUP, MOVING_TO_PICKUP",
            "MOVING_TO_PICKUP, ARRIVED_AT_PICKUP, LOADING",
            "LOADING, LOADING_COMPLETE, MOVING_TO_DROPOFF",
            "MOVING_TO_DROPOFF, ARRIVED_AT_DROPOFF, UNLOADING",
            "UNLOADING, UNLOADING_COMPLETE, RETURNING",
            "RETURNING, RETURN_COMPLETE, IDLE",
            "IDLE, START_CHARGING, CHARGING",
            "CHARGING, CHARGING_COMPLETE, IDLE",
            "IDLE, GO_OFFLINE, OFFLINE",
            "ERROR, ERROR_CLEARED, IDLE"
    })
    void validTransitions(RobotStatus from, RobotStateEvent event, RobotStatus to) {
        forceStatus(from);

        RobotStateTransition transition = machine.transition(event);

        assertThat(transition.from()).isEqualTo(from);
        assertThat(transition.to()).isEqualTo(to);
        assertThat(machine.currentStatus()).isEqualTo(to);
    }

    @ParameterizedTest(name = "reject {0} + {1}")
    @CsvSource({
            "IDLE, POWER_ON",
            "IDLE, ARRIVED_AT_PICKUP",
            "TASK_ASSIGNED, TASK_RECEIVED",
            "MOVING_TO_PICKUP, RETURN_COMPLETE",
            "CHARGING, GO_OFFLINE",
            "OFFLINE, GO_OFFLINE",
            "OFFLINE, TASK_RECEIVED",
            "ERROR, TASK_RECEIVED",
            "INITIALIZING, GO_OFFLINE",
            "RETURNING, START_CHARGING"
    })
    void invalidTransitionsThrow(RobotStatus from, RobotStateEvent event) {
        forceStatus(from);

        assertThatThrownBy(() -> machine.transition(event))
                .isInstanceOf(InvalidRobotStateTransitionException.class)
                .hasMessageContaining(from.name())
                .hasMessageContaining(event.name());

        assertThat(machine.currentStatus()).isEqualTo(from);
    }

    @ParameterizedTest
    @EnumSource(
            value = RobotStatus.class,
            names = {
                    "INITIALIZING",
                    "IDLE",
                    "TASK_ASSIGNED",
                    "MOVING_TO_PICKUP",
                    "LOADING",
                    "MOVING_TO_DROPOFF",
                    "UNLOADING",
                    "RETURNING",
                    "CHARGING",
                    "OFFLINE"
            })
    void faultDetectedFromAnyNonErrorState(RobotStatus from) {
        forceStatus(from);

        RobotStateTransition transition = machine.transition(RobotStateEvent.FAULT_DETECTED);

        assertThat(transition.to()).isEqualTo(RobotStatus.ERROR);
        assertThat(machine.currentStatus()).isEqualTo(RobotStatus.ERROR);
    }

    @Test
    void faultDetectedFromErrorIsInvalid() {
        forceStatus(RobotStatus.ERROR);

        assertThatThrownBy(() -> machine.transition(RobotStateEvent.FAULT_DETECTED))
                .isInstanceOf(InvalidRobotStateTransitionException.class);
    }

    @Test
    void errorClearedRecoversToIdle() {
        forceStatus(RobotStatus.ERROR);

        machine.transition(RobotStateEvent.ERROR_CLEARED);

        assertThat(machine.currentStatus()).isEqualTo(RobotStatus.IDLE);
    }

    @Test
    void offlineTransitionsRoundTrip() {
        forceStatus(RobotStatus.IDLE);
        machine.transition(RobotStateEvent.GO_OFFLINE);
        assertThat(machine.currentStatus()).isEqualTo(RobotStatus.OFFLINE);

        machine.transition(RobotStateEvent.POWER_ON);
        assertThat(machine.currentStatus()).isEqualTo(RobotStatus.INITIALIZING);

        machine.transition(RobotStateEvent.INITIALIZATION_COMPLETE);
        assertThat(machine.currentStatus()).isEqualTo(RobotStatus.IDLE);
    }

    @Test
    void transitionToOfflineFromMissionUsesErrorRecoveryPath() {
        forceStatus(RobotStatus.MOVING_TO_PICKUP);

        machine.transitionToOffline();

        assertThat(machine.currentStatus()).isEqualTo(RobotStatus.OFFLINE);
    }

    @Test
    void transitionToOfflineFromIdleIsDirect() {
        forceStatus(RobotStatus.IDLE);

        machine.transitionToOffline();

        assertThat(machine.currentStatus()).isEqualTo(RobotStatus.OFFLINE);
    }

    @Test
    void transitionToOfflineWhenAlreadyOfflineIsNoOp() {
        assertThat(machine.currentStatus()).isEqualTo(RobotStatus.OFFLINE);

        machine.transitionToOffline();

        assertThat(machine.currentStatus()).isEqualTo(RobotStatus.OFFLINE);
    }

    @Test
    void advanceMissionToIdleWalksHappyPath() {
        forceStatus(RobotStatus.TASK_ASSIGNED);

        machine.advanceMissionToIdle();

        assertThat(machine.currentStatus()).isEqualTo(RobotStatus.IDLE);
    }

    @Test
    void advanceMissionToIdleFromMovingPickup() {
        forceStatus(RobotStatus.MOVING_TO_PICKUP);

        machine.advanceMissionToIdle();

        assertThat(machine.currentStatus()).isEqualTo(RobotStatus.IDLE);
    }

    @Test
    void advanceMissionToIdleFromIdleIsNoOp() {
        forceStatus(RobotStatus.IDLE);

        machine.advanceMissionToIdle();

        assertThat(machine.currentStatus()).isEqualTo(RobotStatus.IDLE);
    }

    @Test
    void advanceMissionToIdleFromChargingFails() {
        forceStatus(RobotStatus.CHARGING);

        assertThatThrownBy(() -> machine.advanceMissionToIdle())
                .isInstanceOf(InvalidRobotStateTransitionException.class);
    }

    @Test
    void canTransitionAndTryTransition() {
        forceStatus(RobotStatus.IDLE);

        assertThat(machine.canTransition(RobotStateEvent.TASK_RECEIVED)).isTrue();
        assertThat(machine.canTransition(RobotStateEvent.POWER_ON)).isFalse();
        assertThat(machine.tryTransition(RobotStateEvent.POWER_ON)).isEmpty();
        assertThat(machine.tryTransition(RobotStateEvent.TASK_RECEIVED))
                .isPresent()
                .get()
                .extracting(RobotStateTransition::to)
                .isEqualTo(RobotStatus.TASK_ASSIGNED);
    }

    @Test
    void chargingCycle() {
        forceStatus(RobotStatus.IDLE);
        machine.transition(RobotStateEvent.START_CHARGING);
        assertThat(machine.currentStatus()).isEqualTo(RobotStatus.CHARGING);

        machine.transition(RobotStateEvent.CHARGING_COMPLETE);
        assertThat(machine.currentStatus()).isEqualTo(RobotStatus.IDLE);
    }

    @Test
    void fullMissionHappyPath() {
        machine.transition(RobotStateEvent.POWER_ON);
        machine.transition(RobotStateEvent.INITIALIZATION_COMPLETE);
        machine.transition(RobotStateEvent.TASK_RECEIVED);
        machine.transition(RobotStateEvent.START_MOVE_TO_PICKUP);
        machine.transition(RobotStateEvent.ARRIVED_AT_PICKUP);
        machine.transition(RobotStateEvent.LOADING_COMPLETE);
        machine.transition(RobotStateEvent.ARRIVED_AT_DROPOFF);
        machine.transition(RobotStateEvent.UNLOADING_COMPLETE);
        machine.transition(RobotStateEvent.RETURN_COMPLETE);

        assertThat(machine.currentStatus()).isEqualTo(RobotStatus.IDLE);
    }

    /**
     * Test helper: legally walk/force into a status using FAULT + recovery where needed,
     * then applyStatus only for unreachable intermediates via controlled machine path.
     */
    private void forceStatus(RobotStatus target) {
        if (state.status() == target) {
            return;
        }

        // Reset to IDLE via legal path when possible.
        if (state.status() == RobotStatus.OFFLINE) {
            machine.transition(RobotStateEvent.POWER_ON);
            machine.transition(RobotStateEvent.INITIALIZATION_COMPLETE);
        } else if (state.status() == RobotStatus.INITIALIZING) {
            machine.transition(RobotStateEvent.INITIALIZATION_COMPLETE);
        } else if (state.status() == RobotStatus.ERROR) {
            machine.transition(RobotStateEvent.ERROR_CLEARED);
        } else if (state.status() == RobotStatus.CHARGING) {
            machine.transition(RobotStateEvent.CHARGING_COMPLETE);
        } else if (state.status() != RobotStatus.IDLE) {
            machine.transition(RobotStateEvent.FAULT_DETECTED);
            machine.transition(RobotStateEvent.ERROR_CLEARED);
        }

        if (target == RobotStatus.IDLE) {
            return;
        }
        if (target == RobotStatus.OFFLINE) {
            machine.transition(RobotStateEvent.GO_OFFLINE);
            return;
        }
        if (target == RobotStatus.INITIALIZING) {
            machine.transition(RobotStateEvent.GO_OFFLINE);
            machine.transition(RobotStateEvent.POWER_ON);
            return;
        }
        if (target == RobotStatus.ERROR) {
            machine.transition(RobotStateEvent.FAULT_DETECTED);
            return;
        }
        if (target == RobotStatus.CHARGING) {
            machine.transition(RobotStateEvent.START_CHARGING);
            return;
        }

        // Mission chain from IDLE
        machine.transition(RobotStateEvent.TASK_RECEIVED);
        if (target == RobotStatus.TASK_ASSIGNED) {
            return;
        }
        machine.transition(RobotStateEvent.START_MOVE_TO_PICKUP);
        if (target == RobotStatus.MOVING_TO_PICKUP) {
            return;
        }
        machine.transition(RobotStateEvent.ARRIVED_AT_PICKUP);
        if (target == RobotStatus.LOADING) {
            return;
        }
        machine.transition(RobotStateEvent.LOADING_COMPLETE);
        if (target == RobotStatus.MOVING_TO_DROPOFF) {
            return;
        }
        machine.transition(RobotStateEvent.ARRIVED_AT_DROPOFF);
        if (target == RobotStatus.UNLOADING) {
            return;
        }
        machine.transition(RobotStateEvent.UNLOADING_COMPLETE);
        if (target == RobotStatus.RETURNING) {
            return;
        }

        throw new IllegalArgumentException("Unsupported forceStatus target: " + target);
    }
}
