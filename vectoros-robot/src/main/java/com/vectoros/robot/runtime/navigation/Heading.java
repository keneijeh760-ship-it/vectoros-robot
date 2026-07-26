package com.vectoros.robot.runtime.navigation;

/**
 * Cardinal robot orientation on the warehouse grid.
 * Degrees match the existing HAL convention: 0° = +X (EAST), 90° = +Y (NORTH).
 */
public enum Heading {
    NORTH(0, 1, 90.0),
    EAST(1, 0, 0.0),
    SOUTH(0, -1, 270.0),
    WEST(-1, 0, 180.0);

    private final int deltaX;
    private final int deltaY;
    private final double degrees;

    Heading(int deltaX, int deltaY, double degrees) {
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.degrees = degrees;
    }

    public int deltaX() {
        return deltaX;
    }

    public int deltaY() {
        return deltaY;
    }

    public double degrees() {
        return degrees;
    }

    public Heading turnLeft() {
        return switch (this) {
            case NORTH -> WEST;
            case WEST -> SOUTH;
            case SOUTH -> EAST;
            case EAST -> NORTH;
        };
    }

    public Heading turnRight() {
        return switch (this) {
            case NORTH -> EAST;
            case EAST -> SOUTH;
            case SOUTH -> WEST;
            case WEST -> NORTH;
        };
    }

    public int stepsTurningLeftTo(Heading target) {
        Heading cursor = this;
        int steps = 0;
        while (cursor != target) {
            cursor = cursor.turnLeft();
            steps++;
        }
        return steps;
    }

    public int stepsTurningRightTo(Heading target) {
        Heading cursor = this;
        int steps = 0;
        while (cursor != target) {
            cursor = cursor.turnRight();
            steps++;
        }
        return steps;
    }

    /**
     * Snaps continuous degrees to the nearest cardinal heading.
     */
    public static Heading fromDegrees(double degrees) {
        if (!Double.isFinite(degrees)) {
            throw new IllegalArgumentException("degrees must be finite");
        }
        double normalized = degrees % 360.0;
        if (normalized < 0) {
            normalized += 360.0;
        }
        if (normalized >= 315.0 || normalized < 45.0) {
            return EAST;
        }
        if (normalized < 135.0) {
            return NORTH;
        }
        if (normalized < 225.0) {
            return WEST;
        }
        return SOUTH;
    }

    public static Heading toward(int fromX, int fromY, int toX, int toY) {
        int dx = Integer.compare(toX, fromX);
        int dy = Integer.compare(toY, fromY);
        if (dx == 0 && dy == 0) {
            throw new IllegalArgumentException("Cannot derive heading toward the same coordinate");
        }
        if (dx != 0 && dy != 0) {
            throw new IllegalArgumentException("Diagonal heading is not supported");
        }
        if (dx > 0) {
            return EAST;
        }
        if (dx < 0) {
            return WEST;
        }
        if (dy > 0) {
            return NORTH;
        }
        return SOUTH;
    }
}
