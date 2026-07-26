package com.vectoros.robot.runtime.energy;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FixedStepEnergyConsumptionModelTest {

    @Test
    void returnsConfiguredFixedAmount() {
        FixedStepEnergyConsumptionModel model = new FixedStepEnergyConsumptionModel(2.5);

        assertThat(model.consumptionForMovementStep(MovementEnergyContext.simpleStep(1.0)))
                .isEqualTo(2.5);
        assertThat(model.energyPerStep()).isEqualTo(2.5);
    }

    @Test
    void defaultIsOnePercentPerStep() {
        assertThat(new FixedStepEnergyConsumptionModel().energyPerStep()).isEqualTo(1.0);
    }

    @Test
    void rejectsNegativeEnergyPerStep() {
        assertThatThrownBy(() -> new FixedStepEnergyConsumptionModel(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void ignoresContextFieldsInFixedModel() {
        FixedStepEnergyConsumptionModel model = new FixedStepEnergyConsumptionModel(1.0);
        MovementEnergyContext rich = new MovementEnergyContext(3.0, 50.0, "ROUGH");

        assertThat(model.consumptionForMovementStep(rich)).isEqualTo(1.0);
    }
}
