package com.vectoros.robot.runtime.task;

import com.vectoros.robot.runtime.model.Position;
import com.vectoros.robot.runtime.model.RobotState;
import com.vectoros.robot.runtime.model.RobotTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaskExecutorTest {

    private final TaskExecutor executor = new TaskExecutor(0.1);

    @Test
    void evaluateCompletionReturnsEmptyWhenNoTask() {
        RobotState state = RobotState.initial("robot-1");

        assertThat(executor.evaluateCompletion(state)).isEmpty();
    }

    @Test
    void evaluateCompletionReturnsEmptyWhenFarFromTarget() {
        RobotState state = RobotState.initial("robot-1");
        state.assignTask(new RobotTask("task-1", new Position(10, 0)));

        assertThat(executor.evaluateCompletion(state)).isEmpty();
    }

    @Test
    void evaluateCompletionReturnsTaskWhenWithinTolerance() {
        RobotState state = RobotState.initial("robot-1");
        RobotTask task = new RobotTask("task-1", new Position(0.05, 0));
        state.assignTask(task);
        state.updatePosition(new Position(0, 0));

        assertThat(executor.evaluateCompletion(state)).contains(task);
    }

    @Test
    void hasArrivedUsesConfiguredTolerance() {
        assertThat(executor.hasArrived(new Position(0, 0), new Position(0.1, 0))).isTrue();
        assertThat(executor.hasArrived(new Position(0, 0), new Position(0.11, 0))).isFalse();
    }
}
