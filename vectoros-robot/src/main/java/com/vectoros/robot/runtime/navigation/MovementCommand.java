package com.vectoros.robot.runtime.navigation;

import com.vectoros.robot.runtime.world.Coordinate;

import java.util.Objects;
import java.util.Optional;

/**
 * Immutable navigation intent for one control step.
 * Contains no hardware logic. Optional fields support future extensions
 * (acceleration, tolerance, timeout, reverse, docking) without breaking callers.
 */
public final class MovementCommand {

    private final MovementType movementType;
    private final Coordinate targetCoordinate;
    private final Heading targetHeading;
    private final double speed;

    public MovementCommand(
            MovementType movementType,
            Coordinate targetCoordinate,
            Heading targetHeading,
            double speed) {
        this.movementType = Objects.requireNonNull(movementType, "movementType");
        this.targetCoordinate = targetCoordinate;
        this.targetHeading = targetHeading;
        if (!Double.isFinite(speed) || speed < 0) {
            throw new IllegalArgumentException("speed must be a non-negative finite number");
        }
        this.speed = speed;
    }

    public static MovementCommand moveForward(Coordinate nextCell, Heading heading, double speed) {
        return new MovementCommand(
                MovementType.MOVE_FORWARD,
                Objects.requireNonNull(nextCell, "nextCell"),
                Objects.requireNonNull(heading, "heading"),
                speed);
    }

    public static MovementCommand turnLeft(Heading targetHeading) {
        return new MovementCommand(
                MovementType.TURN_LEFT,
                null,
                Objects.requireNonNull(targetHeading, "targetHeading"),
                0.0);
    }

    public static MovementCommand turnRight(Heading targetHeading) {
        return new MovementCommand(
                MovementType.TURN_RIGHT,
                null,
                Objects.requireNonNull(targetHeading, "targetHeading"),
                0.0);
    }

    public static MovementCommand stop() {
        return new MovementCommand(MovementType.STOP, null, null, 0.0);
    }

    public MovementType movementType() {
        return movementType;
    }

    public Optional<Coordinate> targetCoordinate() {
        return Optional.ofNullable(targetCoordinate);
    }

    public Optional<Heading> targetHeading() {
        return Optional.ofNullable(targetHeading);
    }

    public double speed() {
        return speed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MovementCommand that)) {
            return false;
        }
        return Double.compare(that.speed, speed) == 0
                && movementType == that.movementType
                && Objects.equals(targetCoordinate, that.targetCoordinate)
                && targetHeading == that.targetHeading;
    }

    @Override
    public int hashCode() {
        return Objects.hash(movementType, targetCoordinate, targetHeading, speed);
    }

    @Override
    public String toString() {
        return "MovementCommand{"
                + "movementType=" + movementType
                + ", targetCoordinate=" + targetCoordinate
                + ", targetHeading=" + targetHeading
                + ", speed=" + speed
                + '}';
    }
}
