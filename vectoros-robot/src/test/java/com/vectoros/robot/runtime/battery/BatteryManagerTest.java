package com.vectoros.robot.runtime.battery;

import com.vectoros.robot.runtime.hal.BatteryHardware;
import com.vectoros.robot.runtime.model.BatteryState;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BatteryManagerTest {

    @Test
    void updateDrainsMoreWhenMoving() {
        FakeBatteryHardware hardware = new FakeBatteryHardware(100);
        BatteryManager manager = new BatteryManager(hardware, 1.0, 5.0);

        BatteryState idle = manager.update(false);
        BatteryState moving = manager.update(true);

        assertThat(idle.percentage()).isEqualTo(99.0);
        assertThat(moving.percentage()).isEqualTo(94.0);
    }

    @Test
    void currentStateReadsHardwareWithoutDraining() {
        FakeBatteryHardware hardware = new FakeBatteryHardware(77);
        BatteryManager manager = new BatteryManager(hardware);

        assertThat(manager.currentState()).isEqualTo(new BatteryState(77));
        assertThat(hardware.percentage).isEqualTo(77);
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
