package com.vectoros.robot.runtime.navigation;

import java.util.Objects;
import java.util.Optional;

/**
 * Outcome of one navigation tick or navigation lifecycle query.
 */
public final class NavigationResult {

    public enum Status {
        IDLE,
        IN_PROGRESS,
        DESTINATION_REACHED,
        FAILED
    }

    private final Status status;
    private final MovementCommand command;
    private final String message;

    public NavigationResult(Status status, MovementCommand command, String message) {
        this.status = Objects.requireNonNull(status, "status");
        this.command = command;
        this.message = message;
    }

    public static NavigationResult idle() {
        return new NavigationResult(Status.IDLE, null, null);
    }

    public static NavigationResult inProgress(MovementCommand command) {
        return new NavigationResult(Status.IN_PROGRESS, command, null);
    }

    public static NavigationResult destinationReached() {
        return new NavigationResult(Status.DESTINATION_REACHED, MovementCommand.stop(), "Destination reached");
    }

    public static NavigationResult failed(String message) {
        return new NavigationResult(Status.FAILED, MovementCommand.stop(), message);
    }

    public Status status() {
        return status;
    }

    public Optional<MovementCommand> command() {
        return Optional.ofNullable(command);
    }

    public Optional<String> message() {
        return Optional.ofNullable(message);
    }
}
