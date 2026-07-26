package com.vectoros.robot.runtime.mission;

/**
 * Thrown when mission acceptance or progression violates lifecycle rules.
 */
public final class IllegalMissionStateException extends IllegalStateException {

    public IllegalMissionStateException(String message) {
        super(message);
    }
}
