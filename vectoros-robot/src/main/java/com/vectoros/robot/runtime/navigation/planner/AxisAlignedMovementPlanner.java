package com.vectoros.robot.runtime.navigation.planner;

import com.vectoros.robot.runtime.navigation.Heading;
import com.vectoros.robot.runtime.navigation.MovementCommand;
import com.vectoros.robot.runtime.world.Coordinate;

import java.util.Objects;

/**
 * Deterministic axis-aligned planner: align on X first, then Y.
 * No shortest-path search and no obstacle avoidance.
 */
public final class AxisAlignedMovementPlanner implements MovementPlanner {

    @Override
    public MovementCommand nextCommand(
            Coordinate current,
            Heading currentHeading,
            Coordinate destination,
            double defaultMoveSpeed) {
        Objects.requireNonNull(current, "current");
        Objects.requireNonNull(currentHeading, "currentHeading");
        Objects.requireNonNull(destination, "destination");
        if (defaultMoveSpeed <= 0 || !Double.isFinite(defaultMoveSpeed)) {
            throw new IllegalArgumentException("defaultMoveSpeed must be a positive finite number");
        }

        if (current.equals(destination)) {
            return MovementCommand.stop();
        }

        if (current.x() != destination.x()) {
            Heading required = current.x() < destination.x() ? Heading.EAST : Heading.WEST;
            return commandTowardHeading(current, currentHeading, required, defaultMoveSpeed);
        }

        Heading required = current.y() < destination.y() ? Heading.NORTH : Heading.SOUTH;
        return commandTowardHeading(current, currentHeading, required, defaultMoveSpeed);
    }

    private static MovementCommand commandTowardHeading(
            Coordinate current,
            Heading currentHeading,
            Heading required,
            double speed) {
        if (currentHeading == required) {
            Coordinate next = current.translate(required.deltaX(), required.deltaY());
            return MovementCommand.moveForward(next, required, speed);
        }
        if (currentHeading.stepsTurningLeftTo(required) <= currentHeading.stepsTurningRightTo(required)) {
            return MovementCommand.turnLeft(currentHeading.turnLeft());
        }
        return MovementCommand.turnRight(currentHeading.turnRight());
    }
}
