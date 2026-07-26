package com.vectoros.robot.runtime.energy;

/**
 * Long-term physical condition of the battery pack.
 * Extensible for future degradation modelling.
 */
public enum BatteryHealth {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    CRITICAL
}
