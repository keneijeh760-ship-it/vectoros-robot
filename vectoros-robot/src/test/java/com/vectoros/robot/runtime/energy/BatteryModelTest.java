package com.vectoros.robot.runtime.energy;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BatteryModelTest {

    @Test
    void enforcesPercentageBounds() {
        assertThatThrownBy(() -> BatteryModel.ofPercentage(-0.1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> BatteryModel.ofPercentage(100.1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(BatteryModel.ofPercentage(0).status()).isEqualTo(BatteryStatus.DEPLETED);
        assertThat(BatteryModel.full().status()).isEqualTo(BatteryStatus.FULL);
    }

    @Test
    void deriveStatusThresholds() {
        assertThat(BatteryModel.ofPercentage(50).status()).isEqualTo(BatteryStatus.NORMAL);
        assertThat(BatteryModel.ofPercentage(20).status()).isEqualTo(BatteryStatus.LOW);
        assertThat(BatteryModel.ofPercentage(10).status()).isEqualTo(BatteryStatus.CRITICAL);
        assertThat(BatteryModel.ofPercentage(0).status()).isEqualTo(BatteryStatus.DEPLETED);
        assertThat(BatteryModel.ofPercentage(80).withCharging(true).status()).isEqualTo(BatteryStatus.CHARGING);
    }

    @Test
    void consumeAndRechargeClamp() {
        assertThat(BatteryModel.ofPercentage(5).consume(10).percentage()).isZero();
        assertThat(BatteryModel.ofPercentage(95).recharge(10).percentage()).isEqualTo(100);
    }

    @Test
    void roundTripsToBatteryState() {
        BatteryModel model = BatteryModel.ofPercentage(77);
        assertThat(BatteryModel.fromBatteryState(model.toBatteryState())).isEqualTo(model);
    }
}
