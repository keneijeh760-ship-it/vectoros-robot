package com.vectoros.robot.runtime.model;

/**
 * Immutable battery charge representation (0–100%).
 */
public final class BatteryState {

    public static final double LOW_THRESHOLD_PERCENT = 20.0;
    public static final double MIN_PERCENT = 0.0;
    public static final double MAX_PERCENT = 100.0;

    private final double percentage;

    public BatteryState(double percentage) {
        if (!Double.isFinite(percentage)) {
            throw new IllegalArgumentException("Battery percentage must be finite");
        }
        if (percentage < MIN_PERCENT || percentage > MAX_PERCENT) {
            throw new IllegalArgumentException(
                    "Battery percentage must be between " + MIN_PERCENT + " and " + MAX_PERCENT);
        }
        this.percentage = percentage;
    }

    public static BatteryState full() {
        return new BatteryState(MAX_PERCENT);
    }

    public double percentage() {
        return percentage;
    }

    public boolean isLow() {
        return percentage <= LOW_THRESHOLD_PERCENT;
    }

    public boolean isEmpty() {
        return percentage <= MIN_PERCENT;
    }

    public BatteryState drain(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Drain amount must be non-negative");
        }
        return new BatteryState(clamp(percentage - amount));
    }

    public BatteryState charge(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Charge amount must be non-negative");
        }
        return new BatteryState(clamp(percentage + amount));
    }

    private static double clamp(double value) {
        return Math.max(MIN_PERCENT, Math.min(MAX_PERCENT, value));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BatteryState that)) {
            return false;
        }
        return Double.compare(that.percentage, percentage) == 0;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(percentage);
    }

    @Override
    public String toString() {
        return "BatteryState{percentage=" + percentage + '}';
    }
}
