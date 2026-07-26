package com.vectoros.robot.runtime.world;

import java.util.Objects;

/**
 * Rectangular warehouse grid bounds. Cells are inclusive in
 * {@code [0, width)} × {@code [0, height)}.
 */
public final class WarehouseWorld {

    private final int width;
    private final int height;

    public WarehouseWorld(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be positive");
        }
        this.width = width;
        this.height = height;
    }

    public static WarehouseWorld square(int size) {
        return new WarehouseWorld(size, size);
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public boolean contains(Coordinate coordinate) {
        Objects.requireNonNull(coordinate, "coordinate");
        return coordinate.x() >= 0
                && coordinate.y() >= 0
                && coordinate.x() < width
                && coordinate.y() < height;
    }

    public void requireContains(Coordinate coordinate) {
        if (!contains(coordinate)) {
            throw new IllegalArgumentException(
                    "Coordinate " + coordinate + " is outside warehouse bounds "
                            + width + "x" + height);
        }
    }
}
