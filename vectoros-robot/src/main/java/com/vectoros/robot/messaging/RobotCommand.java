package com.vectoros.robot.messaging;

/**
 * External command delivered into the robot runtime.
 */
public sealed interface RobotCommand permits AssignMissionCommand, CancelMissionCommand {
}
