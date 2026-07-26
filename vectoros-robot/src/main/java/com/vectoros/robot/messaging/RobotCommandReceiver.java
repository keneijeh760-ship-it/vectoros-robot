package com.vectoros.robot.messaging;

/**
 * Inbound application port for external robot commands.
 * Transport adapters deserialize messages and forward here.
 */
public interface RobotCommandReceiver {

    void receive(RobotCommand command);
}
