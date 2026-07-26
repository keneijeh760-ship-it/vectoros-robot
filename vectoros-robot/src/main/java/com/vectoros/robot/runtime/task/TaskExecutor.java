package com.vectoros.robot.runtime.task;

import com.vectoros.robot.runtime.model.Position;
import com.vectoros.robot.runtime.model.RobotState;
import com.vectoros.robot.runtime.model.RobotTask;

import java.util.Objects;
import java.util.Optional;

/**
 * Manages execution progress of the assigned task against {@link RobotState}.
 */
public final class TaskExecutor {

    private final double arrivalTolerance;

    public TaskExecutor(double arrivalTolerance) {
        if (arrivalTolerance < 0 || !Double.isFinite(arrivalTolerance)) {
            throw new IllegalArgumentException("arrivalTolerance must be a non-negative finite number");
        }
        this.arrivalTolerance = arrivalTolerance;
    }

    public TaskExecutor() {
        this(0.05);
    }

    /**
     * @return the completed task if the robot has arrived at the target; otherwise empty
     */
    public Optional<RobotTask> evaluateCompletion(RobotState state) {
        Objects.requireNonNull(state, "state");
        return state.currentTask().filter(task -> hasArrived(state.position(), task.target()));
    }

    public boolean hasArrived(Position current, Position target) {
        Objects.requireNonNull(current, "current");
        Objects.requireNonNull(target, "target");
        return current.isWithin(target, arrivalTolerance);
    }

    public double arrivalTolerance() {
        return arrivalTolerance;
    }
}
