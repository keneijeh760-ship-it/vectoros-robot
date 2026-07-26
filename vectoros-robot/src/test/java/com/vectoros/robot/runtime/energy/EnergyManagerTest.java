package com.vectoros.robot.runtime.energy;

import com.vectoros.robot.runtime.energy.events.BatteryCriticalEvent;
import com.vectoros.robot.runtime.energy.events.BatteryDepletedEvent;
import com.vectoros.robot.runtime.energy.events.BatteryLowEvent;
import com.vectoros.robot.runtime.events.InMemoryRuntimeEventBus;
import com.vectoros.robot.runtime.hal.BatteryHardware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class EnergyManagerTest {

    private static final Instant FIXED = Instant.parse("2026-07-26T12:00:00Z");

    private FakeBatteryHardware hardware;
    private InMemoryRuntimeEventBus eventBus;
    private EnergyManager energyManager;

    @BeforeEach
    void setUp() {
        hardware = new FakeBatteryHardware(100);
        eventBus = new InMemoryRuntimeEventBus();
        energyManager = new EnergyManager(
                "robot-energy",
                hardware,
                new FixedStepEnergyConsumptionModel(5.0),
                eventBus,
                Clock.fixed(FIXED, ZoneOffset.UTC));
    }

    @Test
    void consumeEnergyDrainsHardwareAndUpdatesModel() {
        BatteryModel after = energyManager.consumeEnergyForMovementStep(1.0);

        assertThat(after.percentage()).isEqualTo(95.0);
        assertThat(hardware.percentage).isEqualTo(95.0);
        assertThat(energyManager.currentBattery().percentage()).isEqualTo(95.0);
    }

    @Test
    void syncFromHardwareDoesNotConsume() {
        hardware.percentage = 42;
        BatteryModel synced = energyManager.syncFromHardware();

        assertThat(synced.percentage()).isEqualTo(42.0);
        assertThat(eventBus.history()).isEmpty();
    }

    @Test
    void emitsLowEventOnce() {
        hardware.percentage = 21;
        energyManager.syncFromHardware();

        energyManager.consumeEnergyForMovementStep(1.0); // -> 16
        energyManager.consumeEnergyForMovementStep(1.0); // -> 11

        assertThat(eventBus.historyOfType(BatteryLowEvent.class)).hasSize(1);
        assertThat(eventBus.historyOfType(BatteryCriticalEvent.class)).isEmpty();
    }

    @Test
    void emitsCriticalThenDepleted() {
        hardware.percentage = 12;
        energyManager = new EnergyManager(
                "robot-energy",
                hardware,
                new FixedStepEnergyConsumptionModel(5.0),
                eventBus,
                Clock.fixed(FIXED, ZoneOffset.UTC));

        energyManager.consumeEnergyForMovementStep(1.0); // 7 critical
        energyManager.consumeEnergyForMovementStep(1.0); // 2 still critical
        energyManager.consumeEnergyForMovementStep(1.0); // 0 depleted

        assertThat(eventBus.historyOfType(BatteryCriticalEvent.class)).hasSize(1);
        assertThat(eventBus.historyOfType(BatteryDepletedEvent.class)).hasSize(1);
        assertThat(energyManager.currentBattery().isDepleted()).isTrue();
    }

    @Test
    void rechargeExtensionUpdatesModel() {
        energyManager.consumeEnergyForMovementStep(1.0);
        BatteryModel charged = energyManager.recharge(10);

        assertThat(charged.percentage()).isEqualTo(100.0);
        assertThat(charged.charging()).isTrue();
    }

    private static final class FakeBatteryHardware implements BatteryHardware {
        private double percentage;

        private FakeBatteryHardware(double percentage) {
            this.percentage = percentage;
        }

        @Override
        public double readPercentage() {
            return percentage;
        }

        @Override
        public void drain(double amount) {
            percentage = Math.max(0, percentage - amount);
        }

        @Override
        public void charge(double amount) {
            percentage = Math.min(100, percentage + amount);
        }
    }
}
