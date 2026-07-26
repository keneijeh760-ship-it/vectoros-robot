# Sprint 04 — Implementation Report
## Mission Foundation

Version: 1.0  
Status: Complete  
Date: 2026-07-26

---

# Summary

Sprint 04 introduces the Mission Layer. `MissionManager` is the highest-level runtime coordinator. `RobotEngine` delegates mission execution to it; `MissionManager` delegates movement to the unchanged `NavigationEngine`.

Stack:

`RobotEngine → MissionManager → Mission / MissionStep → NavigationEngine → MotionController → HAL`

---

# Design Decisions

## 1. Mission as ordered immutable steps + controlled lifecycle

`Mission` freezes identity and step order at construction (`List.copyOf`). Status / index / completion time mutate only through package-visible lifecycle methods used by `MissionManager`.

## 2. Implemented vs reserved step types

Implemented: `NAVIGATE`, `WAIT`.  
Reserved in `MissionStepType` for later: `PICKUP`, `DROPOFF`, `DOCK`, `CHARGE`. Using a reserved type fails the mission with a clear reason.

## 3. Single active mission

`assignMission` throws `IllegalMissionStateException` if another mission is `QUEUED`/`RUNNING`.

## 4. Navigation remains a service

`NavigationEngine` API is unchanged. `MissionManager` calls `startNavigation` / `tick` / `cancelNavigation` only.

## 5. RobotEngine integration

- `assignMission(Mission)` — primary API  
- `assignTask(RobotTask)` — compatibility wrapper (single `NAVIGATE` step)  
- `tick()` → `missionManager.tick()` then applies completion / failure to `RobotStateMachine`  
- `cancelMission()` cancels mission + restores `IDLE`

## 6. Sequential progression in-tick

When a step completes and more steps remain, `MissionManager` continues into the next step in the same tick so WAIT→NAVIGATE chains stay responsive without skipping.

## 7. Bounds validation at assign time

`WarehouseWorld.requireContains` runs for every `NAVIGATE` target during `assignMission`, preserving early rejection of out-of-bounds destinations.

---

# Assumptions

1. One robot / one `MissionManager` / one active mission.  
2. `WAIT` duration is measured in runtime ticks, not wall-clock time.  
3. `RobotTask` remains a thin compatibility projection of the active mission for existing state fields/events.  
4. Mission failure maps to `FAULT_DETECTED` on the robot state machine.  
5. Cancellation mid-mission recovers to `IDLE` via fault→clear (legal transition path).

---

# Test Results

Command: `mvn test`  
Result: **BUILD SUCCESS** — **120 tests**, 0 failures, 0 errors.

Covered:

| Area | Coverage |
|------|----------|
| Mission lifecycle | `MissionTest` |
| MissionManager progression / cancel / fail / single-active | `MissionManagerTest` |
| Navigation integration / multi-step / cancel / assignTask adapter | `RobotEngineTest` |
| Prior sprints | Still green |

---

# Files Created

## Mission model / manager

- `src/main/java/com/vectoros/robot/runtime/mission/Mission.java`
- `src/main/java/com/vectoros/robot/runtime/mission/MissionStep.java`
- `src/main/java/com/vectoros/robot/runtime/mission/MissionStatus.java`
- `src/main/java/com/vectoros/robot/runtime/mission/MissionStepType.java`
- `src/main/java/com/vectoros/robot/runtime/mission/MissionResult.java`
- `src/main/java/com/vectoros/robot/runtime/mission/MissionManager.java`
- `src/main/java/com/vectoros/robot/runtime/mission/IllegalMissionStateException.java`

## Events

- `src/main/java/com/vectoros/robot/runtime/mission/events/MissionStartedEvent.java`
- `src/main/java/com/vectoros/robot/runtime/mission/events/MissionStepStartedEvent.java`
- `src/main/java/com/vectoros/robot/runtime/mission/events/MissionStepCompletedEvent.java`
- `src/main/java/com/vectoros/robot/runtime/mission/events/MissionCompletedEvent.java`
- `src/main/java/com/vectoros/robot/runtime/mission/events/MissionCancelledEvent.java`
- `src/main/java/com/vectoros/robot/runtime/mission/events/MissionFailedEvent.java`

## Tests / docs

- `src/test/java/com/vectoros/robot/runtime/mission/MissionTest.java`
- `src/test/java/com/vectoros/robot/runtime/mission/MissionManagerTest.java`
- `docs/reviews/SPRINT_04_IMPLEMENTATION_REPORT.md`
- `docs/DEVELOPMENT_ROADMAP.md` (Sprint 4 → Mission Foundation)

## Modified

- `RobotEngine.java` — mission delegation
- `RobotEngineTest.java` — mission scenarios

---

# Future Extension Points

1. Implement `PICKUP` / `DROPOFF` / `DOCK` / `CHARGE` step executors.  
2. Mission queue (`QUEUED` beyond single-slot).  
3. Map mission steps onto finer `RobotStateMachine` transitions (loading/unloading) instead of `advanceMissionToIdle()`.  
4. MQTT publish of mission events (telemetry sprint).  
5. Persist mission history.

---

# Remaining TODOs

| Item | Target |
|------|--------|
| Battery-aware mission abort policies | Battery sprint |
| Charging / dock missions | Future |
| Behaviour-tree / richer planning | Out of scope by design |
| Replace `RobotTask` with mission-only state | Cleanup |
| Telemetry of mission progress | Sprint 05+ |

---

# Explicitly Out of Scope

- MQTT / REST / Database  
- Battery system / charging logic  
- Behaviour Trees  
- Multi-robot coordination  
- Obstacle avoidance  

Sprint 05 was not started.
