package com.vectoros.robot.runtime.hal;

/**
 * Hardware capability for battery sensing and energy accounting.
 */
public interface BatteryHardware {

    /**
     * @return current charge percentage in range [0, 100]
     */
    double readPercentage();

    /**
     * Apply energy consumption.
     *
     * @param amount percentage points to drain (non-negative)
     */
    void drain(double amount);

    /**
     * Apply energy replenishment.
     *
     * @param amount percentage points to charge (non-negative)
     */
    void charge(double amount);
}
