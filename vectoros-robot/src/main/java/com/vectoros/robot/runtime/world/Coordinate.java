package com.vectoros.robot.runtime.world;

import com.vectoros.robot.runtime.model.Position;

import java.util.Objects;

/**
 * Discrete warehouse grid coordinate.
 * Continuous {@link Position} maps to the nearest cell for navigation.
 */
public final class Coordinate {

    private final int x;
    private final int y;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static Coordinate origin() {
        return new Coordinate(0, 0);
    }

    public static Coordinate fromPosition(Position position) {
        Objects.requireNonNull(position, "position");
        return new Coordinate((int) Math.round(position.x()), (int) Math.round(position.y()));
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public Position toPosition() {
        return new Position(x, y);
    }

    public Coordinate translate(int dx, int dy) {
        return new Coordinate(x + dx, y + dy);
    }

    public boolean equalsCoordinate(Coordinate other) {
        return other != null && x == other.x && y == other.y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Coordinate coordinate)) {
            return false;
        }
        return x == coordinate.x && y == coordinate.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Coordinate{x=" + x + ", y=" + y + '}';
    }
}
