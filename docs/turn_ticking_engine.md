# Turn-Triggered Ticking Engine

## Overview

The `TurnTickingEngine` is the central mechanism for processing all time-based state
transitions in the Pathfinder 2e Kingdom module. It runs automatically at the end of
each kingdom turn (triggered by the "end-turn" chat button in Foundry VTT) and advances
all duration-based game state by one tick.

**Design principle:** No background real-time timers. All ticks are processed
deterministically on turn change.

## Architecture

```
KingdomSheet (end-turn chat button)
    |
    v
TurnTickingEngine.tick(...)
    |
    +-- Reset solution counters (supernaturalSolutions, creativeSolutions -> 0)
    +-- Advance fame (next -> now, next -> 0)
    +-- Advance resource points (next -> now)
    +-- Advance resource dice (next -> now)
    +-- Advance consumption (next -> now, armies preserved)
    +-- Merge commodities (now + next, capped by storage, next -> 0)
    +-- Tick down council cooldowns (audit, scrying, lockdown, feast)
    +-- Tick down modifier durations (decrement turns, expire at 0)
    |
    v
TickResult (new state + change log)
```

## Source Location

- **Engine:** `src/jsMain/kotlin/.../kingdom/TurnTickingEngine.kt`
- **Tests:** `src/jsTest/kotlin/.../kingdom/TurnTickingEngineTest.kt`
- **Integration:** `src/jsMain/kotlin/.../kingdom/sheet/KingdomSheet.kt` (line ~1176)

## `TurnTickingEngine.tick()` Parameters

| Parameter            | Type                      | Description                                      |
|----------------------|---------------------------|--------------------------------------------------|
| `fame`               | `RawFame`                 | Current fame state (now/next/type)               |
| `resourcePoints`     | `RawResources`            | Current resource points (now/next)               |
| `resourceDice`       | `RawResources`            | Current resource dice (now/next)                 |
| `consumption`        | `RawConsumption`          | Current consumption (now/next/armies)            |
| `commodities`        | `RawCurrentCommodities`   | Current commodities (now/next per type)          |
| `storage`            | `CommodityStorage`        | Storage capacity (caps commodity merge)          |
| `councilCooldowns`   | `RawCouncilCooldowns?`    | Council mission cooldowns (nullable)             |
| `modifiers`          | `Array<RawModifier>`      | Active modifiers (may carry turn durations)      |

## `TickResult` Output

| Field                   | Type                    | Description                                    |
|-------------------------|-------------------------|------------------------------------------------|
| `supernaturalSolutions` | `Int`                   | Always 0 (reset each tick)                     |
| `creativeSolutions`     | `Int`                   | Always 0 (reset each tick)                     |
| `fame`                  | `RawFame`               | Post-tick fame state                           |
| `resourcePoints`        | `RawResources`          | Post-tick resource points                      |
| `resourceDice`          | `RawResources`          | Post-tick resource dice                        |
| `consumption`           | `RawConsumption`       | Post-tick consumption                          |
| `commodities`           | `RawCurrentCommodities` | Post-tick commodities (merged + capped)        |
| `councilCooldowns`      | `RawCouncilCooldowns?`  | Post-tick cooldowns (ticked down, min 0)       |
| `modifiers`             | `Array<RawModifier>`    | Surviving modifiers (expired removed)          |
| `changes`               | `List<TickChange>`      | Diff log for auditing / chat messages          |

## `TickChange` — Change Tracking

Each `TickChange` records a single field transition:

```kotlin
data class TickChange(
    val category: String,    // e.g. "fame", "resourcePoints", "modifiers"
    val field: String,       // e.g. "now", "audit", "expired"
    val oldValue: Any?,      // previous value
    val newValue: Any?,      // new value
)
```

Changes are only emitted when values actually differ, keeping the log sparse.

## Integration Points

### 1. KingdomSheet End-Turn Handler

`KingdomSheet.kt` handles the `"end-turn"` chat button:

```kotlin
"end-turn" -> buildPromise {
    actor.getKingdom()?.let { kingdom ->
        val storage = calculateStorage(realm, settlements)
        val tickResult = TurnTickingEngine.tick(
            fame = kingdom.fame,
            resourcePoints = kingdom.resourcePoints,
            resourceDice = kingdom.resourceDice,
            consumption = kingdom.consumption,
            commodities = kingdom.commodities,
            storage = storage,
            councilCooldowns = kingdom.councilCooldowns,
            modifiers = kingdom.modifiers,
        )
        // Apply result back
        kingdom.fame = tickResult.fame
        kingdom.resourcePoints = tickResult.resourcePoints
        // ... etc
        actor.setKingdom(kingdom)
    }
    postChatTemplate("chatmessages/end-turn.hbs")
}
```

### 2. Modifier Expiration

The engine decrements `turns` on each `RawModifier`. When `turns` reaches 1, the
modifier is removed on the next tick. Use `turns = null` or `turns = 0` for permanent
modifiers that never expire.

### 3. Commodity Storage Capping

Commodities are merged (`now + next`) then capped per-type by the calculated storage
limit. This prevents hoarding beyond warehouse capacity.

## Test Coverage

`TurnTickingEngineTest` covers:

- **Solution counter reset** — always resets to 0
- **Fame advancement** — next -> now, type preserved, zero cases
- **Resource points advancement** — next -> now, zero cases
- **Resource dice advancement** — next -> now
- **Consumption advancement** — next -> now, armies preserved
- **Commodity merging** — sum + cap, next reset
- **Storage capping** — per-type limits, zero storage, overflow
- **Council cooldowns** — tick down, floor at 0, null handling
- **Modifier duration** — permanent (null/0) preserved, decrement, expiration
- **Mixed modifiers** — permanent + expiring + temporary in one tick
- **Full tick** — all fields processed simultaneously
- **Sequential ticks** — feeding result of tick N into tick N+1
- **Interrupted sequences** — save/restore state across ticks (Foundry reload)
- **Large arrays** — 100 modifiers with varying durations
- **High cooldowns** — 200+ cooldown values tick down correctly
- **Commodity overflow** — tiny storage with massive commodity counts
- **Change tracking** — all mutations logged with old/new values

## Adding New Tickable State

To add a new field to the ticking engine:

1. Add the field to `TickResult`
2. Add a parameter to `tick()`
3. Add processing logic in the `tick()` function
4. Add change tracking with `TickChange`
5. Update `KingdomSheet.kt` to pass state in and apply the result back
6. Add tests for advancement, edge cases, and change tracking
