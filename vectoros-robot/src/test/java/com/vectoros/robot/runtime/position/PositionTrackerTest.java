package com.vectoros.robot.runtime.position;

import com.vectoros.robot.runtime.hal.PositionHardware;
import com.vectoros.robot.runtime.model.Position;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PositionTrackerTest {

    @Test
    void readsPositionAndHeadingFromHardware() {
        PositionTracker tracker = new PositionTracker(new StubPositionHardware(new Position(4, 2), 45));

        assertThat(tracker.currentPosition()).isEqualTo(new Position(4, 2));
        assertThat(tracker.currentHeadingDegrees()).isEqualTo(45);
    }

    private static final class StubPositionHardware implements PositionHardware {
        private final Position position;
        private final double heading;

        private StubPositionHardware(Position position, double heading) {
            this.position = position;
            this.heading = heading;
        }

        @Override
        public Position readPosition() {
            return position;
        }

        @Override
        public double readHeading() {
            return heading;
        }
    }
}
