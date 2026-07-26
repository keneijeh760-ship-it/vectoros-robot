package com.vectoros.robot.runtime.energy;

import com.vectoros.robot.runtime.energy.events.BatteryCriticalEvent;
import com.vectoros.robot.runtime.energy.events.BatteryDepletedEvent;
import com.vectoros.robot.runtime.energy.events.BatteryLowEvent;
import com.vectoros.robot.runtime.events.RuntimeEventPublisher;
import com.vectoros.robot.runtime.hal.BatteryHardware;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

/**
 * Sole authority for battery state and energy consumption.
 * MissionManager / NavigationEngine may read {@link #currentBattery()} but must never mutate energy.
 */
public final class EnergyManager {

    private final String robotId;
    private final BatteryHardware batteryHardware;
    private final EnergyConsumptionModel consumptionModel;
    private final RuntimeEventPublisher eventPublisher;
    private final Clock clock;

    private BatteryModel battery;
    private boolean lowAnnounced;
    private boolean criticalAnnounced;
    private boolean depletedAnnounced;

    public EnergyManager(
            String robotId,
            BatteryHardware batteryHardware,
            EnergyConsumptionModel consumptionModel,
            RuntimeEventPublisher eventPublisher,
            Clock clock) {
        if (robotId == null || robotId.isBlank()) {
            throw new IllegalArgumentException("robotId must not be blank");
        }
        this.robotId = robotId;
        this.batteryHardware = Objects.requireNonNull(batteryHardware, "batteryHardware");
        this.consumptionModel = Objects.requireNonNull(consumptionModel, "consumptionModel");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.battery = BatteryModel.ofPercentage(batteryHardware.readPercentage());
        resetAnnouncementFlags();
    }

    public EnergyManager(
            String robotId,
            BatteryHardware batteryHardware,
            RuntimeEventPublisher eventPublisher,
            Clock clock) {
        this(robotId, batteryHardware, new FixedStepEnergyConsumptionModel(), eventPublisher, clock);
    }

    public BatteryModel currentBattery() {
        return battery;
    }

    /**
     * Refresh model from HAL without consuming energy.
     */
    public BatteryModel syncFromHardware() {
        this.battery = new BatteryModel(
                batteryHardware.readPercentage(),
                battery.charging(),
                battery.capacity(),
                battery.health());
        return battery;
    }

    /**
     * Consume energy for one successful movement step and emit threshold events.
     */
    public BatteryModel consumeEnergy(MovementEnergyContext context) {
        Objects.requireNonNull(context, "context");
        double amount = consumptionModel.consumptionForMovementStep(context);
        if (amount > 0) {
            batteryHardware.drain(amount);
        }
        this.battery = new BatteryModel(
                batteryHardware.readPercentage(),
                false,
                battery.capacity(),
                battery.health());
        emitThresholdEvents(clock.instant());
        return battery;
    }

    public BatteryModel consumeEnergyForMovementStep(double speed) {
        return consumeEnergy(MovementEnergyContext.simpleStep(speed));
    }

    /**
     * Future charging extension. Applies charge through HAL and updates the model.
     * Not used by runtime tick in Sprint 05.
     */
    public BatteryModel recharge(double amount) {
        if (amount < 0 || !Double.isFinite(amount)) {
            throw new IllegalArgumentException("recharge amount must be a non-negative finite number");
        }
        if (amount > 0) {
            batteryHardware.charge(amount);
        }
        this.battery = new BatteryModel(
                batteryHardware.readPercentage(),
                true,
                battery.capacity(),
                battery.health());
        resetAnnouncementFlagsIfRecovered();
        return battery;
    }

    private void emitThresholdEvents(Instant now) {
        if (battery.isDepleted()) {
            if (!depletedAnnounced) {
                eventPublisher.publish(new BatteryDepletedEvent(robotId, battery, now));
                depletedAnnounced = true;
                criticalAnnounced = true;
                lowAnnounced = true;
            }
            return;
        }
        depletedAnnounced = false;

        if (battery.status() == BatteryStatus.CRITICAL) {
            if (!criticalAnnounced) {
                eventPublisher.publish(new BatteryCriticalEvent(robotId, battery, now));
                criticalAnnounced = true;
                lowAnnounced = true;
            }
            return;
        }
        criticalAnnounced = false;

        if (battery.status() == BatteryStatus.LOW) {
            if (!lowAnnounced) {
                eventPublisher.publish(new BatteryLowEvent(robotId, battery, now));
                lowAnnounced = true;
            }
            return;
        }
        lowAnnounced = false;
    }

    private void resetAnnouncementFlags() {
        lowAnnounced = battery.status() == BatteryStatus.LOW
                || battery.status() == BatteryStatus.CRITICAL
                || battery.status() == BatteryStatus.DEPLETED;
        criticalAnnounced = battery.status() == BatteryStatus.CRITICAL
                || battery.status() == BatteryStatus.DEPLETED;
        depletedAnnounced = battery.status() == BatteryStatus.DEPLETED;
    }

    private void resetAnnouncementFlagsIfRecovered() {
        if (battery.status() != BatteryStatus.DEPLETED) {
            depletedAnnounced = false;
        }
        if (battery.status() != BatteryStatus.CRITICAL && battery.status() != BatteryStatus.DEPLETED) {
            criticalAnnounced = false;
        }
        if (battery.status() == BatteryStatus.NORMAL
                || battery.status() == BatteryStatus.FULL
                || battery.status() == BatteryStatus.CHARGING) {
            lowAnnounced = false;
            criticalAnnounced = false;
            depletedAnnounced = false;
        }
    }
}
