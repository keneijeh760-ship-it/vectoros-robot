package com.vectoros.robot.messaging;

import com.vectoros.robot.runtime.mission.Mission;

import java.util.Objects;

public record AssignMissionCommand(Mission mission) implements RobotCommand {

    public AssignMissionCommand {
        Objects.requireNonNull(mission, "mission");
    }
}
