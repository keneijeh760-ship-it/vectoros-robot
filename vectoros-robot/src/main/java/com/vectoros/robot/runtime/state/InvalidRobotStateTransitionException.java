package com.vectoros.robot.runtime.state;

import com.vectoros.robot.runtime.model.RobotStatus;

/**
 * Thrown when a requested status change is not permitted by the transition table.
 */
public final class InvalidRobotStateTransitionException extends IllegalStateException {

    private final RobotStatus from;
    private final RobotStatus attemptedTo;
    private final RobotStateEvent event;

    public InvalidRobotStateTransitionException(
            RobotStatus from,
            RobotStatus attemptedTo,
            RobotStateEvent event) {
        super(buildMessage(from, attemptedTo, event));
        this.from = from;
        this.attemptedTo = attemptedTo;
        this.event = event;
    }

    public InvalidRobotStateTransitionException(RobotStatus from, RobotStateEvent event) {
        super("Invalid robot state transition: cannot apply event "
                + event
                + " while in status "
                + from);
        this.from = from;
        this.attemptedTo = null;
        this.event = event;
    }

    public RobotStatus from() {
        return from;
    }

    public RobotStatus attemptedTo() {
        return attemptedTo;
    }

    public RobotStateEvent event() {
        return event;
    }

    private static String buildMessage(RobotStatus from, RobotStatus attemptedTo, RobotStateEvent event) {
        return "Invalid robot state transition: "
                + from
                + " -["
                + event
                + "]-> "
                + attemptedTo
                + " is not allowed";
    }
}
