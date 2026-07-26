package com.vectoros.robot.runtime.motion;

import com.vectoros.robot.runtime.hal.MovementHardware;
import com.vectoros.robot.runtime.model.Position;
import com.vectoros.robot.runtime.navigation.Heading;
import com.vectoros.robot.runtime.navigation.MovementCommand;

import java.util.Objects;

/**
 * Executes {@link MovementCommand} through the HAL.
 * Does not plan routes, choose destinations, or mutate {@code RobotState}.
 */
public final class MotionController {

    private final MovementHardware movementHardware;
    private final double defaultCruiseSpeed;

    public MotionController(MovementHardware movementHardware, double defaultCruiseSpeed) {
        this.movementHardware = Objects.requireNonNull(movementHardware, "movementHardware");
        if (defaultCruiseSpeed <= 0 || !Double.isFinite(defaultCruiseSpeed)) {
            throw new IllegalArgumentException("defaultCruiseSpeed must be a positive finite number");
        }
        this.defaultCruiseSpeed = defaultCruiseSpeed;
    }

    public MotionController(MovementHardware movementHardware) {
        this(movementHardware, 1.0);
    }

    /**
     * Translate one navigation command into HAL actions.
     */
    public void execute(MovementCommand command) {
        Objects.requireNonNull(command, "command");
        switch (command.movementType()) {
            case MOVE_FORWARD -> {
                Heading heading = command.targetHeading()
                        .orElseThrow(() -> new IllegalArgumentException("MOVE_FORWARD requires targetHeading"));
                double speed = command.speed() > 0 ? command.speed() : defaultCruiseSpeed;
                movementHardware.move(heading.degrees(), speed);
            }
            case TURN_LEFT, TURN_RIGHT -> {
                Heading heading = command.targetHeading()
                        .orElseThrow(() -> new IllegalArgumentException(
                                command.movementType() + " requires targetHeading"));
                movementHardware.move(heading.degrees(), 0.0);
            }
            case STOP -> movementHardware.stop();
        }
    }

    /**
     * Legacy continuous step toward a point. Prefer {@link #execute(MovementCommand)}.
     */
    public void moveToward(Position current, Position target) {
        Objects.requireNonNull(current, "current");
        Objects.requireNonNull(target, "target");

        double distance = current.distanceTo(target);
        if (distance == 0.0) {
            stop();
            return;
        }

        double heading = current.headingDegreesToward(target);
        double stepSpeed = Math.min(defaultCruiseSpeed, distance);
        movementHardware.move(heading, stepSpeed);
    }

    public void stop() {
        movementHardware.stop();
    }

    public double readSpeed() {
        return movementHardware.readSpeed();
    }

    public double defaultCruiseSpeed() {
        return defaultCruiseSpeed;
    }
}
