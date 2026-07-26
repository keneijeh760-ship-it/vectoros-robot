package com.vectoros.robot.runtime.navigation.planner;

import com.vectoros.robot.runtime.navigation.Heading;
import com.vectoros.robot.runtime.navigation.MovementCommand;
import com.vectoros.robot.runtime.navigation.MovementType;
import com.vectoros.robot.runtime.world.Coordinate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AxisAlignedMovementPlannerTest {

    private MovementPlanner planner;

    @BeforeEach
    void setUp() {
        planner = new AxisAlignedMovementPlanner();
    }

    @Test
    void returnsStopWhenAlreadyAtDestination() {
        MovementCommand command = planner.nextCommand(
                new Coordinate(2, 2), Heading.EAST, new Coordinate(2, 2), 1.0);

        assertThat(command.movementType()).isEqualTo(MovementType.STOP);
    }

    @Test
    void movesAlongXBeforeY() {
        MovementCommand command = planner.nextCommand(
                new Coordinate(0, 0), Heading.EAST, new Coordinate(2, 3), 1.0);

        assertThat(command.movementType()).isEqualTo(MovementType.MOVE_FORWARD);
        assertThat(command.targetCoordinate()).contains(new Coordinate(1, 0));
        assertThat(command.targetHeading()).contains(Heading.EAST);
    }

    @Test
    void turnsBeforeMovingWhenFacingWrongWayOnX() {
        MovementCommand command = planner.nextCommand(
                new Coordinate(0, 0), Heading.NORTH, new Coordinate(2, 0), 1.0);

        assertThat(command.movementType()).isEqualTo(MovementType.TURN_RIGHT);
        assertThat(command.targetHeading()).contains(Heading.EAST);
    }

    @Test
    void afterXAlignedMovesAlongY() {
        MovementCommand command = planner.nextCommand(
                new Coordinate(2, 0), Heading.NORTH, new Coordinate(2, 3), 1.0);

        assertThat(command.movementType()).isEqualTo(MovementType.MOVE_FORWARD);
        assertThat(command.targetCoordinate()).contains(new Coordinate(2, 1));
    }

    @Test
    void choosesShorterTurnDirection() {
        MovementCommand fromWestToNorth = planner.nextCommand(
                new Coordinate(0, 0), Heading.WEST, new Coordinate(0, 2), 1.0);

        assertThat(fromWestToNorth.movementType()).isEqualTo(MovementType.TURN_RIGHT);
        assertThat(fromWestToNorth.targetHeading()).contains(Heading.NORTH);
    }
}
