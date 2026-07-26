package com.vectoros.robot.runtime.hal.simulation;

import com.vectoros.robot.runtime.hal.BatteryHardware;
import com.vectoros.robot.runtime.hal.MovementHardware;
import com.vectoros.robot.runtime.hal.PositionHardware;
import com.vectoros.robot.runtime.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class SimulationHardwareTest {

    private MovementHardware movement;
    private BatteryHardware battery;
    private PositionHardware position;

    @BeforeEach
    void setUp() {
        SimulationHardwareFactory factory = SimulationHardwareFactory.createDefault();
        movement = factory.movementHardware();
        battery = factory.batteryHardware();
        position = factory.positionHardware();
    }

    @Test
    void movementAdvancesSharedPositionReadableByPositionHardware() {
        movement.move(0, 2.0);

        assertThat(position.readPosition()).isEqualTo(new Position(2.0, 0.0));
        assertThat(position.readHeading()).isEqualTo(0.0);
        assertThat(movement.readSpeed()).isEqualTo(2.0);
    }

    @Test
    void stopClearsSpeedWithoutChangingPosition() {
        movement.move(90, 1.0);
        Position afterMove = position.readPosition();

        movement.stop();

        assertThat(movement.readSpeed()).isZero();
        assertThat(position.readPosition()).isEqualTo(afterMove);
    }

    @Test
    void batteryDrainAndChargeAreClamped() {
        battery.drain(30);
        assertThat(battery.readPercentage()).isEqualTo(70);

        battery.charge(50);
        assertThat(battery.readPercentage()).isEqualTo(100);

        battery.drain(200);
        assertThat(battery.readPercentage()).isZero();
    }

    @Test
    void customInitialPoseIsHonoured() {
        SimulationHardwareFactory factory =
                SimulationHardwareFactory.create(new Position(3, 4), 180, 55);

        assertThat(factory.positionHardware().readPosition()).isEqualTo(new Position(3, 4));
        assertThat(factory.positionHardware().readHeading()).isEqualTo(180);
        assertThat(factory.batteryHardware().readPercentage()).isEqualTo(55);
    }

    @Test
    void diagonalMoveUpdatesCoordinatesDeterministically() {
        movement.move(45, Math.sqrt(2));

        assertThat(position.readPosition().x()).isCloseTo(1.0, within(1e-9));
        assertThat(position.readPosition().y()).isCloseTo(1.0, within(1e-9));
    }
}
