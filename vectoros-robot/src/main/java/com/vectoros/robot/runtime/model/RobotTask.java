package com.vectoros.robot.runtime.model;

import java.util.Objects;

/**
 * Assigned work unit for the robot runtime.
 * Sprint 01 models a navigate-to-target task only.
 */
public final class RobotTask {

    private final String taskId;
    private final Position target;

    public RobotTask(String taskId, Position target) {
        if (taskId == null || taskId.isBlank()) {
            throw new IllegalArgumentException("taskId must not be blank");
        }
        this.taskId = taskId;
        this.target = Objects.requireNonNull(target, "target");
    }

    public String taskId() {
        return taskId;
    }

    public Position target() {
        return target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RobotTask robotTask)) {
            return false;
        }
        return taskId.equals(robotTask.taskId) && target.equals(robotTask.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, target);
    }

    @Override
    public String toString() {
        return "RobotTask{taskId='" + taskId + "', target=" + target + '}';
    }
}
