package com.vectoros.robot.messaging;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryRobotEventPublisherTest {

    @Test
    void recordsAllMessageChannelsIndependently() {
        InMemoryRobotEventPublisher publisher = new InMemoryRobotEventPublisher();
        Instant now = Instant.parse("2026-07-26T18:00:00Z");

        publisher.publishStatus(new RobotStatusMessage("r", "IDLE", now));
        publisher.publishMission(new RobotMissionMessage("r", "m", RobotMissionMessage.EventType.STARTED, now));
        publisher.publishBattery(new RobotBatteryMessage("r", 50, "NORMAL", now));
        publisher.publishPosition(new RobotPositionMessage("r", 1, 2, "NORTH", now));

        assertThat(publisher.statusMessages()).hasSize(1);
        assertThat(publisher.missionMessages()).hasSize(1);
        assertThat(publisher.batteryMessages()).hasSize(1);
        assertThat(publisher.positionMessages()).hasSize(1);

        publisher.clear();
        assertThat(publisher.statusMessages()).isEmpty();
    }
}
