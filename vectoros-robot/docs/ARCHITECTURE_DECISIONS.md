# Architecture Decisions

---

## ADR-001

Robot Runtime owns execution.

Fleet owns orchestration.

---

## ADR-002

MQTT is the only communication mechanism.

No REST between Robot and Fleet.

---

## ADR-003

Business logic never accesses MQTT directly.

Communication is abstracted.

---

## ADR-004

Simulation is replaceable.

Movement must depend on interfaces.

---

## ADR-005

Robot state is managed centrally.

Battery

Position

Heading

Speed

Status

Task

must remain internally consistent.

---

## ADR-006

The runtime must support future hardware implementations.

Simulation should never become tightly coupled to movement logic.

---

## ADR-007

Hardware Abstraction Layer (HAL) sits between runtime components and device I/O.

RobotEngine must never communicate directly with simulation or hardware.

All movement, battery, and position commands pass through HAL interfaces.

Simulation is the current HAL implementation.

Future ESP32 / STM32 / Raspberry Pi / ROS2 adapters replace simulation without changing RobotEngine.
