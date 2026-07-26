package com.vectoros.robot.runtime.model;

import com.vectoros.robot.runtime.state.RobotStateEvent;
import com.vectoros.robot.runtime.state.RobotStateMachine;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RobotStateTest {

    @Test
    void initialStateIsOfflineAtOriginWithFullBattery() {
        RobotState state = RobotState.initial("robot-1");

        assertThat(state.robotId()).isEqualTo("robot-1");
        assertThat(state.status()).isEqualTo(RobotStatus.OFFLINE);
        assertThat(state.battery()).isEqualTo(BatteryState.full());
        assertThat(state.position()).isEqualTo(Position.origin());
        assertThat(state.currentTask()).isEmpty();
        assertThat(state.headingDegrees()).isZero();
        assertThat(state.heading()).isEqualTo(com.vectoros.robot.runtime.navigation.Heading.EAST);
        assertThat(state.speed()).isZero();
        assertThat(state.lastHeartbeat()).isEqualTo(Instant.EPOCH);
    }

    @Test
    void mutatorsUpdateSingleSourceOfTruth() {
        RobotState state = RobotState.initial("robot-1");
        RobotTask task = new RobotTask("task-1", new Position(5, 5));
        RobotStateMachine stateMachine = new RobotStateMachine(state);

        stateMachine.transition(RobotStateEvent.POWER_ON);
        stateMachine.transition(RobotStateEvent.INITIALIZATION_COMPLETE);
        stateMachine.transition(RobotStateEvent.TASK_RECEIVED);

        state.updateBattery(new BatteryState(80));
        state.updatePosition(new Position(1, 2));
        state.assignTask(task);
        state.updateHeading(90);
        state.updateSpeed(1.5);
        Instant heartbeat = Instant.parse("2026-07-22T12:00:00Z");
        state.updateHeartbeat(heartbeat);

        assertThat(state.status()).isEqualTo(RobotStatus.TASK_ASSIGNED);
        assertThat(state.battery().percentage()).isEqualTo(80);
        assertThat(state.position()).isEqualTo(new Position(1, 2));
        assertThat(state.currentTask()).contains(task);
        assertThat(state.headingDegrees()).isEqualTo(90);
        assertThat(state.heading()).isEqualTo(com.vectoros.robot.runtime.navigation.Heading.NORTH);
        assertThat(state.speed()).isEqualTo(1.5);
        assertThat(state.lastHeartbeat()).isEqualTo(heartbeat);
        assertThat(state.isMoving()).isTrue();
    }

    @Test
    void clearTaskRemovesAssignment() {
        RobotState state = RobotState.initial("robot-1");
        state.assignTask(new RobotTask("task-1", new Position(1, 1)));

        state.clearTask();

        assertThat(state.currentTask()).isEmpty();
    }

    @Test
    void rejectsBlankRobotId() {
        assertThatThrownBy(() -> RobotState.initial(" "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void positionDistanceAndHeadingAreConsistent() {
        Position a = new Position(0, 0);
        Position b = new Position(3, 4);

        assertThat(a.distanceTo(b)).isEqualTo(5.0);
        assertThat(a.headingDegreesToward(b)).isCloseTo(53.13, org.assertj.core.data.Offset.offset(0.01));
    }

    @Test
    void batteryStateEnforcesBoundsAndLowThreshold() {
        BatteryState low = new BatteryState(20);
        assertThat(low.isLow()).isTrue();
        assertThat(new BatteryState(21).isLow()).isFalse();
        assertThat(BatteryState.full().drain(150).percentage()).isZero();
        assertThatThrownBy(() -> new BatteryState(101))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
