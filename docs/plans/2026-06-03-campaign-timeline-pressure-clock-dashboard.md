# Campaign Timeline & Pressure-Clock Dashboard Implementation Plan

> **Status:** Draft — pending Gregory review
> **Date:** 2026-06-03
> **Feature:** feature-roadmap.md #1 — Campaign timeline and pressure-clock dashboard

---

## Executive Summary

This plan describes a **campaign timeline and pressure-clock dashboard** for the pf2e-kingmaker-tools Foundry module. The system lets the GM define named deadline clocks (e.g., "Stag Lord deadline," "Varnhold Vanishing rescue timer") that tick down automatically each kingdom turn. Clocks display on a new dashboard panel on the kingdom sheet, fire chat/journal output when they advance or expire, and optionally enforce consequences on expiry. The architecture follows Decision 3 (hybrid strict automation with per-clock soft-pause) from the roadmap design-decisions doc.

**Key capabilities:**

- **Campaign clock data model** stored in `KingdomData` with fields for label, turns remaining, max turns, expiry action description, and pause-on-expiry toggle
- **New `CampaignClockManager` pure engine** (commonMain) that ticks all active clocks, produces advance/expiry events, and is called from `TurnTickingEngine.tick()`
- **TurnTickingEngine integration** — clocks tick as step 9 of the existing engine (after modifier expiry), producing `ClockTickChange` records alongside the existing `TickChange` list
- **Kingdom sheet dashboard panel** — new "Campaign" tab in `turnSectionNav` showing clock progress bars, remaining turns, and expiry warnings (GM-only for editing, player-visible for viewing)
- **Chat/journal output** on clock advance, expiry, and triggered consequences using a new `clock-tick.hbs` chat template
- **Clock management dialog** (`CampaignClockDialog`, `CrudApplication` subclass) for GM to add, edit, pause, resume, and delete clocks
- **Soft-pause per clock** (Decision 3) — clocks with "pause on expiry" stop at 0 and wait for GM confirmation before firing consequences
- **Migration29** — adds new fields with safe defaults, idempotent and re-runnable

**Tech Stack:** Kotlin Multiplatform jsMain/jsTest/commonMain, Foundry `CrudApplication` dialogs, Handlebars templates, `kotlin.test`, existing `TurnTickingEngine` + kingdom sheet architecture.

---

## Source of Truth

- Roadmap item #1: `docs/feature-roadmap.md` lines 37-59
- Design decisions (card f0): `docs/plans/2026-06-01-roadmap-design-decisions.md` — Decision 3 (hybrid strict+soft-pause clocks)
- Existing turn engine: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/TurnTickingEngine.kt`
- Existing kingdom data: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/KingdomData.kt`
- Existing kingdom sheet: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/KingdomSheet.kt`
- Existing turn page template: `src/jsMain/resources/applications/kingdom/sections/turn/page.hbs`
- Existing end-turn chat: `src/jsMain/resources/chatmessages/end-turn.hbs`
- Existing turn section nav: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/navigation/TurnNavEntry.kt`
- Existing turn nav entry: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/navigation/MainNavEntry.kt`
- Existing context: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/contexts/KingdomSheetContext.kt`
- Existing defaults: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/Defaults.kt`
- Existing migration pattern: `src/jsMain/kotlin/at/posselt/pfrpg2e/migrations/migrations/Migration26.kt`
- Existing migration registry: `src/jsMain/kotlin/at/posselt/pfrpg2e/migrations/Migrations.kt`
- Existing module init: `src/jsMain/kotlin/com/foundryvtt/kingmaker/KingmakerModule.kt`
- Existing localization: `src/jsMain/resources/lang/en.json`
- Existing test patterns: `src/jsTest/kotlin/at/posselt/pfrpg2e/kingdom/TurnTickingEngineTest.kt`

## Non-Goals

- Do NOT implement quest/event generation from clocks (roadmap item #2, separate feature). The expiry action is a human-readable description string; consequence auto-application is limited to unrest increase (toggleable) and chat output.
- Do NOT implement hex content management (roadmap item #3, separate feature).
- Do NOT implement a session prep dashboard (roadmap item #10, separate feature). Clock data is available for future session prep aggregation, but the dashboard is read-only for now.
- Do NOT implement homebrew profile import/export (roadmap item #9, separate feature). Clock behaviour does not vary by profile.
- Do NOT implement campaign timeline visualization (horizontal scrollable timeline). The MVP is a simple list/dashboard panel with progress bars; a visual timeline can be additive later.
- Do NOT add pacing/balance alert thresholds (roadmap item #13, separate feature).
- Do NOT modify `DailyTickEngine`. Clock granularity is kingdom-turn (monthly), not daily.

---

## Affected Files

### New files to create

| File | Purpose |
|------|---------|
| `src/commonMain/kotlin/at/posselt/pfrpg2e/campaign/CampaignClock.kt` | Data class for a single clock (id, label, turnsRemaining, maxTurns, description, pauseOnExpiry, expired, expiryConsequenceUnrest, expiryMessage) |
| `src/commonMain/kotlin/at/posselt/pfrpg2e/campaign/ClockTickResult.kt` | Result of ticking all clocks: `List<ClockTickEvent>` (advance, expiry, triggered) |
| `src/commonMain/kotlin/at/posselt/pfrpg2e/campaign/CampaignClockManager.kt` | Pure engine: `tickAll(clocks, kingdomData) → ClockTickResult`. Contains `tickClock()`, `advanceClock()`, `expireClock()`, `triggerClock()`. No Foundry deps. |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/campaign/CampaignClockDialog.kt` | `CrudApplication` subclass for clock CRUD: add, edit, pause/resume, delete, set pause-on-expiry |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/contexts/CampaignClockContext.kt` | JS context interface for the dashboard panel template (clock progress bars, colours, remaining turns, isGM flag) |
| `src/jsMain/resources/applications/kingdom/sections/clocks/page.hbs` | Clock dashboard panel Handlebars template: progress bars per clock, remaining turn badges, expiry warnings, GM edit buttons |
| `src/jsMain/resources/applications/kingdom/campaign-clock-dialog.hbs` | Clock management dialog template: form fields, pause-on-expiry toggle, list of existing clocks |
| `src/jsMain/resources/applications/kingdom/clocks.css` | New CSS for clock progress bars (green→yellow→red gradient by urgency), expiry flash animation, dialog form |
| `src/jsMain/resources/chatmessages/clock-tick.hbs` | Chat message template for clock advance/expiry/trigger events |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/migrations/migrations/Migration29.kt` | Migration29 — adds `campaignClocks` array to `CampaignData`, seeds empty array, idempotent |
| `src/commonTest/kotlin/at/posselt/pfrpg2e/campaign/CampaignClockTest.kt` | Unit tests for CampaignClock data model defaults and field validation (Test T1) |
| `src/commonTest/kotlin/at/posselt/pfrpg2e/campaign/CampaignClockManagerTest.kt` | Unit tests for pure-function clock ticking engine (Test T2) — tick advance, expiry, soft-pause, trigger, unrest consequence. ~30 tests. |
| `src/jsTest/kotlin/at/posselt/pfrpg2e/campaign/CampaignClockIntegrationTest.kt` | Integration test: clocks tick via TurnTickingEngine → ClockTickResult → chat output flow (Test I1) |
| `src/jsTest/kotlin/at/posselt/pfrpg2e/campaign/CampaignClockDialogTest.kt` | Unit tests for dialog defaults, form validation, CRUD operations (Test I2) |
| `src/jsTest/kotlin/at/posselt/pfrpg2e/campaign/CampaignClockContextTest.kt` | Unit tests for dashboard context computation (progress percentages, urgency colours, isGM gating) (Test I3) |

### Existing files to modify

| File | Change |
|------|--------|
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/TurnTickingEngine.kt` | Add `campaignClocks` parameter to `tick()`. Add step 9: call `CampaignClockManager.tickAll()`, merge resulting `ClockTickEvent` records into `TickResult` as a new `clockEvents` field. Expired clocks with unrest consequence increment `unrest`. |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/KingdomData.kt` | Add `campaignClocks: Array<CampaignClock>?` field |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/Defaults.kt` | Add `campaignClocks = emptyArray()` to `createKingdomDefaults()` |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/KingdomSheet.kt` | Wire clock dashboard context into the turn section. Add `data-action="open-clock-dialog"` handler (GM-only). Read clock state for dashboard panel context. |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/navigation/TurnNavEntry.kt` | Add `CLOCK` enum value between `EVENT` and `XP` |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/contexts/KingdomSheetContext.kt` | Add `campaignClocks: Array<CampaignClockContext>` field |
| `src/jsMain/resources/applications/kingdom/sections/turn/page.hbs` | Add clock dashboard section partial include after the event section. Add "Open Clock Manager" button (GM-only) in the End Turn section header area. |
| `src/jsMain/resources/applications/kingdom/sections/turn/page.hbs` (activities partial area) | Include clock summary badges in the End Turn section header: "3 active clocks · 1 expiring next turn" |
| `src/jsMain/resources/chatmessages/end-turn.hbs` | Add conditional `{{#if clockEvents}}` block iterating clock tick events (advance, expiry, trigger) alongside existing turn changes |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/BeforeKingdomUpdate.kt` | Add clock expiry validation: prevent reducing `turnsRemaining` below 0 via manual edit; coerce to 0 and mark `expired = true` |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/migrations/Migrations.kt` | Register `Migration29()` in the migrations list |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/contexts/OngoingEventContext.kt` (context data flow) | No direct change — clocks are independent of ongoing events, but the clock panel sits adjacent to the events section in the UI tab order. |
| `src/jsMain/resources/lang/en.json` | Add ~20 localization keys (see section below) |

### Localization keys to add

All in `src/jsMain/resources/lang/en.json`:

- `kingdom.clocks.title` — "Campaign Clocks"
- `kingdom.clocks.noClocks` — "No campaign clocks active"
- `kingdom.clocks.remaining` — "{remaining} / {max} turns"
- `kingdom.clocks.expiring` — "Expiring next turn!"
- `kingdom.clocks.expired` — "Expired"
- `kingdom.clocks.paused` — "Paused at expiry"
- `kingdom.clocks.manage` — "Manage Clocks"
- `kingdom.clocks.add` — "Add Clock"
- `kingdom.clocks.edit` — "Edit"
- `kingdom.clocks.delete` — "Delete"
- `kingdom.clocks.pauseOnExpiry` — "Pause on expiry (soft-pause)"
- `kingdom.clocks.turnsRemaining` — "Turns remaining"
- `kingdom.clocks.maxTurns` — "Total turns"
- `kingdom.clocks.expiryMessage` — "On expiry"
- `kingdom.clocks.expiryConsequenceUnrest` — "Unrest on expiry"
- `kingdom.chat.clockAdvanced` — "{label}: {old} → {remaining} turns remaining"
- `kingdom.chat.clockExpired` — "{label}: EXPIRED — {message}"
- `kingdom.chat.clockTriggered` — "{label}: Consequence triggered"
- `kingdom.chat.clockPaused` — "{label}: Paused at 0 — awaiting GM resolution"
- `kingdom.clocks.summary` — "{active} active · {expiring} expiring next turn"

---

## Data Models

### CampaignClock (commonMain pure data class)

```kotlin
// src/commonMain/kotlin/at/posselt/pfrpg2e/campaign/CampaignClock.kt

package at.posselt.pfrpg2e.campaign

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface CampaignClock {
    val id: String              // unique UUID
    val label: String           // "Stag Lord Deadline", "Varnhold Vanishing"
    var turnsRemaining: Int     // current countdown value (>= 0)
    val maxTurns: Int           // starting value (for progress bar, >= 1)
    val description: String     // GM-facing description of what this clock tracks
    var pauseOnExpiry: Boolean  // Decision 3: if true, stop at 0 and wait for GM
    var expired: Boolean        // true once turnsRemaining reaches 0 (regardless of soft-pause)
    var active: Boolean         // false = manually deactivated by GM
    val expiryConsequenceUnrest: Int  // unrest to add on expiry trigger (0 = none)
    val expiryMessage: String   // human-readable consequence description
}
```

**Constraints:**
- `maxTurns >= 1`
- `turnsRemaining >= 0` and `<= maxTurns` (coerced to `[0, maxTurns]` on write)
- `expired == true` iff `turnsRemaining == 0`
- When `pauseOnExpiry == true` and `turnsRemaining == 0`, clock stops ticking until GM sets `pauseOnExpiry = false` or manually adjusts `turnsRemaining`

### ClockTickEvent (commonMain)

```kotlin
// src/commonMain/kotlin/at/posselt/pfrpg2e/campaign/ClockTickResult.kt

package at.posselt.pfrpg2e.campaign

import kotlinx.js.JsPlainObject

enum class ClockEventType { ADVANCED, EXPIRED, TRIGGERED, PAUSED }

@JsPlainObject
external interface ClockTickEvent {
    val clockId: String
    val type: String            // ClockEventType value
    val label: String           // clock label (for chat output)
    val oldTurns: Int           // turns before tick
    val newTurns: Int           // turns after tick
    val message: String?        // expiryMessage or custom event description
    val unrestChange: Int       // change to apply to kingdom unrest (from expiryConsequenceUnrest)
}

@JsPlainObject
external interface ClockTickResult {
    val events: Array<ClockTickEvent>
    val updatedClocks: Array<CampaignClock>
    val totalUnrestChange: Int  // sum of all unrest changes this tick
}
```

### TickResult extension (jsMain)

Add to the existing `TickResult` class:

```kotlin
// Added to TurnTickingEngine.kt TickResult
data class TickResult(
    // ... existing fields ...
    val clockEvents: Array<ClockTickEvent> = emptyArray(),
)
```

### KingdomData extension

Add to the existing `KingdomData` interface:

```kotlin
// Added to KingdomData.kt
var campaignClocks: Array<CampaignClock>?   // null = not yet migrated; empty = no clocks
```

---

## CampaignClockManager Engine (commonMain)

Pure function — no Foundry/Game dependencies, fully unit-testable. Mirrors the `TurnTickingEngine` pattern.

```kotlin
// src/commonMain/kotlin/at/posselt/pfrpg2e/campaign/CampaignClockManager.kt

object CampaignClockManager {

    /**
     * Tick all active, non-expired clocks by 1 turn.
     * Paused-at-expiry clocks (pauseOnExpiry && turnsRemaining == 0 && !expired)
     * are skipped and produce a PAUSED event.
     */
    fun tickAll(clocks: Array<CampaignClock>): ClockTickResult {
        val events = mutableListOf<ClockTickEvent>()
        var totalUnrest = 0
        val updated = clocks.map { clock ->
            if (!clock.active || clock.expired) return@map clock
            if (clock.turnsRemaining == 0 && clock.pauseOnExpiry) {
                // Decision 3: soft-pause — do not tick
                events += ClockTickEvent(
                    clockId = clock.id, type = "PAUSED", label = clock.label,
                    oldTurns = 0, newTurns = 0, message = null, unrestChange = 0
                )
                return@map clock
            }
            if (clock.turnsRemaining <= 1) {
                // Reaches 0 this tick → expire
                val expired = clock.copy(turnsRemaining = 0, expired = true)
                val unrest = if (!clock.pauseOnExpiry) clock.expiryConsequenceUnrest else 0
                totalUnrest += unrest
                events += ClockTickEvent(
                    clockId = clock.id, type = "EXPIRED", label = clock.label,
                    oldTurns = 1, newTurns = 0,
                    message = clock.expiryMessage, unrestChange = unrest
                )
                if (!clock.pauseOnExpiry && unrest > 0) {
                    events += ClockTickEvent(
                        clockId = clock.id, type = "TRIGGERED", label = clock.label,
                        oldTurns = 0, newTurns = 0,
                        message = "Unrest +${clock.expiryConsequenceUnrest}",
                        unrestChange = 0  // already counted
                    )
                }
                return@map expired
            }
            // Normal advance
            val oldTurns = clock.turnsRemaining
            val updated = clock.copy(turnsRemaining = oldTurns - 1)
            events += ClockTickEvent(
                clockId = clock.id, type = "ADVANCED", label = clock.label,
                oldTurns = oldTurns, newTurns = updated.turnsRemaining,
                message = null, unrestChange = 0
            )
            return@map updated
        }.toTypedArray()

        return ClockTickResult(events = events.toTypedArray(), updatedClocks = updated, totalUnrestChange = totalUnrest)
    }
}
```

---

## TurnTickingEngine Integration

### Modified `tick()` signature

```kotlin
fun tick(
    fame: RawFame,
    resourcePoints: RawResources,
    resourceDice: RawResources,
    consumption: RawConsumption,
    commodities: RawCurrentCommodities,
    storage: CommodityStorage,
    councilCooldowns: RawCouncilCooldowns?,
    modifiers: Array<RawModifier>,
    campaignClocks: Array<CampaignClock> = emptyArray(),  // NEW
): TickResult {
```

### Step 9 in tick(): after modifier expiry (line ~185)

```kotlin
    // 9) Tick campaign clocks
    val clockResult = CampaignClockManager.tickAll(campaignClocks)
    // Apply unrest from clock expiries
    val unrestIncrease = clockResult.totalUnrestChange
    // ... include clockResult.events in TickResult ...
```

### Modified TickResult

Add:
```kotlin
data class TickResult(
    // ... all existing fields ...
    val clockEvents: Array<ClockTickEvent> = emptyArray(),
)
```

### KingdomSheet.endTurn handler change

The existing `end-turn` action handler in `KingdomSheet.kt` extracts the engine inputs from current kingdom state. It must now also pass `kingdom.campaignClocks ?: emptyArray()` and merge `TickResult.clockEvents` into the end-turn chat message.

---

## Migration Strategy

### Migration29 (jsMain)

- **Version:** 29
- **Target:** `KingdomData` on each kingdom actor
- **Operation:** Add `campaignClocks: []` (empty array) if field is missing
- **Data backfill:** None — no existing clock data exists
- **Idempotent:** Check `kingdom.campaignClocks != null`; if present, skip
- **Ordering:** Runs after Migration28 (if registered); the migrations list is ordered by version number

```kotlin
class Migration29 : Migration(29) {
    override suspend fun migrateKingdom(game: Game, kingdom: dynamic) {
        if (kingdom.campaignClocks == null) {
            kingdom.campaignClocks = emptyArray<Any>()
        }
    }
}
```

### Migration registration

Add `Migration29()` to the `migrations` list in `Migrations.kt`:

```kotlin
private val migrations = listOf(
    Migration17(),
    // ... through ...
    Migration26(),
    Migration29(),   // latest — campaign clocks
)
latestMigrationVersion reclaculates automatically from the list.
```

### Settings registration

No new Foundry settings keys needed. Clocks are stored per-kingdom-actor (in `KingdomData`), not as world/client settings.

---

## UI / Template Changes

### 1. New "Campaign Clocks" tab in turn section nav

Add `CLOCK` to `TurnNavEntry` enum (between `EVENT` and `XP`). This places a "Campaign Clocks" sub-tab on the turn page sidebar tabs ("Upkeep", "Commerce", "Leadership", "Region", "Civic", "Army", "Event", **"Campaign"**, "XP", "End").

### 2. Clock dashboard panel (new partial template)

**File:** `src/jsMain/resources/applications/kingdom/sections/clocks/page.hbs`

```
<aside class="km-kingdom-sheet-sidebar km-kingdom-sheet-sidebar-clocks"
       {{#if (ne currentNavEntry 'clocks')}}hidden{{/if}}>
    <div>
        <h3>{{localizeKM "kingdom.clocks.title"}}</h3>
        {{#if isGM}}
            <button type="button" data-action="open-clock-dialog">
                {{localizeKM "kingdom.clocks.manage"}}
            </button>
        {{/if}}
        {{#if campaignClocks.length}}
            {{#each campaignClocks}}
                <div class="km-clock-card {{#if expired}}km-clock-expired{{/if}}">
                    <div class="km-clock-header">
                        <span class="km-clock-label">{{label}}</span>
                        {{#if active}}
                            <span class="km-clock-remaining">{{remainingTurns}}/{{maxTurns}}</span>
                        {{else}}
                            <span class="km-clock-inactive">Inactive</span>
                        {{/if}}
                    </div>
                    <div class="km-clock-progress-bar">
                        <div class="km-clock-progress-fill
                                    {{#if isExpiring}}km-clock-urgent{{/if}}
                                    {{#if isExpired}}km-clock-expired-fill{{/if}}"
                             style="width: {{progressPercent}}%;">
                        </div>
                    </div>
                    {{#if isExpiring}}
                        <p class="km-clock-warning">{{localizeKM "kingdom.clocks.expiring"}}</p>
                    {{/if}}
                    {{#if expired}}
                        <p class="km-clock-warning">
                            {{#if paused}}
                                {{localizeKM "kingdom.clocks.paused"}}
                            {{else}}
                                {{localizeKM "kingdom.clocks.expired"}}
                            {{/if}}
                        </p>
                    {{/if}}
                    {{#if isGM}}
                        <div class="km-clock-actions">
                            <button type="button" data-action="edit-clock" data-clock-id="{{id}}">
                                {{localizeKM "kingdom.clocks.edit"}}
                            </button>
                        </div>
                    {{/if}}
                </div>
            {{/each}}
        {{else}}
            <p>{{localizeKM "kingdom.clocks.noClocks"}}</p>
        {{/if}}
    </div>
</aside>
```

### 3. Turn page main content section

Add a corresponding `<div class="km-kingdom-sheet-content" {{#if (ne currentNavEntry 'clocks')}}hidden{{/if}}>` section with a detailed clock view (read-only summary for players, with full details).

### 4. Clock summary in End Turn section

Modify the "End Turn" section of the turn page to show a clock summary line:

```html
{{#if hasActiveClocks}}
    <p class="km-clock-summary">
        🕐 {{localizeKM "kingdom.clocks.summary" active=activeClockCount expiring=expiringClockCount}}
    </p>
{{/if}}
```

### 5. End-turn chat message with clock events

Modify `end-turn.hbs` to add at the bottom (after existing `<ul>`):

```handlebars
{{#if clockEvents.length}}
<hr>
<h3>{{localizeKM "kingdom.clocks.title"}}</h3>
<ul>
    {{#each clockEvents}}
        <li>
            {{#if (eq type "ADVANCED")}}
                {{localizeKM "kingdom.chat.clockAdvanced" label=label old=oldTurns remaining=newTurns}}
            {{/if}}
            {{#if (eq type "EXPIRED")}}
                {{localizeKM "kingdom.chat.clockExpired" label=label message=message}}
            {{/if}}
            {{#if (eq type "TRIGGERED")}}
                {{localizeKM "kingdom.chat.clockTriggered" label=label}}
            {{/if}}
        </li>
    {{/each}}
</ul>
{{/if}}
```

### 6. Campaign Clock Dialog (GM-only)

A `CrudApplication` dialog with:
- List of existing clocks (label, remaining/max turns, pause-on-expiry status, active/inactive)
- "Add Clock" button → opens inline form
- Per-clock: Edit, Delete, Toggle Active, Toggle Pause-on-Expiry
- Form fields: label (text), maxTurns (number, min 1), description (textarea), pauseOnExpiry (checkbox), expiryConsequenceUnrest (number, min 0), expiryMessage (textarea)
- Validation: label non-empty, maxTurns >= 1
- On save: write clocks back to `kingdom.campaignClocks`, then call `kingdom.setKingdom()`

### 7. CSS additions (clocks.css)

- `.km-clock-card` — card layout for each clock
- `.km-clock-progress-bar` — outer bar (grey background, fixed height)
- `.km-clock-progress-fill` — inner fill: green (>50%), yellow (25-50%), red (<25%), animation on expiry
- `.km-clock-urgent` — pulsing animation for clocks with 1 turn remaining
- `.km-clock-expired` — red border, dimmed card
- `.km-clock-warning` — bold orange/red text for expiry warnings
- `.km-clock-summary` — compact summary badge text style

---

## Tests

### Unit tests — CampaignClockTest (T1, commonTest)

| Test | Acceptance |
|------|------------|
| T1.1 default clock has positive maxTurns | `CampaignClock(id="x", label="Test", turnsRemaining=5, maxTurns=5, ...).maxTurns == 5` |
| T1.2 turnsRemaining coerced to >= 0 | Creating with `turnsRemaining = -1` coerces to 0 |
| T1.3 turnsRemaining coerced to <= maxTurns | Creating with `turnsRemaining = 10, maxTurns = 5` coerces to 5 |
| T1.4 expired true when turnsRemaining == 0 | `clock.copy(turnsRemaining=0).expired == true` |
| T1.5 expired false when turnsRemaining > 0 | `clock.copy(turnsRemaining=3).expired == false` |

### Unit tests — CampaignClockManagerTest (T2, commonTest)

| Test | Acceptance |
|------|------------|
| T2.1 empty clocks → empty result | `tickAll(emptyArray())` → `events=[], totalUnrestChange=0` |
| T2.2 single clock advances by 1 | Clock 5→4, 1 ADVANCED event, unrest=0 |
| T2.3 clock with 1 turn expires | Clock 1→0, 1 EXPIRED event, `expired=true` |
| T2.4 clock with 0 turns and expired=true → skipped | No events, clock unchanged |
| T2.5 pauseOnExpiry stops at 0 | Clock 1→0: EXPIRED event, but next tick: PAUSED event, no unrest |
| T2.6 pauseOnExpiry clock after GM unblocks | `pauseOnExpiry=false` on expired clock: next tick → EXPIRED with unrest |
| T2.7 unrest consequence summed correctly | 2 clocks with 3 and 5 unrest → `totalUnrestChange=8` |
| T2.8 inactive clocks skipped completely | `active=false` → no events for that clock |
| T2.9 multiple clocks tick independently | 3 clocks at different values each advance correctly |
| T2.10 ADVANCED event has correct old/new values | `oldTurns=5, newTurns=4` |
| T2.11 EXPIRED event includes expiryMessage | event.message == clock.expiryMessage |
| T2.12 TRIGGERED event only fires when unrest > 0 | Clock with `expiryConsequenceUnrest=0` → no TRIGGERED event |
| T2.13 TRIGGERED event fires for each clock with unrest | 2 expiring clocks with unrest → 2 TRIGGERED events |
| T2.14 soft-paused clock counts as expiring in summary | In KingdomSheet context, `isExpiring == true` for paused-at-0 clocks |
| T2.15 progressPercent correct: 5/10 → 50 | `progressPercent == 50` |
| T2.16 progressPercent correct: 0/10 → 0 | `progressPercent == 0` |
| T2.17 urgency colour: >50% = green | `urgencyClass == "km-clock-green"` for 6/10 |
| T2.18 urgency colour: 25-50% = yellow | `urgencyClass == "km-clock-yellow"` for 3/10 |
| T2.19 urgency colour: <25% = red | `urgencyClass == "km-clock-red"` for 1/10 |
| T2.20 maxTurns == 1, turnsRemaining == 1 → expires after 1 tick | EXPIRED after one tickAll call |
| T2.21 event ordering: ADVANCED before EXPIRED before TRIGGERED | Multiple events are sorted by type order |
| T2.22 5-clock stress test: all tick independently | 5 clocks with different values → correct individual results |
| T2.23 do not mutate input array | Input `clocks` array is not modified; `updatedClocks` is a new array |
| T2.24 clock with pauseOnExpiry but turnsRemaining > 0 ticks normally | No special behavior until 0 is reached |
| T2.25 summary counts: active, expiring, expired | `activeCount` excludes inactive; `expiringCount` includes paused-at-0 and 1-turn clocks |

### Integration tests — CampaignClockIntegrationTest (I1, jsTest)

| Test | Acceptance |
|------|------------|
| I1.1 clocks survive TurnTickingEngine tick and affect kingdom state | Run `TurnTickingEngine.tick(kingdomData.clocks)`, verify `clockEvents` in result |
| I1.2 clock expiry increases kingdom unrest | Kingdom with clock (unrest=3) → tick → unrest in result matches |
| I1.3 clock events rendered in end-turn chat | After tick, `end-turn.hbs` receives `clockEvents` context with correct values |
| I1.4 migration adds empty campaignClocks | KingdomData before migration → after Migration29 → `campaignClocks != null` |

### Integration tests — CampaignClockDialogTest (I2, jsTest)

| Test | Acceptance |
|------|------------|
| I2.1 dialog opens for GM, hidden for players | `isGM=true` → dialog renders; `isGM=false` → no button |
| I2.2 add clock creates new entry | Form submit → `kingdom.campaignClocks.length` increases by 1 |
| I2.3 edit clock updates existing entry | Change label → submit → label updated |
| I2.4 delete clock removes entry | Delete → `kingdom.campaignClocks.length` decreases by 1 |
| I2.5 validation rejects empty label | Empty label → form error, no clock created |
| I2.6 validation rejects maxTurns < 1 | `maxTurns=0` → form error |
| I2.7 toggle pause-on-expiry persists | Checkbox checked → submit → `clock.pauseOnExpiry == true` |

### Integration tests — CampaignClockContextTest (I3, jsTest)

| Test | Acceptance |
|------|------------|
| I3.1 context includes correct progressPercent | Clock 3/10 → `progressPercent=30` |
| I3.2 context includes isExpiring for 1-turn clock | 1 turn remaining → `isExpiring=true` |
| I3.3 context includes isExpired for 0-turn clock | 0 turns → `isExpired=true` |
| I3.4 isGM flag gates edit actions | `isGM=false` → context has `showActions=false` |
| I3.5 empty clocks → noEmptyMessage shown | No clocks → `noClocksMessage` key rendered |
| I3.6 correct localization key inclusion | Context uses `{{localizeKM "kingdom.clocks.title"}}` |
| I3.7 summary string formatting correct | `activeClockCount=3, expiringCount=1` → formatted string matches key |

---

## Dependency and Sequencing Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│  PREREQS (already done):                                            │
│  ✅ TurnTickingEngine — complete, tested                            │
│  ✅ KingdomData — complete                                          │
│  ✅ KingdomSheet — complete                                         │
│  ✅ Migration26 pattern — established                               │
└───────────────────────────┬─────────────────────────────────────────┘
                            │
                            ▼
┌───────────────────────────────────────────────────────────────────┐
│  PHASE 1: Core Data Models + Engine (commonMain/commonTest)      │
│  CampaignClock.kt, ClockTickResult.kt, CampaignClockManager.kt    │
│  Tests: T1, T2 (25+ tests, pure commonTest)                       │
└───────────────────────────┬───────────────────────────────────────┘
                            │
                            ▼
┌───────────────────────────────────────────────────────────────────┐
│  PHASE 2: Migration29 (jsMain)                                    │
│  Add campaignClocks field, register in Migrations.kt              │
│  Tests: I1.4 (migration test, compile check + existing pipeline)  │
└───────────────────────────┬───────────────────────────────────────┘
                            │
                            ▼
┌───────────────────────────────────────────────────────────────────┐
│  PHASE 3: TurnTickingEngine Integration (jsMain/jsTest)           │
│  Extend tick() signature, add step 9, extend TickResult           │
│  Tests: I1.1–I1.3                                                 │
└───────────────────────────┬───────────────────────────────────────┘
                            │
                            ▼
┌───────────────────────────────────────────────────────────────────┐
│  PHASE 4: UI — Localization + Dashboard Panel (jsMain)           │
│  en.json keys, clocks.hbs, clocks.css, CampaignClockContext.kt    │
│  Tests: I3.1–I3.7 (context tests)                                 │
└───────────────────────────┬───────────────────────────────────────┘
                            │
                            ▼
┌───────────────────────────────────────────────────────────────────┐
│  PHASE 5: UI — Clock Management Dialog (jsMain)                 │
│  CampaignClockDialog.kt, campaign-clock-dialog.hbs               │
│  Wire to KingdomSheet.kt action handler                           │
│  Tests: I2.1–I2.7                                                 │
└───────────────────────────┬───────────────────────────────────────┘
                            │
                            ▼
┌───────────────────────────────────────────────────────────────────┐
│  PHASE 6: Chat Integration (jsMain)                              │
│  Modify end-turn.hbs, wire clock events into chat output          │
│  Tests: I1.3                                                      │
└───────────────────────────────────────────────────────────────────┘
```

**Parallel work streams:** Phase 1 must complete first (engine contract). Phases 2–6 can proceed in parallel after Phase 1 depends only on the `CampaignClockManager.tickAll()` interface and `CampaignClock` data class.

**No other features depend on this feature.** Campaign clocks are standalone. Future features (quest/event generator, session prep) can read `campaignClocks` from KingdomData but do not require it.

---

## Manual Foundry Verification Checklist

Use this checklist after implementation to verify the feature end-to-end in a Foundry VTT instance.

### Setup
1. Install/update the module and launch a Foundry world with a kingdom actor
2. Open the kingdom sheet and confirm the turn section loads normally

### Migration
3. If using a pre-existing world: open the browser console, run `game.migratePfrpg2eKingdomCampingWeather()` and confirm no errors
4. Open a kingdom actor → go to the Campaign tab in the turn section → verify "No campaign clocks active" message appears (confirming the migration seeded `campaignClocks: []`)

### Clock Creation (GM only)
5. Click "Manage Clocks" button in the Campaign tab → dialog opens
6. Click "Add Clock" → fill in: label = "Stag Lord", maxTurns = 6, description = "Deadline to deal with the Stag Lord", pauseOnExpiry = unchecked, expiryMessage = "Stag Lord's power grows unchecked", expiryConsequenceUnrest = 2
7. Click Save → verify the clock appears as a card in the Campaign tab with a progress bar at 100% (6/6)
8. Verify the card shows "6 / 6 turns" and the progress bar is green

### Clock Ticking
9. Perform an End Turn (or click end-turn button) → verify the clock advances to 5/6
10. Check the chat log: a message should appear showing "Stag Lord: 6 → 5 turns remaining"
11. Verify the progress bar is still green (>50%)
12. End turn 4 more times → verify the clock shows 1/6, progress bar turns red
13. Verify the card shows "Expiring next turn!" warning text

### Clock Expiry with Consequences
14. End turn once more → clock should show 0/6, expired state (red border, dimmed)
15. Verify the chat log shows "Stag Lord: EXPIRED — Stag Lord's power grows unchecked" and "Stag Lord: Consequence triggered"
16. Verify kingdom unrest increased by 2 (check the unrest field on the kingdom sheet)
17. End turn again → expired clock should not tick further (remains at 0)

### Soft-Pause (Decision 3)
18. Open Clock Manager → edit "Stag Lord" → check "Pause on expiry" → Save
19. Reset turns remaining to 3 (via edit)
20. End turn until clock reaches 0 → verify it stops at 0 and shows "Paused at expiry — awaiting GM resolution"
21. Verify unrest did NOT increase on this expiry
22. Uncheck "Pause on expiry" → end turn → verify the clock now fires its expiry consequence

### Player Visibility (non-GM)
23. Log in as a player (or open the sheet in non-GM mode)
24. Navigate to the Campaign tab → verify clocks are visible but "Manage Clocks" button is hidden
25. Verify edit/delete/action buttons are not rendered for players

### Inactive Clocks
26. Edit a clock → toggle "Active" off → Save
27. End turn → verify the inactive clock does not tick
28. Toggle "Active" back on → end turn → verify it resumes ticking

### Deletion
29. Open Clock Manager → delete a clock → verify it disappears from the dashboard
30. Verify no errors in the browser console throughout all steps

---

## Decision Summary

| Decision | Choice | Citation |
|----------|--------|----------|
| 3 (Campaign Clock Strictness) | Hybrid — strict automation with per-clock soft-pause | `docs/plans/2026-06-01-roadmap-design-decisions.md` Decision 3 |
| Clock storage | Per-kingdom-actor in `KingdomData` (not world settings) | Follows existing pattern (quests, events, modifiers) |
| Clock granularity | Kingdom-turn (monthly) | Roadmap item #1: "Builds directly on TurnTickingEngine.kt" |
| Unrest consequence | Optional per-clock field (0 = none) | Minimal viable automation; avoids complex consequence engine |
| Player visibility | Read-only (GM-only edit) | Follows Decision 2 pattern: GM controls, players see results |
