package com.vectoros.robot.runtime.navigation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeadingTest {

    @Test
    void turnLeftAndRightCycleCardinals() {
        assertThat(Heading.NORTH.turnLeft()).isEqualTo(Heading.WEST);
        assertThat(Heading.WEST.turnLeft()).isEqualTo(Heading.SOUTH);
        assertThat(Heading.NORTH.turnRight()).isEqualTo(Heading.EAST);
        assertThat(Heading.EAST.turnRight()).isEqualTo(Heading.SOUTH);
    }

    @Test
    void fromDegreesSnapsToNearestCardinal() {
        assertThat(Heading.fromDegrees(0)).isEqualTo(Heading.EAST);
        assertThat(Heading.fromDegrees(90)).isEqualTo(Heading.NORTH);
        assertThat(Heading.fromDegrees(180)).isEqualTo(Heading.WEST);
        assertThat(Heading.fromDegrees(270)).isEqualTo(Heading.SOUTH);
        assertThat(Heading.fromDegrees(44)).isEqualTo(Heading.EAST);
        assertThat(Heading.fromDegrees(46)).isEqualTo(Heading.NORTH);
    }

    @Test
    void towardComputesAxisAlignedHeading() {
        assertThat(Heading.toward(0, 0, 2, 0)).isEqualTo(Heading.EAST);
        assertThat(Heading.toward(2, 0, 0, 0)).isEqualTo(Heading.WEST);
        assertThat(Heading.toward(0, 0, 0, 3)).isEqualTo(Heading.NORTH);
        assertThat(Heading.toward(0, 3, 0, 0)).isEqualTo(Heading.SOUTH);
    }

    @Test
    void towardRejectsDiagonalAndZero() {
        assertThatThrownBy(() -> Heading.toward(0, 0, 1, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Heading.toward(1, 1, 1, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deltaMatchesCardinalStep() {
        assertThat(Heading.EAST.deltaX()).isEqualTo(1);
        assertThat(Heading.NORTH.deltaY()).isEqualTo(1);
        assertThat(Heading.WEST.deltaX()).isEqualTo(-1);
        assertThat(Heading.SOUTH.deltaY()).isEqualTo(-1);
    }
}
