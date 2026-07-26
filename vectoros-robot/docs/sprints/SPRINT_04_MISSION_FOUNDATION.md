# Sprint 04 – Mission Foundation

Version: 1.0

Status: Ready for Implementation

Estimated Duration: 1 Day

---

# Sprint Goal

Introduce the Mission Layer into the Robot Runtime.

The Mission Layer represents the highest level of robot behaviour.

A mission consists of one or more ordered objectives that the robot executes.

The Mission Manager coordinates the execution of those objectives by delegating navigation work to the Navigation Engine.

---

# Business Context

Fleet assigns missions.

Robot Runtime executes missions.

Navigation determines how to move.

Mission determines what to do.

---

# Responsibilities

Mission creation.

Mission execution.

Mission lifecycle.

Mission progress.

Mission cancellation.

Mission completion.

Mission state tracking.

---

# Out of Scope

Do NOT implement

MQTT

Battery

Charging

Obstacle avoidance

Multi-robot coordination

Persistence

REST

Scheduling

Behaviour Trees

---

# Architecture

RobotEngine

↓

MissionManager

↓

Mission

↓

MissionStep

↓

NavigationEngine

↓

MotionController

↓

HAL

---

# Components

## MissionManager

The only component responsible for managing mission execution.

Responsibilities

- Accept missions
- Start missions
- Advance through mission steps
- Cancel missions
- Complete missions
- Notify RobotEngine of mission progress

MissionManager must not perform navigation.

MissionManager delegates movement to NavigationEngine.

---

## Mission

Represents a complete robot objective.

Contains

- Mission ID
- Mission Status
- Ordered Mission Steps
- Creation Time
- Completion Time (future)

Mission should be immutable where practical.

---

## MissionStep

Represents one unit of work.

Initial step types:

- NAVIGATE
- WAIT

Future step types:

- PICKUP
- DROPOFF
- DOCK
- CHARGE
- INSPECT

---

## MissionStatus

Enum

CREATED

QUEUED

RUNNING

COMPLETED

FAILED

CANCELLED

---

## MissionStepType

Enum

NAVIGATE

WAIT

---

# Runtime Behaviour

RobotEngine

↓

MissionManager.tick()

↓

Current Mission Step

↓

NavigationEngine

↓

Navigation Complete?

↓

Advance Mission

↓

Mission Finished?

↓

Mission Complete

---

# Validation

Only one active mission.

Mission steps execute sequentially.

Mission cannot skip steps.

Mission cancellation leaves runtime consistent.

Mission completion updates RobotState.

---

# Acceptance Criteria

MissionManager implemented.

Mission model implemented.

Mission lifecycle implemented.

Sequential execution supported.

Navigation integration complete.

All tests pass.

---

# AI Coding Assistant Instructions

Implement only Sprint 04.

Do not implement battery.

Do not implement MQTT.

Do not implement Behaviour Trees.

Keep MissionManager independent from NavigationEngine implementation.

Design for future mission expansion.