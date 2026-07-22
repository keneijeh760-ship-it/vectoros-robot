# Sprint 01 — Implementation Report
## Robot Runtime Foundation

Version: 1.0  
Status: Complete  
Date: 2026-07-22

---

# Summary

Sprint 01 delivers the Robot Runtime Foundation for the VectorOS Warehouse Robot Runtime. The module models a single robot's internal behaviour with a tick-based `RobotEngine`, centrally owned `RobotState`, focused runtime components, internal domain events, and a Hardware Abstraction Layer (HAL) whose current implementation is simulation.

MQTT, REST, persistence, Docker, fleet communication, path planning, and telemetry publishing were intentionally excluded.

---

# Design Decisions

## 1. HAL introduced in Sprint 01 (ADR-007)

`RUNTIME_ARCHITECTURE.md`, `ARCHITECTURE_DECISIONS.md`, and `DEVELOPMENT_ROADMAP.md` were updated so the stack is:

`RobotEngine → runtime components → HAL interfaces → simulation (now) / firmware (later)`.

Roadmap Sprint 8 is reframed as **HAL Firmware Adapters** because the interface boundary itself is required from day one.

## 2. Pure Java Maven module (no Spring in Sprint 01)

The runtime is a Java 21 Maven library with JUnit 5 / AssertJ. Constructor injection is used without Spring so the core stays portable for future on-robot hosts. Spring Boot / MQTT can be layered on in later sprints without rewriting the engine.

## 3. RobotState as single source of truth

`RobotState` holds robot ID, status, battery, position, current task, heading, speed, and last heartbeat. Value objects (`Position`, `BatteryState`, `RobotTask`) and enum `RobotStatus` prevent duplicated concepts. Components mutate state only through controlled mutators; HAL never owns the runtime truth.

## 4. Capability-based HAL interfaces

| Interface | Capability |
|-----------|------------|
| `MovementHardware` | Command move / stop; read speed |
| `BatteryHardware` | Read / drain / charge energy |
| `PositionHardware` | Read position and heading |

Simulation adapters share a package-private `SimulationContext` via `SimulationHardwareFactory`. Future ESP32 / STM32 / Raspberry Pi / ROS2 adapters implement the same three interfaces.

## 5. RobotEngine never touches simulation or MQTT

`RobotEngine` depends on `MotionController`, `BatteryManager`, `PositionTracker`, `TaskExecutor`, and `RuntimeEventPublisher` only. Movement I/O goes through `MotionController → MovementHardware`.

## 6. Deterministic tick loop

Each `tick()`:

1. Updates heartbeat  
2. Executes movement (if tasked)  
3. Syncs pose from HAL  
4. Updates battery  
5. Evaluates task completion  
6. Publishes internal events  

No threading and no networking in Sprint 01.

## 7. Internal events only

`TaskStartedEvent`, `TaskCompletedEvent`, `BatteryLowEvent`, and `PositionChangedEvent` are domain events published through `InMemoryRuntimeEventBus`. No MQTT payloads.

`TaskCompletedEvent` matches the product brief; the implementation spec's `TaskFinishedEvent` name is treated as an alias in documentation only.

## 8. Status vocabulary aligned with Fleet

`RobotStatus` uses `IDLE`, `WORKING`, `CHARGING`, `OFFLINE`, `ERROR` to stay interoperable with Fleet MQTT events later.

---

# Assumptions

1. Sprint 01 tasks are navigate-to-target only (`RobotTask` with a `Position` target). Pick/place semantics come later.  
2. One distance unit per tick at cruise speed `1.0` is an acceptable kinematic simplification until the Movement Engine sprint.  
3. Battery drain rates (`0.01` idle / `0.1` moving per tick) are placeholders for the Battery Simulation sprint.  
4. Arrival tolerance defaults to `0.05` distance units.  
5. `BatteryLowEvent` uses `BatteryState.LOW_THRESHOLD_PERCENT` (`20%`) and is emitted once until charge recovers above the threshold.  
6. Empty battery while working transitions status to `ERROR` and stops motion.  
7. Project root is `vectoros-robot/vectoros-robot` (alongside `docs/`), mirroring Fleet layout.

---

# Test Results

Command: `mvn test`  
Working directory: `vectoros-robot/`  
Result: **BUILD SUCCESS** — **31 tests**, 0 failures, 0 errors.

| Test class | Coverage focus |
|------------|----------------|
| `RobotStateTest` | Models / single source of truth |
| `MotionControllerTest` | Motion via HAL mock |
| `BatteryManagerTest` | Battery drain behaviour |
| `PositionTrackerTest` | Pose reads via HAL stub |
| `TaskExecutorTest` | Arrival / completion |
| `SimulationHardwareTest` | HAL simulation adapters |
| `RobotEngineTest` | Lifecycle, tick, events |

---

# Files Created

## Documentation

- `docs/RUNTIME_ARCHITECTURE.md` (updated — HAL inserted)
- `docs/ARCHITECTURE_DECISIONS.md` (updated — ADR-007)
- `docs/DEVELOPMENT_ROADMAP.md` (updated — Sprint 8 reframed)
- `docs/reviews/SPRINT_01_IMPLEMENTATION_REPORT.md` (this file)

## Build

- `pom.xml`

## Runtime models

- `src/main/java/com/vectoros/robot/runtime/model/RobotStatus.java`
- `src/main/java/com/vectoros/robot/runtime/model/Position.java`
- `src/main/java/com/vectoros/robot/runtime/model/BatteryState.java`
- `src/main/java/com/vectoros/robot/runtime/model/RobotTask.java`
- `src/main/java/com/vectoros/robot/runtime/model/RobotState.java`

## HAL

- `src/main/java/com/vectoros/robot/runtime/hal/MovementHardware.java`
- `src/main/java/com/vectoros/robot/runtime/hal/BatteryHardware.java`
- `src/main/java/com/vectoros/robot/runtime/hal/PositionHardware.java`
- `src/main/java/com/vectoros/robot/runtime/hal/simulation/SimulationContext.java`
- `src/main/java/com/vectoros/robot/runtime/hal/simulation/SimulatedMovementHardware.java`
- `src/main/java/com/vectoros/robot/runtime/hal/simulation/SimulatedBatteryHardware.java`
- `src/main/java/com/vectoros/robot/runtime/hal/simulation/SimulatedPositionHardware.java`
- `src/main/java/com/vectoros/robot/runtime/hal/simulation/SimulationHardwareFactory.java`

## Runtime components

- `src/main/java/com/vectoros/robot/runtime/motion/MotionController.java`
- `src/main/java/com/vectoros/robot/runtime/battery/BatteryManager.java`
- `src/main/java/com/vectoros/robot/runtime/position/PositionTracker.java`
- `src/main/java/com/vectoros/robot/runtime/task/TaskExecutor.java`

## Engine & events

- `src/main/java/com/vectoros/robot/runtime/engine/RobotEngine.java`
- `src/main/java/com/vectoros/robot/runtime/events/RuntimeEvent.java`
- `src/main/java/com/vectoros/robot/runtime/events/RuntimeEventListener.java`
- `src/main/java/com/vectoros/robot/runtime/events/RuntimeEventPublisher.java`
- `src/main/java/com/vectoros/robot/runtime/events/InMemoryRuntimeEventBus.java`
- `src/main/java/com/vectoros/robot/runtime/events/TaskStartedEvent.java`
- `src/main/java/com/vectoros/robot/runtime/events/TaskCompletedEvent.java`
- `src/main/java/com/vectoros/robot/runtime/events/BatteryLowEvent.java`
- `src/main/java/com/vectoros/robot/runtime/events/PositionChangedEvent.java`

## Tests

- `src/test/java/com/vectoros/robot/runtime/model/RobotStateTest.java`
- `src/test/java/com/vectoros/robot/runtime/motion/MotionControllerTest.java`
- `src/test/java/com/vectoros/robot/runtime/battery/BatteryManagerTest.java`
- `src/test/java/com/vectoros/robot/runtime/position/PositionTrackerTest.java`
- `src/test/java/com/vectoros/robot/runtime/task/TaskExecutorTest.java`
- `src/test/java/com/vectoros/robot/runtime/hal/simulation/SimulationHardwareTest.java`
- `src/test/java/com/vectoros/robot/runtime/engine/RobotEngineTest.java`

---

# Remaining TODOs

| Item | Target |
|------|--------|
| Richer movement model (acceleration, turning radius, time-based ticks) | Sprint 2 — Movement Engine |
| Warehouse map / occupancy | Sprint 3 |
| Realistic battery curves and charging behaviour | Sprint 4 |
| MQTT telemetry / task progress publishing | Sprint 5 |
| Full task lifecycle beyond navigate-to-target | Sprint 6 |
| Fault injection | Sprint 7 |
| Firmware HAL adapters (ESP32 / STM32 / Pi / ROS2) | Sprint 8+ |
| Communication layer (MQTT subscribe/publish) | Future sprint |
| Spring Boot host / packaging | Future sprint |

---

# Suggested Improvements

1. **Inject `Clock` via `RobotEngine.create`** — factory currently uses `Clock.systemUTC()`; expose an overload for fully deterministic integration tests.  
2. **Immutable snapshots** — add `RobotState.snapshot()` for telemetry without exposing mutators to callers outside the engine package.  
3. **Sealed `RuntimeEvent` hierarchy** — tighten compile-time exhaustiveness for event handlers.  
4. **Configurable tick duration** — separate “distance per second” from “per tick” once wall-clock timing is introduced.  
5. **Obstacle-aware motion** — keep out of `MotionController` until map/HAL sensor interfaces exist.

---

# Explicitly Out of Scope (Not Implemented)

- MQTT / fleet communication  
- REST controllers  
- Database / Spring Data  
- Docker changes  
- Scheduling  
- Telemetry publishing  
- Path planning / obstacle avoidance  

Sprint 02 was not started.
