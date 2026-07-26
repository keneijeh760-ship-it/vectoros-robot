package com.vectoros.robot.runtime.model;

import java.util.Objects;

/**
 * Immutable 2D warehouse coordinate.
 */
public final class Position {

    private final double x;
    private final double y;

    public Position(double x, double y) {
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException("Position coordinates must be finite");
        }
        this.x = x;
        this.y = y;
    }

    public static Position origin() {
        return new Position(0.0, 0.0);
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double distanceTo(Position other) {
        Objects.requireNonNull(other, "other");
        double dx = other.x - x;
        double dy = other.y - y;
        return Math.hypot(dx, dy);
    }

    public Position translate(double dx, double dy) {
        return new Position(x + dx, y + dy);
    }

    /**
     * Heading in degrees from this position toward {@code target}, where 0° is +X and 90° is +Y.
     */
    public double headingDegreesToward(Position target) {
        Objects.requireNonNull(target, "target");
        double dx = target.x - x;
        double dy = target.y - y;
        double degrees = Math.toDegrees(Math.atan2(dy, dx));
        return normalizeDegrees(degrees);
    }

    public boolean isWithin(Position other, double tolerance) {
        Objects.requireNonNull(other, "other");
        if (tolerance < 0) {
            throw new IllegalArgumentException("tolerance must be non-negative");
        }
        return distanceTo(other) <= tolerance;
    }

    private static double normalizeDegrees(double degrees) {
        double normalized = degrees % 360.0;
        if (normalized < 0) {
            normalized += 360.0;
        }
        return normalized;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Position position)) {
            return false;
        }
        return Double.compare(position.x, x) == 0 && Double.compare(position.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Position{x=" + x + ", y=" + y + '}';
    }
}
