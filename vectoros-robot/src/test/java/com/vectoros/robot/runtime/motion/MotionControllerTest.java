package com.vectoros.robot.runtime.motion;

import com.vectoros.robot.runtime.hal.MovementHardware;
import com.vectoros.robot.runtime.model.Position;
import com.vectoros.robot.runtime.navigation.Heading;
import com.vectoros.robot.runtime.navigation.MovementCommand;
import com.vectoros.robot.runtime.world.Coordinate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MotionControllerTest {

    private RecordingMovementHardware hardware;
    private MotionController controller;

    @BeforeEach
    void setUp() {
        hardware = new RecordingMovementHardware();
        controller = new MotionController(hardware, 2.0);
    }

    @Test
    void executeMoveForwardUsesHeadingAndSpeed() {
        controller.execute(MovementCommand.moveForward(new Coordinate(1, 0), Heading.EAST, 1.0));

        assertThat(hardware.moveCalled).isTrue();
        assertThat(hardware.lastHeading).isEqualTo(0.0);
        assertThat(hardware.lastSpeed).isEqualTo(1.0);
    }

    @Test
    void executeTurnUpdatesHeadingWithoutTranslationSpeed() {
        controller.execute(MovementCommand.turnLeft(Heading.NORTH));

        assertThat(hardware.lastHeading).isEqualTo(90.0);
        assertThat(hardware.lastSpeed).isZero();
    }

    @Test
    void executeStopDelegatesToHardware() {
        controller.execute(MovementCommand.stop());
        assertThat(hardware.stopCalled).isTrue();
    }

    @Test
    void moveTowardCommandsHeadingAndClampedSpeed() {
        controller.moveToward(new Position(0, 0), new Position(10, 0));

        assertThat(hardware.lastHeading).isEqualTo(0.0);
        assertThat(hardware.lastSpeed).isEqualTo(2.0);
    }

    @Test
    void moveTowardDoesNotOvershootWhenClose() {
        controller.moveToward(new Position(0, 0), new Position(0.5, 0));

        assertThat(hardware.lastSpeed).isEqualTo(0.5);
    }

    @Test
    void moveTowardStopsWhenAlreadyAtTarget() {
        controller.moveToward(new Position(1, 1), new Position(1, 1));

        assertThat(hardware.stopCalled).isTrue();
        assertThat(hardware.moveCalled).isFalse();
    }

    @Test
    void stopDelegatesToHardware() {
        controller.stop();
        assertThat(hardware.stopCalled).isTrue();
    }

    private static final class RecordingMovementHardware implements MovementHardware {
        private double lastHeading;
        private double lastSpeed;
        private boolean moveCalled;
        private boolean stopCalled;

        @Override
        public void move(double headingDegrees, double speed) {
            moveCalled = true;
            lastHeading = headingDegrees;
            lastSpeed = speed;
        }

        @Override
        public void stop() {
            stopCalled = true;
            lastSpeed = 0;
        }

        @Override
        public double readSpeed() {
            return lastSpeed;
        }
    }
}
