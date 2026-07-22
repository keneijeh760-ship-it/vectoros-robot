# Robot Runtime Architecture

---

# Overview

The Robot Runtime follows a layered architecture.

Fleet communicates through MQTT.

The runtime executes robot behaviour.

The runtime reports state changes back to Fleet.

---

Architecture

Fleet

↓

MQTT

↓

Communication Layer

↓

Runtime Layer (RobotEngine + components)

↓

Hardware Abstraction Layer (HAL)

↓

Simulation Implementation (Current)

↓

Future Hardware Implementation (ESP32 / STM32 / Raspberry Pi / ROS2)

---

Communication Layer

Responsibilities

Receive MQTT events.

Publish MQTT events.

Serialize messages.

No business logic.

---

Runtime Layer

Responsibilities

Execute robot behaviour.

Manage task lifecycle.

Manage robot state.

Coordinate movement.

Publish progress.

This is the heart of the application.

The RobotEngine coordinates runtime components and never communicates directly with simulation or hardware.

---

Hardware Abstraction Layer (HAL)

Responsibilities

Define hardware capabilities as interfaces.

Isolate RobotEngine and runtime components from physical or simulated devices.

Allow simulation and firmware implementations to be swapped without changing runtime logic.

Example capability interfaces

MovementHardware

BatteryHardware

PositionHardware

All movement, battery, and position I/O must pass through these interfaces.

---

Simulation Layer

Responsibilities

Implement HAL interfaces for development and testing.

Move robot.

Update coordinates.

Drain battery.

Calculate travel.

Wait realistic intervals.

No MQTT code belongs here.

No RobotEngine code belongs here.

---

Future Hardware Layer

Future implementations

ESP32

STM32

Raspberry Pi

ROS2

Serial communication

CAN Bus

Motor drivers

Sensors

Firmware adapters implement the same HAL interfaces used by simulation.

The runtime should not know whether it is controlling simulation or hardware.
