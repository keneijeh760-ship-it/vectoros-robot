package com.vectoros.robot.runtime.world;

import com.vectoros.robot.runtime.model.Position;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WarehouseWorldTest {

    @Test
    void containsValidatesInclusiveOriginExclusiveMax() {
        WarehouseWorld world = new WarehouseWorld(5, 4);

        assertThat(world.contains(new Coordinate(0, 0))).isTrue();
        assertThat(world.contains(new Coordinate(4, 3))).isTrue();
        assertThat(world.contains(new Coordinate(5, 0))).isFalse();
        assertThat(world.contains(new Coordinate(0, 4))).isFalse();
        assertThat(world.contains(new Coordinate(-1, 0))).isFalse();
    }

    @Test
    void requireContainsThrowsForOutOfBounds() {
        WarehouseWorld world = WarehouseWorld.square(3);

        assertThatThrownBy(() -> world.requireContains(new Coordinate(3, 0)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void coordinateRoundTripsThroughPosition() {
        Coordinate coordinate = new Coordinate(4, 7);

        assertThat(Coordinate.fromPosition(coordinate.toPosition())).isEqualTo(coordinate);
        assertThat(Coordinate.fromPosition(new Position(1.4, 2.6))).isEqualTo(new Coordinate(1, 3));
    }
}
