# Sprint 05 — Implementation Report
## Energy Management System

Version: 1.0  
Status: Complete  
Date: 2026-07-26

---

# Summary

Sprint 05 introduces a dedicated Energy Management System. `EnergyManager` is the only component that mutates battery state. Consumption is deterministic, configurable, and applied only after successful movement (position change). `NavigationEngine` and `MissionManager` remain energy-agnostic aside from read-only depletion checks in `RobotEngine`.

Tick flow:

`MissionManager → NavigationEngine → (if moved) EnergyManager.consumeEnergy() → battery events → RobotState`

---

# Design Decisions

## 1. `EnergyManager` as single writer

All HAL `drain` / `charge` calls for runtime energy policy go through `EnergyManager`. `RobotState.battery` is updated from `BatteryModel.toBatteryState()` after consumption/sync.

## 2. Immutable `BatteryModel`

Holds percentage (0–100), charging flag, capacity, `BatteryHealth`, and derived `BatteryStatus` (`FULL` / `NORMAL` / `LOW` / `CRITICAL` / `DEPLETED` / `CHARGING`).

Thresholds:

| Status | Range |
|--------|--------|
| LOW | ≤ 20% and > 10% |
| CRITICAL | ≤ 10% and > 0% |
| DEPLETED | 0% |

## 3. Replaceable `EnergyConsumptionModel`

`FixedStepEnergyConsumptionModel` consumes a fixed percentage per successful movement step (default **1.0**). `MovementEnergyContext` carries speed / payload / terrain for future variable models without changing `EnergyManager`.

## 4. Consume only after successful movement

`RobotEngine` compares position before/after the mission tick. Energy is consumed only when the coordinate changes (forward grid step). Turns, WAIT steps, and idle ticks consume nothing.

## 5. Internal events

Published by `EnergyManager` (once per threshold crossing until recovery):

- `energy.events.BatteryLowEvent`
- `energy.events.BatteryCriticalEvent`
- `energy.events.BatteryDepletedEvent`

Legacy `runtime.events.BatteryLowEvent` remains for compatibility but is unused by the EMS path.

## 6. Charging extension point

`EnergyManager.recharge(amount)` and `BatteryModel.recharge` / `withCharging` exist but are not invoked by the runtime tick. No charging stations or auto-charge logic.

## 7. Legacy `BatteryManager`

Sprint 01 `BatteryManager` (idle + moving drain) remains in the codebase for HAL-oriented unit tests but is no longer wired into `RobotEngine`.

---

# Assumptions

1. One successful movement = one grid-cell translation (position change).  
2. Default cost is 1.0 percentage point per step.  
3. `RobotState` continues to expose `BatteryState` for existing callers; EMS source of truth is `BatteryModel` inside `EnergyManager`.  
4. Mission abort on depletion still happens in `RobotEngine` after EMS updates (not inside `MissionManager`).  
5. Turns do not consume energy in Sprint 05.

---

# Test Results

Command: `mvn test`  
Result: **BUILD SUCCESS** — **136 tests**, 0 failures, 0 errors.

| Suite | Focus |
|-------|--------|
| `BatteryModelTest` | Bounds / status / clamp |
| `FixedStepEnergyConsumptionModelTest` | Deterministic consumption |
| `EnergyManagerTest` | Consume / events / recharge stub |
| `RobotEngineTest` | Movement consumes; idle/WAIT do not; depleted / low |

---

# Files Created

## Energy domain

- `src/main/java/com/vectoros/robot/runtime/energy/BatteryHealth.java`
- `src/main/java/com/vectoros/robot/runtime/energy/BatteryStatus.java`
- `src/main/java/com/vectoros/robot/runtime/energy/BatteryModel.java`
- `src/main/java/com/vectoros/robot/runtime/energy/MovementEnergyContext.java`
- `src/main/java/com/vectoros/robot/runtime/energy/EnergyConsumptionModel.java`
- `src/main/java/com/vectoros/robot/runtime/energy/FixedStepEnergyConsumptionModel.java`
- `src/main/java/com/vectoros/robot/runtime/energy/EnergyManager.java`

## Events

- `src/main/java/com/vectoros/robot/runtime/energy/events/BatteryLowEvent.java`
- `src/main/java/com/vectoros/robot/runtime/energy/events/BatteryCriticalEvent.java`
- `src/main/java/com/vectoros/robot/runtime/energy/events/BatteryDepletedEvent.java`

## Tests / docs

- `src/test/java/com/vectoros/robot/runtime/energy/BatteryModelTest.java`
- `src/test/java/com/vectoros/robot/runtime/energy/FixedStepEnergyConsumptionModelTest.java`
- `src/test/java/com/vectoros/robot/runtime/energy/EnergyManagerTest.java`
- `docs/reviews/SPRINT_05_IMPLEMENTATION_REPORT.md`
- `docs/DEVELOPMENT_ROADMAP.md` (Sprint 5 → Energy Management System)

## Modified

- `RobotEngine.java` — EMS integration; movement-gated consumption
- `RobotEngineTest.java` — energy integration scenarios

---

# Future Charging Extensions

1. Dock / charger mission steps calling `EnergyManager.recharge`.  
2. `BatteryChargedEvent` / `BatteryFullEvent` when leaving charging.  
3. Health-aware charge rates and capacity fade.  
4. Temperature / voltage / current fields on `BatteryModel`.  
5. Variable `EnergyConsumptionModel` using speed, payload, terrain.

---

# Remaining TODOs

| Item | Target |
|------|--------|
| Remove or adapt legacy `BatteryManager` | Cleanup |
| Migrate `RobotState` to hold `BatteryModel` directly | Future |
| Battery-aware mission planning | Future |
| Charging stations / auto-dock | Future |
| MQTT telemetry of energy events | Telemetry sprint |
| Turn / idle baseline drain (optional realism) | Future |

---

# Explicitly Out of Scope

- MQTT / REST / Database / Dashboard  
- Charging stations / automatic charging  
- Fleet communication / multi-robot coordination  
- Mission replanning on low battery  

Sprint 06 was not started.
