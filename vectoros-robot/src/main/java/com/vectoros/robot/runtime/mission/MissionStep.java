package com.vectoros.robot.runtime.mission;

import com.vectoros.robot.runtime.world.Coordinate;

import java.util.Objects;
import java.util.Optional;

/**
 * Immutable unit of mission work.
 */
public final class MissionStep {

    private final String stepId;
    private final MissionStepType type;
    private final Coordinate target;
    private final int waitTicks;

    private MissionStep(String stepId, MissionStepType type, Coordinate target, int waitTicks) {
        if (stepId == null || stepId.isBlank()) {
            throw new IllegalArgumentException("stepId must not be blank");
        }
        this.stepId = stepId;
        this.type = Objects.requireNonNull(type, "type");
        this.target = target;
        if (waitTicks < 0) {
            throw new IllegalArgumentException("waitTicks must be non-negative");
        }
        this.waitTicks = waitTicks;
    }

    public static MissionStep navigate(String stepId, Coordinate target) {
        Objects.requireNonNull(target, "target");
        return new MissionStep(stepId, MissionStepType.NAVIGATE, target, 0);
    }

    public static MissionStep waitTicks(String stepId, int ticks) {
        if (ticks <= 0) {
            throw new IllegalArgumentException("WAIT ticks must be positive");
        }
        return new MissionStep(stepId, MissionStepType.WAIT, null, ticks);
    }

    public String stepId() {
        return stepId;
    }

    public MissionStepType type() {
        return type;
    }

    public Optional<Coordinate> target() {
        return Optional.ofNullable(target);
    }

    public int waitTicks() {
        return waitTicks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MissionStep that)) {
            return false;
        }
        return waitTicks == that.waitTicks
                && stepId.equals(that.stepId)
                && type == that.type
                && Objects.equals(target, that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stepId, type, target, waitTicks);
    }

    @Override
    public String toString() {
        return "MissionStep{stepId='" + stepId + "', type=" + type
                + ", target=" + target + ", waitTicks=" + waitTicks + '}';
    }
}
