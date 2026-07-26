package com.vectoros.robot.runtime.energy;

import com.vectoros.robot.runtime.model.BatteryState;

import java.util.Objects;

/**
 * Immutable battery domain snapshot (0–100%).
 * Owned and updated exclusively via {@link EnergyManager}.
 */
public final class BatteryModel {

    public static final double MIN_PERCENT = 0.0;
    public static final double MAX_PERCENT = 100.0;
    public static final double LOW_THRESHOLD_PERCENT = 20.0;
    public static final double CRITICAL_THRESHOLD_PERCENT = 10.0;

    private final double percentage;
    private final boolean charging;
    private final double capacity;
    private final BatteryHealth health;
    private final BatteryStatus status;

    public BatteryModel(
            double percentage,
            boolean charging,
            double capacity,
            BatteryHealth health) {
        if (!Double.isFinite(percentage)) {
            throw new IllegalArgumentException("percentage must be finite");
        }
        if (percentage < MIN_PERCENT || percentage > MAX_PERCENT) {
            throw new IllegalArgumentException(
                    "percentage must be between " + MIN_PERCENT + " and " + MAX_PERCENT);
        }
        if (!Double.isFinite(capacity) || capacity <= 0) {
            throw new IllegalArgumentException("capacity must be a positive finite number");
        }
        this.percentage = percentage;
        this.charging = charging;
        this.capacity = capacity;
        this.health = Objects.requireNonNull(health, "health");
        this.status = deriveStatus(percentage, charging);
    }

    public static BatteryModel full() {
        return new BatteryModel(MAX_PERCENT, false, MAX_PERCENT, BatteryHealth.GOOD);
    }

    public static BatteryModel ofPercentage(double percentage) {
        return new BatteryModel(percentage, false, MAX_PERCENT, BatteryHealth.GOOD);
    }

    public double percentage() {
        return percentage;
    }

    public boolean charging() {
        return charging;
    }

    public double capacity() {
        return capacity;
    }

    public BatteryHealth health() {
        return health;
    }

    public BatteryStatus status() {
        return status;
    }

    public boolean isLow() {
        return percentage <= LOW_THRESHOLD_PERCENT && percentage > CRITICAL_THRESHOLD_PERCENT;
    }

    public boolean isCritical() {
        return percentage <= CRITICAL_THRESHOLD_PERCENT && percentage > MIN_PERCENT;
    }

    public boolean isDepleted() {
        return percentage <= MIN_PERCENT;
    }

    public boolean isFull() {
        return percentage >= MAX_PERCENT && !charging;
    }

    public BatteryModel withPercentage(double newPercentage) {
        return new BatteryModel(clamp(newPercentage), charging, capacity, health);
    }

    public BatteryModel withCharging(boolean charging) {
        return new BatteryModel(percentage, charging, capacity, health);
    }

    public BatteryModel withHealth(BatteryHealth health) {
        return new BatteryModel(percentage, charging, capacity, health);
    }

    public BatteryModel consume(double amount) {
        if (amount < 0 || !Double.isFinite(amount)) {
            throw new IllegalArgumentException("consume amount must be a non-negative finite number");
        }
        return withPercentage(percentage - amount);
    }

    /**
     * Future charging extension point. Sprint 05 does not activate charging behaviour.
     */
    public BatteryModel recharge(double amount) {
        if (amount < 0 || !Double.isFinite(amount)) {
            throw new IllegalArgumentException("recharge amount must be a non-negative finite number");
        }
        return withPercentage(percentage + amount);
    }

    public BatteryState toBatteryState() {
        return new BatteryState(percentage);
    }

    public static BatteryModel fromBatteryState(BatteryState state) {
        Objects.requireNonNull(state, "state");
        return ofPercentage(state.percentage());
    }

    private static BatteryStatus deriveStatus(double percentage, boolean charging) {
        if (charging) {
            return BatteryStatus.CHARGING;
        }
        if (percentage <= MIN_PERCENT) {
            return BatteryStatus.DEPLETED;
        }
        if (percentage <= CRITICAL_THRESHOLD_PERCENT) {
            return BatteryStatus.CRITICAL;
        }
        if (percentage <= LOW_THRESHOLD_PERCENT) {
            return BatteryStatus.LOW;
        }
        if (percentage >= MAX_PERCENT) {
            return BatteryStatus.FULL;
        }
        return BatteryStatus.NORMAL;
    }

    private static double clamp(double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("percentage must be finite");
        }
        return Math.max(MIN_PERCENT, Math.min(MAX_PERCENT, value));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BatteryModel that)) {
            return false;
        }
        return Double.compare(that.percentage, percentage) == 0
                && charging == that.charging
                && Double.compare(that.capacity, capacity) == 0
                && health == that.health
                && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(percentage, charging, capacity, health, status);
    }

    @Override
    public String toString() {
        return "BatteryModel{percentage=" + percentage
                + ", charging=" + charging
                + ", capacity=" + capacity
                + ", health=" + health
                + ", status=" + status
                + '}';
    }
}
