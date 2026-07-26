package com.vectoros.robot.messaging;

import java.util.Optional;

/**
 * Request to cancel the active mission.
 * {@code missionId} is optional metadata for fleet correlation.
 */
public record CancelMissionCommand(String missionId) implements RobotCommand {

    public Optional<String> optionalMissionId() {
        return Optional.ofNullable(missionId).filter(id -> !id.isBlank());
    }
}
