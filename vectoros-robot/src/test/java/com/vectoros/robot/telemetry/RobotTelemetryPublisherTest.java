package com.vectoros.robot.telemetry;

import com.vectoros.robot.messaging.InMemoryRobotEventPublisher;
import com.vectoros.robot.runtime.model.RobotStatus;
import com.vectoros.robot.runtime.navigation.Heading;
import com.vectoros.robot.runtime.world.Coordinate;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RobotTelemetryPublisherTest {

    @Test
    void delegatesToRobotEventPublisher() {
        InMemoryRobotEventPublisher eventPublisher = new InMemoryRobotEventPublisher();
        RobotTelemetryPublisher telemetryPublisher = new RobotTelemetryPublisher(eventPublisher);

        RobotTelemetrySnapshot snapshot = new RobotTelemetrySnapshot(
                "tel-robot",
                Instant.parse("2026-07-26T14:00:00Z"),
                RobotStatus.IDLE,
                null,
                72.0,
                new Coordinate(1, 2),
                Heading.WEST,
                RobotTelemetryType.MANUAL);

        telemetryPublisher.publish(snapshot);

        assertThat(eventPublisher.telemetrySnapshots()).containsExactly(snapshot);
    }

    @Test
    void rejectsNullSnapshot() {
        RobotTelemetryPublisher publisher =
                new RobotTelemetryPublisher(new InMemoryRobotEventPublisher());
        assertThatThrownBy(() -> publisher.publish(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNullEventPublisher() {
        assertThatThrownBy(() -> new RobotTelemetryPublisher(null))
                .isInstanceOf(NullPointerException.class);
    }
}
