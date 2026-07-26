package com.vectoros.robot.runtime.energy;

/**
 * Operational charge status derived from percentage / charging flag.
 */
public enum BatteryStatus {
    FULL,
    NORMAL,
    LOW,
    CRITICAL,
    DEPLETED,
    CHARGING
}
