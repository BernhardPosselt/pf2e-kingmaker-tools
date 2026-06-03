# Migration Tasks Checklist

- [x] Implement data models & migrations
  - [x] Implement `RawCharacter.kt` schema flags for companions
  - [x] Update `KingdomData.kt` and `Defaults.kt` properties
  - [x] Create database migration `Migration24.kt` and register in `Migrations.kt`
- [x] Implement Settlements comparison matrix
  - [x] Update `SettlementsContext.kt` to include population, maxItemBonus, influence, and specific item group levels
  - [x] Update `toContext()` calculation logic in `SettlementsContext.kt`
  - [x] ~~Update `KingdomSheet.kt` to pass chosen feats list to `toContext()`~~ — NOT IMPLEMENTED: item levels computed from structures, not feats (see verification report)
  - [x] Update `sections/settlements/page.hbs` to include a Detailed Matrix side-by-side view
- [x] Implement Roster VTT tab & companion syncing
  - [x] Sync companions with native character actor documents
  - [x] Create `roster.hbs` sheet tab
- [x] Implement turn-based simulation engine ticks
- [x] Synchronize custom hex states with Foundry VTT scene drawings
- [x] Run full build & verify compilation

## Code-Complete Status

All migration tasks above are **code-complete** as of 2026-05-31.
Five independent QA verification passes confirmed the implementation (see `docs/migration-verification-report.md`).

## Known Gaps (from QA)

| Gap | Status | Notes |
|-----|--------|-------|
| Only `claimed` hex state synced to drawings | NOT_IMPLEMENTED | `explored`, `cleared`, `roads` absent from `HexState` interface |
| `chosenFeats` not passed to `settlements.toContext()` | NOT_IMPLEMENTED | Item levels come from structures, not feats — likely intentional |
| `write-only` companion sync | DESIGN CHOICE | `setAppFlag` writes companion-data but no read-back path; functionally sufficient |
| Injury duration ticking | REMOVED | Was an unwired engine stub (no `injuries` data model, no UI, no source) carried over from the migration plan. Dropped entirely on 2026-06-02 rather than ship speculative dead code. |

## Tick cadence: monthly vs daily (2026-06-02)

Per Gregory's direction, ticks are split by timescale instead of all firing on the monthly kingdom End Turn:

| Tick | Cadence | Where |
|------|---------|-------|
| Fame, resource points/dice, consumption, commodities, council cooldowns, modifier durations | Monthly (kingdom turn) | `TurnTickingEngine.tick()`, called from the End Turn button in `KingdomSheet.kt`. |
| Weather | Daily | `registerDailyTickHooks` (`DailyTickHooks.kt`) hooks `onUpdateWorldTime`; on each new calendar day it calls the existing `rollWeather(game)` (flat checks, weather events, scene FX, chat), gated by the weather setting and the camping "Check Weather" toggle. |
| Companion travel ETA | Daily | Same hook; `DailyTickEngine.tickTravelEta()` counts each traveling companion's ETA (now measured in **days**) down to arrival, then posts chat + moves the token. |

The daily hook fires whenever the Foundry world clock crosses a day boundary — i.e. when the party rests in the camping sheet, uses Set Time of Day, or the GM advances the clock. The previous monthly weather/travel handling in `TurnTickingEngine` + the End-Turn handler was removed.
