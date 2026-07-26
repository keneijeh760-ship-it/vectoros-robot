package com.vectoros.robot.messaging;

import com.vectoros.robot.runtime.engine.RobotEngine;

import java.util.Objects;

/**
 * Forwards inbound commands to {@link RobotEngine} without transport concerns.
 */
public final class EngineRobotCommandReceiver implements RobotCommandReceiver {

    private final RobotEngine robotEngine;

    public EngineRobotCommandReceiver(RobotEngine robotEngine) {
        this.robotEngine = Objects.requireNonNull(robotEngine, "robotEngine");
    }

    @Override
    public void receive(RobotCommand command) {
        Objects.requireNonNull(command, "command");
        switch (command) {
            case AssignMissionCommand assign -> robotEngine.assignMission(assign.mission());
            case CancelMissionCommand ignored -> robotEngine.cancelMission();
        }
    }
}
