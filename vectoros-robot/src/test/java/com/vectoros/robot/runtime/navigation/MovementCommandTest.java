package com.vectoros.robot.runtime.navigation;

import com.vectoros.robot.runtime.world.Coordinate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MovementCommandTest {

    @Test
    void factoriesCreateImmutableCommands() {
        MovementCommand forward = MovementCommand.moveForward(new Coordinate(1, 0), Heading.EAST, 1.0);
        MovementCommand left = MovementCommand.turnLeft(Heading.NORTH);
        MovementCommand right = MovementCommand.turnRight(Heading.SOUTH);
        MovementCommand stop = MovementCommand.stop();

        assertThat(forward.movementType()).isEqualTo(MovementType.MOVE_FORWARD);
        assertThat(forward.targetCoordinate()).contains(new Coordinate(1, 0));
        assertThat(forward.targetHeading()).contains(Heading.EAST);
        assertThat(forward.speed()).isEqualTo(1.0);

        assertThat(left.movementType()).isEqualTo(MovementType.TURN_LEFT);
        assertThat(right.movementType()).isEqualTo(MovementType.TURN_RIGHT);
        assertThat(stop.movementType()).isEqualTo(MovementType.STOP);
        assertThat(stop.targetCoordinate()).isEmpty();
    }

    @Test
    void rejectsNegativeSpeed() {
        assertThatThrownBy(() -> new MovementCommand(MovementType.MOVE_FORWARD, new Coordinate(1, 0), Heading.EAST, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void movementTypeEnumContainsExpectedValues() {
        assertThat(MovementType.values()).containsExactly(
                MovementType.MOVE_FORWARD,
                MovementType.TURN_LEFT,
                MovementType.TURN_RIGHT,
                MovementType.STOP);
    }
}
