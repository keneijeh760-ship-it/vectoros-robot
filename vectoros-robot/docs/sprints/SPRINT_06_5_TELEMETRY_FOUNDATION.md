# Sprint 06.5 – Telemetry Foundation

Version: 1.0

Status: Ready

Estimated Duration: Half Day

---

# Sprint Goal

Introduce a canonical telemetry model for the Robot Runtime.

Telemetry represents the current state of the robot.

It is independent from domain events.

Domain events describe things that happened.

Telemetry describes what the robot currently looks like.

---

# Business Context

Fleet services require a consistent robot state.

Dashboards require snapshots.

Analytics require timestamps.

Telemetry provides this unified representation.

---

# Responsibilities

Represent robot state.

Publish snapshots.

Remain independent from MQTT.

Support future dashboards.

---

# Out of Scope

Mission history

Persistence

Analytics

REST

Dashboard

---

# Components

## RobotTelemetrySnapshot

Represents the current robot state.

Contains

- Robot ID
- Timestamp
- Robot Status
- Mission Status
- Battery Percentage
- Position
- Heading

---

## RobotTelemetryType

Enum

PERIODIC

ON_CHANGE

MANUAL

---

## TelemetryMapper

Maps runtime state into RobotTelemetrySnapshot.

No business logic.

---

## RobotTelemetryPublisher

Publishes telemetry snapshots.

Delegates transport to RobotEventPublisher.

---

# Acceptance Criteria

Canonical snapshot implemented.

Mapper implemented.

Publisher implemented.

Runtime capable of producing snapshots.

All tests pass.

---

# AI Coding Assistant Instructions

Implement only the telemetry foundation.

Do not modify MissionManager.

Do not modify NavigationEngine.

Do not change MQTT architecture.