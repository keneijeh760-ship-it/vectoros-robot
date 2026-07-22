# Sprint 03 — Implementation Report
## Navigation Foundation

Version: 1.0  
Status: Complete  
Date: 2026-07-22

---

# Summary

Sprint 03 delivers deterministic grid navigation for the WarehouseOS Robot Runtime. Navigation is a reusable subsystem (`NavigationEngine`) that can be driven by `RobotEngine` today and by a future `MissionManager` later.

Stack:

`RobotEngine → RobotStateMachine → NavigationEngine → MovementPlanner → MovementCommand → MotionController → HAL → Simulation`

Architecture layers were extended, not rewritten. HAL interfaces remain unchanged.

---

# Design Decisions

## 1. Immutable `MovementCommand` (not an enum)

`MovementType` is the enum (`MOVE_FORWARD`, `TURN_LEFT`, `TURN_RIGHT`, `STOP`).  
`MovementCommand` is an immutable intent object with `movementType`, optional `targetCoordinate`, optional `targetHeading`, and `speed`. Future fields (acceleration, tolerance, timeout, reverse, docking) can be added without changing callers' control flow.

## 2. Replaceable `MovementPlanner`

`MovementPlanner` is an interface. `AxisAlignedMovementPlanner` implements X-then-Y deterministic steps with in-place turns. A* / Dijkstra can replace only this component.

## 3. `NavigationEngine` orchestration API

Public API suitable for MissionManager:

- `startNavigation(Coordinate)`
- `cancelNavigation()`
- `isNavigating()`
- `destination()`
- `tick() → NavigationResult`

It never accesses HAL, never assumes it is the top-level coordinator, and does not modify status (that remains `RobotStateMachine`).

## 4. `MotionController` executes commands only

`execute(MovementCommand)` translates intents to HAL `move` / `stop`. Legacy `moveToward` remains for compatibility but is unused by the runtime tick path.

## 5. Heading synced with orientation

`Heading` (`NORTH`/`EAST`/`SOUTH`/`WEST`) lives on `RobotState`. Degrees follow HAL convention (`0°=+X/EAST`, `90°=+Y/NORTH`). Updating heading from degrees or from the enum keeps both representations consistent.

## 6. Minimal world surface

Sprint 02 World Model sources were not present in the repository. Sprint 03 introduces the minimal types navigation requires:

- `Coordinate` — integer grid cell
- `WarehouseWorld` — rectangular bounds

These are the world-model dependency surface for bounds validation. A fuller World Model can expand this package later without changing navigation contracts.

## 7. One cell per tick

Forward steps use speed `1.0` (one grid unit). Turns update heading with zero translation speed. No teleportation.

## 8. Runtime integration

`assignTask` starts navigation to the task target. Each motion-status tick calls `navigationEngine.tick()`. On `DESTINATION_REACHED`, the engine completes the task via the existing state machine happy-path helper. On `FAILED`, it raises `FAULT_DETECTED`.

---

# Assumptions

1. Warehouse cells are integer coordinates in `[0, width) × [0, height)`.  
2. Default simulated world is `50×50` via `RobotEngine.create`.  
3. Continuous `Position` maps to grid via rounding.  
4. Battery drain behaviour is unchanged from Sprint 01 (not redesigned here).  
5. Mission substates (`MOVING_TO_PICKUP`, etc.) still advance via Sprint 01.5 helpers after arrival; MissionManager will own that later.  
6. Diagonal motion is unsupported; planner is strictly axis-aligned.

---

# Test Results

Command: `mvn test`  
Result: **BUILD SUCCESS** — **104 tests**, 0 failures, 0 errors.

Covered areas:

| Area | Tests |
|------|--------|
| Heading / MovementType / MovementCommand | yes |
| AxisAlignedMovementPlanner | yes |
| NavigationEngine (reach / turn / fail / cancel) | yes |
| MotionController.execute | yes |
| Warehouse bounds | yes |
| RobotEngine navigation integration | yes |

---

# Files Created

## World

- `src/main/java/com/vectoros/robot/runtime/world/Coordinate.java`
- `src/main/java/com/vectoros/robot/runtime/world/WarehouseWorld.java`

## Navigation

- `src/main/java/com/vectoros/robot/runtime/navigation/Heading.java`
- `src/main/java/com/vectoros/robot/runtime/navigation/MovementType.java`
- `src/main/java/com/vectoros/robot/runtime/navigation/MovementCommand.java`
- `src/main/java/com/vectoros/robot/runtime/navigation/NavigationResult.java`
- `src/main/java/com/vectoros/robot/runtime/navigation/NavigationEngine.java`
- `src/main/java/com/vectoros/robot/runtime/navigation/planner/MovementPlanner.java`
- `src/main/java/com/vectoros/robot/runtime/navigation/planner/AxisAlignedMovementPlanner.java`
- `src/main/java/com/vectoros/robot/runtime/navigation/events/NavigationStartedEvent.java`
- `src/main/java/com/vectoros/robot/runtime/navigation/events/MovementStepCompletedEvent.java`
- `src/main/java/com/vectoros/robot/runtime/navigation/events/DestinationReachedEvent.java`
- `src/main/java/com/vectoros/robot/runtime/navigation/events/NavigationFailedEvent.java`

## Tests

- `src/test/java/com/vectoros/robot/runtime/navigation/HeadingTest.java`
- `src/test/java/com/vectoros/robot/runtime/navigation/MovementCommandTest.java`
- `src/test/java/com/vectoros/robot/runtime/navigation/NavigationEngineTest.java`
- `src/test/java/com/vectoros/robot/runtime/navigation/planner/AxisAlignedMovementPlannerTest.java`
- `src/test/java/com/vectoros/robot/runtime/world/WarehouseWorldTest.java`

## Docs

- `docs/reviews/SPRINT_03_IMPLEMENTATION_REPORT.md`
- `docs/sprints/SPRINT_03_NAVIGATION_FOUNDATION.md`
- `docs/implementation/IMPLEMENT_NAVIGATION_FOUNDATION.md`
- `docs/DEVELOPMENT_ROADMAP.md` (updated)

## Modified

- `MotionController.java` — `execute(MovementCommand)`
- `RobotState.java` — `Heading` field + synced updates
- `RobotEngine.java` — navigation integration
- Existing engine / motion / model tests updated

---

# Future Extension Points

1. **MissionManager** calls `NavigationEngine.startNavigation` / `tick` for pickup and dropoff legs.  
2. **A\* MovementPlanner** drop-in replacement.  
3. **Obstacle / occupancy** checks inside planner using richer world model.  
4. **MovementCommand** optional fields: acceleration, tolerance, timeout, reverse, docking.  
5. **HAL firmware adapters** execute the same low-level `move`/`stop` without touching navigation.

---

# Remaining TODOs

| Item | Target |
|------|--------|
| Full World Model (zones, racks, chargers, occupancy) | Expand Sprint 02 surface |
| MissionManager orchestration | Future sprint |
| Obstacle avoidance | Future sprint |
| Battery-aware navigation / charging | Sprint 04+ |
| MQTT telemetry of navigation events | Telemetry sprint |
| Remove legacy `MotionController.moveToward` once unused externally | Cleanup |

---

# Explicitly Out of Scope (Not Implemented)

- MissionManager  
- MQTT / REST / Database / Spring Data / Docker  
- Telemetry publishing  
- Battery consumption redesign / charging behaviour  
- Obstacle avoidance, A*, Dijkstra  
- Multi-robot coordination / fleet communication  

Sprint 04 was not started.
