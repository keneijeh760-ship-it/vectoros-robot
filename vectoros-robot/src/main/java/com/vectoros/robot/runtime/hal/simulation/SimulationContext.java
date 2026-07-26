package com.vectoros.robot.runtime.hal.simulation;

import com.vectoros.robot.runtime.model.Position;

/**
 * Shared mutable simulation world used by HAL simulation adapters.
 * Not part of the public runtime API — RobotEngine never depends on this type.
 */
final class SimulationContext {

    private Position position;
    private double headingDegrees;
    private double speed;
    private double batteryPercentage;

    SimulationContext(Position position, double headingDegrees, double batteryPercentage) {
        this.position = position;
        this.headingDegrees = normalizeDegrees(headingDegrees);
        this.speed = 0.0;
        this.batteryPercentage = clampBattery(batteryPercentage);
    }

    static SimulationContext defaults() {
        return new SimulationContext(Position.origin(), 0.0, 100.0);
    }

    Position position() {
        return position;
    }

    double headingDegrees() {
        return headingDegrees;
    }

    double speed() {
        return speed;
    }

    double batteryPercentage() {
        return batteryPercentage;
    }

    void stop() {
        this.speed = 0.0;
    }

    void move(double headingDegrees, double speed) {
        if (speed < 0 || !Double.isFinite(speed)) {
            throw new IllegalArgumentException("speed must be a non-negative finite number");
        }
        this.headingDegrees = normalizeDegrees(headingDegrees);
        this.speed = speed;
        if (speed == 0.0) {
            return;
        }
        double radians = Math.toRadians(this.headingDegrees);
        double dx = Math.cos(radians) * speed;
        double dy = Math.sin(radians) * speed;
        this.position = this.position.translate(dx, dy);
    }

    void drain(double amount) {
        if (amount < 0 || !Double.isFinite(amount)) {
            throw new IllegalArgumentException("drain amount must be a non-negative finite number");
        }
        this.batteryPercentage = clampBattery(this.batteryPercentage - amount);
    }

    void charge(double amount) {
        if (amount < 0 || !Double.isFinite(amount)) {
            throw new IllegalArgumentException("charge amount must be a non-negative finite number");
        }
        this.batteryPercentage = clampBattery(this.batteryPercentage + amount);
    }

    private static double normalizeDegrees(double degrees) {
        if (!Double.isFinite(degrees)) {
            throw new IllegalArgumentException("heading must be finite");
        }
        double normalized = degrees % 360.0;
        if (normalized < 0) {
            normalized += 360.0;
        }
        return normalized;
    }

    private static double clampBattery(double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("battery percentage must be finite");
        }
        return Math.max(0.0, Math.min(100.0, value));
    }
}
