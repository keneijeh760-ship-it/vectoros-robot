package com.vectoros.robot.runtime.navigation.planner;

import com.vectoros.robot.runtime.navigation.Heading;
import com.vectoros.robot.runtime.navigation.MovementCommand;
import com.vectoros.robot.runtime.world.Coordinate;

/**
 * Produces the next navigation intent toward a destination.
 * Implementations are replaceable (e.g. future A*) without changing NavigationEngine.
 */
public interface MovementPlanner {

    /**
     * @param current           robot grid cell
     * @param currentHeading    robot facing
     * @param destination       target cell
     * @param defaultMoveSpeed  forward speed for one grid step
     * @return next command (STOP when already at destination)
     */
    MovementCommand nextCommand(
            Coordinate current,
            Heading currentHeading,
            Coordinate destination,
            double defaultMoveSpeed);
}
