# WarehouseOS Robot Runtime

Version: 1.0

---

# Purpose

The Robot Runtime is responsible for simulating and eventually controlling autonomous warehouse robots.

Unlike the Fleet Service, which manages warehouse operations, the Robot Runtime represents software executing on an individual robot.

It receives commands, executes work, monitors robot state and reports events back to Fleet.

The Robot Runtime should be designed so that simulation can later be replaced with real hardware.

---

# Responsibilities

Receive task assignments.

Execute robot movement.

Track robot position.

Track battery level.

Track robot state.

Publish telemetry.

Publish task progress.

Publish task completion.

Receive future firmware updates.

---

# Non Responsibilities

The Robot Runtime does NOT:

Schedule robots.

Manage warehouse inventory.

Assign tasks.

Store historical warehouse data.

Make fleet-wide decisions.

Those responsibilities belong to Fleet.

---

# Long-Term Vision

Today:

Fleet

↓

MQTT

↓

Robot Runtime

↓

Hardware Abstraction Layer

↓

Simulation

Future:

Fleet

↓

MQTT

↓

Robot Runtime

↓

Hardware Abstraction Layer

↓

ESP32 / STM32 / Raspberry Pi / ROS2

↓

Motors / Sensors

HAL interfaces exist from Sprint 01. Simulation is the current implementation; firmware adapters replace it later without changing RobotEngine.

---

# Guiding Principles

Keep business logic independent from MQTT.

Keep movement independent from hardware.

Use interfaces for replaceable implementations.

Avoid coupling simulation to communication.

Write code as if it will eventually run on a real robot.