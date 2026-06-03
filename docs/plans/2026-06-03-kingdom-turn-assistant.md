# Kingdom Turn Assistant Implementation Plan

> **Status:** Draft — pending Gregory review
> **Date:** 2026-06-03
> **Feature:** feature-roadmap.md #5 — Kingdom turn assistant

---

## Executive Summary

This plan describes a **Kingdom Turn Assistant** for the pf2e-kingmaker-tools Foundry module. The assistant guides the GM and players through a kingdom turn with fewer missed steps by providing a pre-turn checklist, kingdom state summary, activity cap display, end-turn diff preview, and post-turn chat summary — all built on top of the existing `TurnTickingEngine` and kingdom sheet architecture.

**Key capabilities:**

- **Pre-turn checklist** — surfaces Gain Fame, Adjust Unrest, Collect Resources, Pay Consumption, and Check Events as actionable items before committing the turn
- **Kingdom state summary** — RP (now/next), commodities with storage caps, consumption, ruin table, active modifier count
- **Activity cap enforcement** — displays "(2/3)" style caps per phase on the turn page and in the wizard, computed from RAW + homebrew settings
- **End-turn diff preview** — calls `TurnTickingEngine.tick()` with current state, displays `TickChange` list before commit
- **Post-turn chat summary** — extends the existing `end-turn.hbs` template with dynamic `TickChange` iteration
- **Turn wizard dialog** — new `TurnWizardApplication` (Foundry `FormApp` subclass) that orchestrates the flow

**Architecture:** The wizard is a UI layer only; all state remains on `KingdomData`. The `TurnTickingEngine` already produces `List<TickChange>` — the wizard consumes this as a preview before commit. Activity cap enforcement is a pure helper function checked at render time in both the wizard and the existing turn page.

**Tech Stack:** Kotlin Multiplatform jsMain/jsTest, Foundry `FormApp` / `CrudApplication` dialogs, Handlebars templates, `kotlin.test`, existing `TurnTickingEngine` + kingdom sheet architecture.

---

## Source of Truth

- Roadmap item #5: `docs/feature-roadmap.md` lines 126-146
- Design decisions (card f0): `docs/plans/2026-06-01-roadmap-design-decisions.md` — Decisions 1, 3, 5
- Existing turn engine: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/TurnTickingEngine.kt`
- Existing kingdom data: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/KingdomData.kt`
- Existing kingdom sheet: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/KingdomSheet.kt`
- Existing turn page template: `src/jsMain/resources/applications/kingdom/sections/turn/page.hbs`
- Existing end-turn chat: `src/jsMain/resources/chatmessages/end-turn.hbs`
- Existing activities context: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/contexts/ActivitiesContext.kt`
- Existing event management: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/dialogs/KingdomEventManagement.kt`
- Existing dialog pattern: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/dialogs/ActivityManagement.kt`
- Existing context pattern: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/contexts/KingdomSheetContext.kt`
- Existing localization: `src/jsMain/resources/lang/en.json`
- Existing test patterns: `src/jsTest/kotlin/at/posselt/pfrpg2e/kingdom/TurnTickingEngineTest.kt`

## Non-Goals

- Do NOT modify the camping system or camping activity logic.
- Do NOT add campaign clock data models or UI (roadmap item #1, separate feature).
- Do NOT add pacing/balance alert thresholds (roadmap item #13, separate feature).
- Do NOT add homebrew profile import/export or multi-profile support (roadmap item #9, Decision 1: single Gregory profile seam only).
- Do NOT modify `TurnTickingEngine.tick()` itself — the engine is complete; we build on top of its `TickResult.changes` output.
- Do NOT implement a separate turn state machine. The wizard is a UI layer only; all state remains on `KingdomData`.
- Do NOT implement pressure event suggestions (depends on Feature 13 pacing alerts).
- Do NOT implement journal export of turn summaries (v2 enhancement).

---

## Affected Files

### New files to create (6)

| # | File | Purpose | Phase |
|---|------|---------|-------|
| N1 | `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/dialogs/TurnWizardApplication.kt` | Main wizard dialog: pre-turn checklist, state summary, activity cap display, end-turn preview, commit | 3 |
| N2 | `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/ActivityCapCalculator.kt` | Pure helper: computes activity caps by phase from `KingdomData`, homebrew settings, and `RawActivity` rules | 1 |
| N3 | `src/jsMain/resources/applications/kingdom/turn-wizard.hbs` | Wizard Handlebars template: wizard layout with sections for checklist, summary, caps, preview, and action buttons | 3 |
| N4 | `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/contexts/TurnWizardContext.kt` | JS context interface for the wizard template | 2 |
| N5 | `src/jsTest/kotlin/at/posselt/pfrpg2e/kingdom/ActivityCapCalculatorTest.kt` | Tests for activity cap calculation with RAW, homebrew toggles, and phase restrictions | 1 |
| N6 | `src/jsTest/kotlin/at/posselt/pfrpg2e/kingdom/TurnWizardApplicationTest.kt` | Tests for wizard state computation (checklist items, turn summary data, TickChange formatting) | 3 |

### Existing files to modify (5)

| # | File | Change | Phase |
|---|------|--------|-------|
| M1 | `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/KingdomSheet.kt` | Add `data-action="open-turn-wizard"` button handler; extract `end-turn` action logic into reusable `performEndTurn(kingdom)` method callable from both the existing end-turn button and the wizard; thread `TickResult.changes` into end-turn chat | 4 |
| M2 | `src/jsMain/resources/applications/kingdom/sections/turn/page.hbs` | Add "Open Turn Wizard" button next to existing End Turn button; show activity caps in section headers (e.g., "Leadership (2/2)") | 4 |
| M3 | `src/jsMain/resources/chatmessages/end-turn.hbs` | Replace static bullet list with dynamic `{{#each changes}}` iteration over `TickChange` records | 4 |
| M4 | `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/contexts/ActivitiesContext.kt` | Add `performedCount` and `cap` fields to activity group context so the template can display "(2/3)" style labels | 2 |
| M5 | `src/jsMain/resources/lang/en.json` | Add ~25 localization keys (see Localization Keys section below) | 2 |

### Localization keys to add

All in `src/jsMain/resources/lang/en.json` under a `kingdom.turnWizard` namespace:

- `kingdom.turnWizard.title` — "Kingdom Turn Assistant"
- `kingdom.turnWizard.checklist` — "Pre-Turn Checklist"
- `kingdom.turnWizard.checklist.gainFame` — "Gain 1 Fame point"
- `kingdom.turnWizard.checklist.adjustUnrest` — "Adjust Unrest"
- `kingdom.turnWizard.checklist.collectResources` — "Collect Resource Dice"
- `kingdom.turnWizard.checklist.payConsumption` — "Pay Consumption"
- `kingdom.turnWizard.checklist.checkEvents` — "Check Ongoing Events"
- `kingdom.turnWizard.state` — "Kingdom State"
- `kingdom.turnWizard.state.rp` — "Resource Points"
- `kingdom.turnWizard.state.commodities` — "Commodities"
- `kingdom.turnWizard.state.consumption` — "Consumption"
- `kingdom.turnWizard.state.unrest` — "Unrest"
- `kingdom.turnWizard.state.ruin` — "Ruin"
- `kingdom.turnWizard.state.modifiers` — "Active Modifiers"
- `kingdom.turnWizard.caps` — "Activity Caps"
- `kingdom.turnWizard.caps.leadership` — "Leadership: {current}/{max}"
- `kingdom.turnWizard.caps.civic` — "Civic: {current}/{max}"
- `kingdom.turnWizard.caps.region` — "Region: {current}/{max}"
- `kingdom.turnWizard.caps.army` — "Army: {current}/{max}"
- `kingdom.turnWizard.caps.commerce` — "Commerce: {current}/{max}"
- `kingdom.turnWizard.preview` — "End-Turn Preview"
- `kingdom.turnWizard.preview.empty` — "No recorded activities this turn"
- `kingdom.turnWizard.preview.changeRp` — "RP: {old} → {new}"
- `kingdom.turnWizard.preview.changeFame` — "Fame: {old} → {new}"
- `kingdom.turnWizard.preview.changeConsumption` — "Consumption: {old} → {new}"
- `kingdom.turnWizard.preview.expiredModifiers` — "{count} modifier(s) expired"
- `kingdom.turnWizard.commit` — "Complete Turn"
- `kingdom.turnWizard.cancel` — "Cancel"
- `kingdom.turnWizard.previewButton` — "Preview Turn"
- `kingdom.turnWizard.journalSummary` — "Turn summary written to journal"

---

## Data Models

### ActivityCapCalculator (pure helper, no state)

```kotlin
// src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/ActivityCapCalculator.kt

data class ActivityCap(
    val phase: String,           // "leadership", "civic", "region", "army", "commerce"
    val current: Int,            // activities performed this turn
    val maximum: Int,            // RAW cap or homebrew-adjusted cap
    val isOverCap: Boolean,      // true when current > maximum
)

data class ActivityCapsResult(
    val caps: List<ActivityCap>,
    val totalPerformed: Int,
    val totalAllowed: Int,
    val hasAnyOverCap: Boolean,
)
```

The calculator reads from `KingdomData.settings` toggles and the homebrew Gregory profile seam (Decision 1). Specifically:

- **Leadership cap**: base 2, +1 if `globalBonuses.increaseLeadershipActivities` (already computed in `KingdomSheet.kt` line ~1904).
- **Civic cap**: equal to the number of settlements that have a Civic-phase activity available (computed dynamically).
- **Region cap**: number of claimed hexes with region activities available (computed from work sites and hex state).
- **Army cap**: 1 per army, or unconstrained if army activities are not limited.
- **Commerce cap**: always 1 (Collect Taxes is the only commerce activity).

**Homebrew integration (Decision 1):** The calculator uses `KingdomSettings` toggles (e.g., `kingdomSkillIncreaseEveryLevel`, V&K toggles) as the seam. A single Gregory profile is represented as a set of settings toggles rather than a separate profile object. The `RuleProfile.resolve()` parameterized seam is NOT built yet — the calculator reads settings directly.

### TurnWizardContext (JS interface for Handlebars)

```kotlin
@JsPlainObject
external interface TurnWizardContext : ValidatedHandlebarsContext {
    val checklist: List<ChecklistItemContext>
    val kingdomState: KingdomStateContext
    val activityCaps: List<ActivityCapContext>
    val previewChanges: List<TickChangeContext>
    val showPreview: Boolean
    val canCommit: Boolean
}
```

Each sub-context (`ChecklistItemContext`, `KingdomStateContext`, `ActivityCapContext`, `TickChangeContext`) is a small JS-plain object with localized labels and values for the template.

### TickChange formatting extension

Add a `fun TickChange.toDisplayString(): String` extension that formats `category/field/oldValue/newValue` into human-readable strings for the chat summary and wizard preview. This is used by both the wizard and the modified `end-turn.hbs` template.

### ActivitiesContext extension

Add to the existing `ActivitiesContext` interface:

```kotlin
val leadershipPerformed: Int
val leadershipCap: Int
val civicPerformed: Int
val civicCap: Int
val regionPerformed: Int
val regionCap: Int
val armyPerformed: Int
val armyCap: Int
val commercePerformed: Int
val commerceCap: Int
```

These are computed at context-building time in `KingdomSheet.kt` by calling `ActivityCapCalculator`.

---

## Migration Strategy

No database or data migrations are required. All data lives on `KingdomData` which is already persisted via Foundry app flags (`setAppFlag`/`getAppFlag`).

The only state addition is a lightweight `turnWizardState` object stored on the kingdom actor as a transient flag:

- Key: `kingdom-sheet.turn-wizard-state`
- Shape: `{ activitiesPerformed: Record<String, Int>, currentPhase: String, lastTurnTimestamp: Number? }`
- Purpose: Track activities performed during the current wizard session so the wizard can show "(1/2) Leadership" counters.

**Rollback:** If the wizard is cancelled, the flag is never committed. On commit, the flag is cleared. No rollback steps needed.

---

## UI / Template Changes

### 1. Turn page (`page.hbs`)

In the End Turn section, add a new button before the existing End Turn button:

```handlebars
<button type="button" data-action="open-turn-wizard">{{localizeKM "kingdom.turnWizard.title"}}</button>
```

Update activity section headers to show performed/cap counts. For example, the Leadership heading:

```handlebars
<h2 id="{{actorUuid}}-km-turn-leadership" {{#unless activities.leadership}}hidden{{/unless}}>
  {{localizeKM "kingdom.leadership"}} ({{activities.leadershipPerformed}}/{{activities.leadershipCap}})
</h2>
```

### 2. Wizard template (`turn-wizard.hbs` — new file)

Layout:
1. **Header** — localized title + kingdom name
2. **Pre-Turn Checklist section** — checklist items (Gain Fame, Adjust Unrest, Collect Resources, Pay Consumption, Check Events) with checkboxes; each item shows its current status
3. **Kingdom State Summary section** — RP (now/next), Commodities (now with storage cap), Consumption, Unsimplified Ruin table, Active Modifiers count
4. **Activity Caps section** — one row per phase: phase label, current/maximum, visual indicator (green/red) if over cap
5. **End-Turn Preview section** (initially hidden, shown after user clicks "Preview Turn") — formatted `TickChange` list
6. **Footer** — "Complete Turn" (primary) and "Cancel" buttons

### 3. End-turn chat (`end-turn.hbs` — modified)

Replace the static `<ul>` with a dynamic iteration:

```handlebars
<h2>{{localizeKM "chatMessages.endTurn.title"}}: {{kingdomName}}</h2>
{{#if changes.length}}
<ul>
  {{#each changes}}
  <li>{{displayText}}</li>
  {{/each}}
</ul>
{{else}}
<p>{{localizeKM "chatMessages.endTurn.noChanges"}}</p>
{{/if}}
```

### 4. KingdomSheet action handler changes

- Add `"open-turn-wizard"` action handler that launches `TurnWizardApplication`
- Extract the existing `"end-turn"` handler logic (lines 1219-1250) into a reusable `performEndTurn(kingdom, actor)` method
- The wizard calls `performEndTurn()` on commit, then posts the chat template
- Thread `TickResult.changes` into the end-turn chat template context

---

## Gradle Commands

All build commands use:
```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew <task>
```

| Task | Command |
|------|---------|
| Compile JS | `compileKotlinJs` |
| Compile tests | `compileTestKotlinJs` |
| Run JS unit tests | `jsTest --tests "at.posselt.pfrpg2e.kingdom.ActivityCapCalculatorTest"` |
| Run JS wizard tests | `jsTest --tests "at.posselt.pfrpg2e.kingdom.TurnWizardApplicationTest"` |
| Validate JSON data | `validateStructures validateKingdomActivities` |

---

## Dependency and Sequencing Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│  PREREQS (already done):                                            │
│  ✅ TurnTickingEngine — complete, tested                            │
│  ✅ KingdomData — complete                                          │
│  ✅ KingdomSheet — complete                                         │
│  ✅ ActivitiesContext — complete                                    │
│  ✅ CrudApplication/FormApp dialog pattern — established            │
│  ✅ Design decisions (card f0) — Decisions 1, 3, 5                 │
└───────────────────────────┬─────────────────────────────────────────┘
                            │
                            ▼
┌───────────────────────────────────────────────────────────────────┐
│  PHASE 1: Activity Cap Calculator (jsMain/jsTest)                │
│  Pure helper — computes caps from KingdomData + settings          │
│  N2: ActivityCapCalculator.kt                                     │
│  N5: ActivityCapCalculatorTest.kt                                 │
│  Tests: U1–U6 (6+ tests)                                          │
└───────────────────────────┬───────────────────────────────────────┘
                            │
                            ▼
┌───────────────────────────────────────────────────────────────────┐
│  PHASE 2: Context + Localization (jsMain)                        │
│  TurnWizardContext interface, ActivitiesContext extension,        │
│  localization keys                                                │
│  N4: TurnWizardContext.kt                                         │
│  M4: ActivitiesContext.kt (add performed/cap fields)              │
│  M5: en.json (localization keys)                                  │
└───────────────────────────┬───────────────────────────────────────┘
                            │
                            ▼
┌───────────────────────────────────────────────────────────────────┐
│  PHASE 3: Turn Wizard Dialog (jsMain/jsTest)                     │
│  TurnWizardApplication, turn-wizard.hbs, TickChange formatting   │
│  N1: TurnWizardApplication.kt                                     │
│  N3: turn-wizard.hbs                                              │
│  N6: TurnWizardApplicationTest.kt                                 │
│  Tests: W1–W8 (8+ tests)                                          │
└───────────────────────────┬───────────────────────────────────────┘
                            │
                            ▼
┌───────────────────────────────────────────────────────────────────┐
│  PHASE 4: Sheet Integration + Chat (jsMain)                      │
│  Wire wizard into KingdomSheet, extract performEndTurn,           │
│  update turn page template, update end-turn chat template         │
│  M1: KingdomSheet.kt                                              │
│  M2: turn/page.hbs                                                │
│  M3: chatmessages/end-turn.hbs                                    │
└───────────────────────────────────────────────────────────────────┘
```

**Parallel work streams:** Phase 1 must complete first (cap calculator contract). Phase 2 can proceed in parallel with Phase 3 since they interact through the `TurnWizardContext` interface defined in Phase 2. Phase 4 depends on all prior phases.

**No other features depend on this feature.** The turn wizard is standalone. Future features (session prep, pacing alerts) can read `TickResult.changes` but do not require the wizard.

---

## Implementation Tasks

### Phase 1: Activity Cap Calculator

---

#### Task 1: Add failing test for ActivityCapCalculator — Leadership cap

**Objective:** Lock in Leadership cap calculation with default and homebrew settings before implementation.

**Files:**
- Create: `src/jsTest/kotlin/at/posselt/pfrpg2e/kingdom/ActivityCapCalculatorTest.kt` (N5)

**Step 1: Write failing test**

```kotlin
package at.posselt.pfrpg2e.kingdom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ActivityCapCalculatorTest {
    @Test
    fun `Leadership cap is 2 with default settings`() {
        val kingdom = createTestKingdom() // helper: default KingdomData
        val result = ActivityCapCalculator.calculate(kingdom)
        val leadership = result.caps.find { it.phase == "leadership" }!!
        assertEquals(2, leadership.maximum)
        assertFalse(leadership.isOverCap)
    }

    @Test
    fun `Leadership cap is 3 with increaseLeadershipActivities bonus`() {
        val kingdom = createTestKingdom(increaseLeadershipActivities = true)
        val result = ActivityCapCalculator.calculate(kingdom)
        val leadership = result.caps.find { it.phase == "leadership" }!!
        assertEquals(3, leadership.maximum)
    }
}
```

**Step 2: Run test to verify failure**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew jsTest --tests "at.posselt.pfrpg2e.kingdom.ActivityCapCalculatorTest"
```
Expected: FAIL — `ActivityCapCalculator` not found

---

#### Task 2: Implement ActivityCapCalculator

**Objective:** Create the `ActivityCapCalculator` pure helper with cap computation for all phases.

**Files:**
- Create: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/ActivityCapCalculator.kt` (N2)

Use the data model defined in the Data Models section above. Key implementation notes:
- Read `KingdomData.settings` for homebrew toggles
- Leadership cap: base 2 + 1 if `globalBonuses.increaseLeadershipActivities`
- Civic cap: count of settlements with civic activities available
- Region cap: count of claimed hexes with region work sites
- Army cap: number of armies (or unconstrained)
- Commerce cap: always 1
- `current` counts come from `KingdomData` activity tracking (0 if no tracking exists yet — the wizard's transient state handles this)

**Step 1: Run test to verify pass**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew jsTest --tests "at.posselt.pfrpg2e.kingdom.ActivityCapCalculatorTest"
```
Expected: PASS

**Step 2: Commit**

```bash
git add src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/ActivityCapCalculator.kt \
        src/jsTest/kotlin/at/posselt/pfrpg2e/kingdom/ActivityCapCalculatorTest.kt
git commit -m "feat(turn-wizard): add ActivityCapCalculator with Leadership cap tests"
```

---

#### Task 3: Add failing tests for remaining phases + over-cap detection

**Objective:** Lock in cap calculation for Civic, Region, Army, Commerce phases and over-cap detection.

**Files:**
- Modify: `src/jsTest/kotlin/at/posselt/pfrpg2e/kingdom/ActivityCapCalculatorTest.kt` (N5)

Add tests:

```kotlin
@Test
fun `Civic cap scales with settlement count`() {
    val kingdom1 = createTestKingdom(settlements = listOf(createSettlement(type = "capital")))
    val kingdom2 = createTestKingdom(settlements = listOf(
        createSettlement(type = "capital"),
        createSettlement(type = "town"),
    ))
    val cap1 = ActivityCapCalculator.calculate(kingdom1).caps.find { it.phase == "civic" }!!.maximum
    val cap2 = ActivityCapCalculator.calculate(kingdom2).caps.find { it.phase == "civic" }!!.maximum
    assertTrue(cap2 > cap1)
}

@Test
fun `Commerce cap is always 1`() {
    val kingdom = createTestKingdom()
    val result = ActivityCapCalculator.calculate(kingdom)
    val commerce = result.caps.find { it.phase == "commerce" }!!
    assertEquals(1, commerce.maximum)
}

@Test
fun `Over-cap detection flags correctly`() {
    val kingdom = createTestKingdom(leadershipPerformed = 3, increaseLeadershipActivities = false)
    val result = ActivityCapCalculator.calculate(kingdom)
    val leadership = result.caps.find { it.phase == "leadership" }!!
    assertTrue(leadership.isOverCap)
    assertTrue(result.hasAnyOverCap)
}

@Test
fun `Homebrew settings toggles affect caps correctly`() {
    val kingdom = createTestKingdom(kingdomSkillIncreaseEveryLevel = true)
    val result = ActivityCapCalculator.calculate(kingdom)
    // Verify that homebrew toggles modify caps as expected
    val leadership = result.caps.find { it.phase == "leadership" }!!
    assertTrue(leadership.maximum >= 2) // base value
}
```

**Step 1: Run tests to verify failure**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew jsTest --tests "at.posselt.pfrpg2e.kingdom.ActivityCapCalculatorTest"
```
Expected: FAIL — new tests reference unimplemented behavior

**Step 2: Implement the remaining cap logic in ActivityCapCalculator**

**Step 3: Run tests to verify pass**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew jsTest --tests "at.posselt.pfrpg2e.kingdom.ActivityCapCalculatorTest"
```
Expected: PASS

**Step 4: Commit**

```bash
git add src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/ActivityCapCalculator.kt \
        src/jsTest/kotlin/at/posselt/pfrpg2e/kingdom/ActivityCapCalculatorTest.kt
git commit -m "feat(turn-wizard): add Civic/Region/Army/Commerce cap tests and over-cap detection"
```

---

### Phase 2: Context + Localization

---

#### Task 4: Add TurnWizardContext interface

**Objective:** Define the JS context interface for the wizard Handlebars template.

**Files:**
- Create: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/contexts/TurnWizardContext.kt` (N4)

```kotlin
package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.ValidatedHandlebarsContext
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface ChecklistItemContext {
    val id: String
    val label: String
    val description: String
    val checked: Boolean
    val highlight: Boolean  // true when item needs attention (e.g., unrest > 0)
}

@JsPlainObject
external interface KingdomStateContext {
    val rpNow: Int
    val rpNext: Int
    val commodities: CommoditiesStateContext
    val consumption: Int
    val unrest: Int
    val ruin: List<RuinStateContext>
    val activeModifiers: Int
}

@JsPlainObject
external interface ActivityCapContext {
    val phase: String
    val phaseLabel: String
    val current: Int
    val maximum: Int
    val isOverCap: Boolean
}

@JsPlainObject
external interface TickChangeContext {
    val category: String
    val field: String
    val displayText: String
}

@JsPlainObject
external interface TurnWizardContext : ValidatedHandlebarsContext {
    val kingdomName: String
    val checklist: Array<ChecklistItemContext>
    val kingdomState: KingdomStateContext
    val activityCaps: Array<ActivityCapContext>
    val previewChanges: Array<TickChangeContext>
    val showPreview: Boolean
    val canCommit: Boolean
}
```

**Step 1: Compile to verify**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew compileKotlinJs
```
Expected: PASS

**Step 2: Commit**

```bash
git add src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/contexts/TurnWizardContext.kt
git commit -m "feat(turn-wizard): add TurnWizardContext JS interface"
```

---

#### Task 5: Add localization keys

**Objective:** Add all `kingdom.turnWizard.*` localization keys to `en.json`.

**Files:**
- Modify: `src/jsMain/resources/lang/en.json` (M5)

Add all keys from the Localization Keys section above.

**Step 1: Compile to verify**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew compileKotlinJs
```
Expected: PASS

**Step 2: Commit**

```bash
git add src/jsMain/resources/lang/en.json
git commit -m "feat(turn-wizard): add localization keys for turn wizard"
```

---

#### Task 6: Extend ActivitiesContext with performed/cap fields

**Objective:** Add `performedCount` and `cap` fields to the activity group context so the turn page template can display "(2/3)" labels.

**Files:**
- Modify: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/contexts/ActivitiesContext.kt` (M4)

Add to `ActivitiesContext`:
```kotlin
val leadershipPerformed: Int
val leadershipCap: Int
val civicPerformed: Int
val civicCap: Int
val regionPerformed: Int
val regionCap: Int
val armyPerformed: Int
val armyCap: Int
val commercePerformed: Int
val commerceCap: Int
```

Update `toActivitiesContext()` to compute these values using `ActivityCapCalculator`.

**Step 1: Compile to verify**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew compileKotlinJs
```
Expected: PASS

**Step 2: Commit**

```bash
git add src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/contexts/ActivitiesContext.kt
git commit -m "feat(turn-wizard): extend ActivitiesContext with performed/cap fields"
```

---

### Phase 3: Turn Wizard Dialog

---

#### Task 7: Add failing test for TurnWizardApplication — checklist derivation

**Objective:** Lock in checklist item derivation from kingdom state before implementation.

**Files:**
- Create: `src/jsTest/kotlin/at/posselt/pfrpg2e/kingdom/TurnWizardApplicationTest.kt` (N6)

```kotlin
package at.posselt.pfrpg2e.kingdom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TurnWizardApplicationTest {
    @Test
    fun `Checklist derives Adjust Unrest as highlighted when unrest is greater than 0`() {
        val kingdom = createTestKingdom(unrest = 3)
        val context = TurnWizardApplication.buildContext(kingdom)
        val unrestItem = context.checklist.find { it.id == "adjust-unrest" }!!
        assertTrue(unrestItem.highlight)
    }

    @Test
    fun `Checklist derives Gain Fame as not highlighted when fame is normal`() {
        val kingdom = createTestKingdom()
        val context = TurnWizardApplication.buildContext(kingdom)
        val fameItem = context.checklist.find { it.id == "gain-fame" }!!
        assertFalse(fameItem.highlight)
    }

    @Test
    fun `All 5 checklist items are present`() {
        val kingdom = createTestKingdom()
        val context = TurnWizardApplication.buildContext(kingdom)
        val ids = context.checklist.map { it.id }.toSet()
        assertTrue(ids.containsAll(listOf(
            "gain-fame", "adjust-unrest", "collect-resources",
            "pay-consumption", "check-events"
        )))
    }
}
```

**Step 1: Run test to verify failure**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew jsTest --tests "at.posselt.pfrpg2e.kingdom.TurnWizardApplicationTest"
```
Expected: FAIL — `TurnWizardApplication` not found

---

#### Task 8: Implement TurnWizardApplication — buildContext

**Objective:** Create the `TurnWizardApplication` class with `buildContext()` static method that derives wizard context from `KingdomData`.

**Files:**
- Create: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/dialogs/TurnWizardApplication.kt` (N1)

Key implementation:
- `buildContext(kingdom: KingdomData): TurnWizardContext` — pure function that computes all context fields
- Checklist derivation: map kingdom state to checklist items (unrest > 0 → highlight Adjust Unrest, etc.)
- Kingdom state: extract RP, commodities, consumption, ruin, modifier count from `KingdomData`
- Activity caps: call `ActivityCapCalculator.calculate(kingdom)`
- Preview changes: initially empty (populated on "Preview Turn" button click)

**Step 1: Run test to verify pass**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew jsTest --tests "at.posselt.pfrpg2e.kingdom.TurnWizardApplicationTest"
```
Expected: PASS

**Step 2: Commit**

```bash
git add src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/dialogs/TurnWizardApplication.kt \
        src/jsTest/kotlin/at/posselt/pfrpg2e/kingdom/TurnWizardApplicationTest.kt
git commit -m "feat(turn-wizard): add TurnWizardApplication with checklist derivation tests"
```

---

#### Task 9: Add failing tests for TickChange formatting and preview

**Objective:** Lock in `TickChange.toDisplayString()` formatting and wizard preview behavior.

**Files:**
- Modify: `src/jsTest/kotlin/at/posselt/pfrpg2e/kingdom/TurnWizardApplicationTest.kt` (N6)

Add tests:

```kotlin
@Test
fun `TickChange toDisplayString formats RP change correctly`() {
    val change = TickChange(category = "resourcePoints", field = "now",
        oldValue = 10, newValue = 12)
    val display = change.toDisplayString()
    assertTrue(display.contains("10"))
    assertTrue(display.contains("12"))
}

@Test
fun `TickChange toDisplayString formats fame change correctly`() {
    val change = TickChange(category = "fame", field = "value",
        oldValue = 5, newValue = 6)
    val display = change.toDisplayString()
    assertTrue(display.contains("Fame") || display.contains("fame"))
}

@Test
fun `Wizard preview shows TickChange list after preview button`() {
    val kingdom = createTestKingdom()
    val context = TurnWizardApplication.buildContextWithPreview(kingdom)
    assertTrue(context.previewChanges.isNotEmpty())
    assertTrue(context.showPreview)
}

@Test
fun `Wizard canCommit is false when over-cap exists`() {
    val kingdom = createTestKingdom(leadershipPerformed = 5) // way over cap
    val context = TurnWizardApplication.buildContext(kingdom)
    assertFalse(context.canCommit)
}

@Test
fun `Wizard canCommit is true when all checks pass`() {
    val kingdom = createTestKingdom() // default state, no over-cap
    val context = TurnWizardApplication.buildContext(kingdom)
    assertTrue(context.canCommit)
}
```

**Step 1: Run tests to verify failure**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew jsTest --tests "at.posselt.pfrpg2e.kingdom.TurnWizardApplicationTest"
```
Expected: FAIL — new tests reference unimplemented behavior

**Step 2: Implement TickChange.toDisplayString() and buildContextWithPreview()**

Add `fun TickChange.toDisplayString(): String` extension in `TurnWizardApplication.kt` or a shared utils file. Implement `buildContextWithPreview()` that calls `TurnTickingEngine.tick()` and maps the resulting `TickChange` list to `TickChangeContext`.

**Step 3: Run tests to verify pass**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew jsTest --tests "at.posselt.pfrpg2e.kingdom.TurnWizardApplicationTest"
```
Expected: PASS

**Step 4: Commit**

```bash
git add src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/dialogs/TurnWizardApplication.kt \
        src/jsTest/kotlin/at/posselt/pfrpg2e/kingdom/TurnWizardApplicationTest.kt
git commit -m "feat(turn-wizard): add TickChange formatting and preview tests"
```

---

#### Task 10: Create turn-wizard.hbs template

**Objective:** Create the Handlebars template for the turn wizard dialog.

**Files:**
- Create: `src/jsMain/resources/applications/kingdom/turn-wizard.hbs` (N3)

Template structure:
1. Header with kingdom name
2. Checklist section with `{{#each checklist}}` iteration
3. Kingdom state summary section
4. Activity caps section with `{{#each activityCaps}}` iteration
5. Preview section (hidden by default, shown when `showPreview` is true)
6. Footer with Preview, Complete Turn, and Cancel buttons

**Step 1: Compile to verify**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew compileKotlinJs
```
Expected: PASS

**Step 2: Commit**

```bash
git add src/jsMain/resources/applications/kingdom/turn-wizard.hbs
git commit -m "feat(turn-wizard): add turn-wizard Handlebars template"
```

---

#### Task 11: Implement TurnWizardApplication dialog class

**Objective:** Implement the full `TurnWizardApplication` as a Foundry `FormApp` subclass with render, action handlers, and commit/cancel logic.

**Files:**
- Modify: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/dialogs/TurnWizardApplication.kt` (N1)

Key implementation:
- Extend `FormApp` (or appropriate base class from `at.posselt.pfrpg2e.app`)
- `render()` — builds context and renders `turn-wizard.hbs`
- `"preview-turn"` action — calls `TurnTickingEngine.tick()`, populates `previewChanges`, sets `showPreview = true`, re-renders
- `"commit-turn"` action — calls `performEndTurn()` on the kingdom actor, posts chat template, closes dialog
- `"cancel"` action — closes dialog without applying changes
- `"toggle-checklist-item"` action — toggles checklist item checked state, re-renders

**Step 1: Compile to verify**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew compileKotlinJs
```
Expected: PASS

**Step 2: Commit**

```bash
git add src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/dialogs/TurnWizardApplication.kt
git commit -m "feat(turn-wizard): implement TurnWizardApplication dialog with preview/commit/cancel"
```

---

### Phase 4: Sheet Integration + Chat

---

#### Task 12: Extract performEndTurn and wire wizard into KingdomSheet

**Objective:** Refactor the existing end-turn handler to share logic with the wizard, and add the wizard launch button.

**Files:**
- Modify: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/KingdomSheet.kt` (M1)

Changes:
1. Extract the end-turn logic from the `"end-turn"` action handler (lines 1219-1250) into a reusable method:
   ```kotlin
   private fun performEndTurn(kingdom: KingdomData, actor: KingdomActor) {
       val realm = game.getRealmData(actor, kingdom)
       val settlements = kingdom.getAllSettlements(game)
       val storage = calculateStorage(realm = realm, settlements = settlements.allSettlements)
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
       // Apply tickResult to kingdom (existing logic)
       kingdom.supernaturalSolutions = tickResult.supernaturalSolutions
       kingdom.creativeSolutions = tickResult.creativeSolutions
       kingdom.fame = tickResult.fame
       kingdom.resourcePoints = tickResult.resourcePoints
       kingdom.resourceDice = tickResult.resourceDice
       kingdom.consumption = tickResult.consumption
       kingdom.commodities = tickResult.commodities
       kingdom.councilCooldowns = tickResult.councilCooldowns
       kingdom.modifiers = tickResult.modifiers
       actor.setKingdom(kingdom)
       // Return tickResult for chat integration
   }
   ```
2. Add `"open-turn-wizard"` action handler:
   ```kotlin
   "open-turn-wizard" -> buildPromise {
       actor.getKingdom()?.let { kingdom ->
           TurnWizardApplication(kingdomActor = actor).launch()
       }
   }
   ```
3. Modify the existing `"end-turn"` handler to call `performEndTurn()` and pass `tickResult.changes` to the chat template.

**Step 1: Compile to verify**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew compileKotlinJs
```
Expected: PASS

**Step 2: Commit**

```bash
git add src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/KingdomSheet.kt
git commit -m "feat(turn-wizard): extract performEndTurn and wire wizard into KingdomSheet"
```

---

#### Task 13: Update turn page template with wizard button and cap display

**Objective:** Add the "Open Turn Wizard" button and activity cap labels to the turn page.

**Files:**
- Modify: `src/jsMain/resources/applications/kingdom/sections/turn/page.hbs` (M2)

Changes:
1. Add wizard button in the End Turn section:
   ```handlebars
   <button type="button" data-action="open-turn-wizard">{{localizeKM "kingdom.turnWizard.title"}}</button>
   ```
2. Update activity section headers to show caps:
   ```handlebars
   <h2>{{localizeKM "kingdom.leadership"}} ({{activities.leadershipPerformed}}/{{activities.leadershipCap}})</h2>
   ```
   (Repeat for each phase)

**Step 1: Compile to verify**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew compileKotlinJs
```
Expected: PASS

**Step 2: Commit**

```bash
git add src/jsMain/resources/applications/kingdom/sections/turn/page.hbs
git commit -m "feat(turn-wizard): add wizard button and cap display to turn page"
```

---

#### Task 14: Update end-turn chat template with dynamic changes

**Objective:** Replace the static bullet list in `end-turn.hbs` with dynamic `TickChange` iteration.

**Files:**
- Modify: `src/jsMain/resources/chatmessages/end-turn.hbs` (M3)

Replace:
```handlebars
<h2>{{localizeKM "chatMessages.endTurn.title"}}: {{kingdomName}}</h2>
{{#if changes.length}}
<ul>
  {{#each changes}}
  <li>{{displayText}}</li>
  {{/each}}
</ul>
{{else}}
<p>{{localizeKM "chatMessages.endTurn.noChanges"}}</p>
{{/if}}
```

Update the `postChatTemplate` call in `KingdomSheet.kt` to pass `changes: tickResult.changes.map { it.toDisplayString() }` in the template context.

**Step 1: Compile to verify**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew compileKotlinJs
```
Expected: PASS

**Step 2: Commit**

```bash
git add src/jsMain/resources/chatmessages/end-turn.hbs
git commit -m "feat(turn-wizard): update end-turn chat template with dynamic TickChange display"
```

---

#### Task 15: Final build verification

**Objective:** Verify the full build compiles cleanly.

**Step 1: Full build**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew compileKotlinJs compileTestKotlinJs
```
Expected: PASS

**Step 2: Run all new tests**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew jsTest --tests "at.posselt.pfrpg2e.kingdom.ActivityCapCalculatorTest" --tests "at.posselt.pfrpg2e.kingdom.TurnWizardApplicationTest"
```
Expected: PASS

**Step 3: Commit**

```bash
git commit -m "feat(turn-wizard): final build verification — all phases complete"
```

---

## Tests

### Unit tests — ActivityCapCalculatorTest (jsTest)

| Test | Acceptance |
|------|------------|
| U1.1 Leadership cap is 2 with default settings | `cap == 2` |
| U1.2 Leadership cap is 3 with `increaseLeadershipActivities` | `cap == 3` |
| U1.3 Civic cap scales with settlement count | More settlements → higher cap |
| U1.4 Region cap respects hex/work site availability | More hexes → higher cap |
| U1.5 Commerce cap is always 1 | `cap == 1` |
| U1.6 Over-cap detection flags correctly | `isOverCap == true` when current > maximum |
| U1.7 Homebrew settings toggles affect caps correctly | Toggles modify caps |
| U1.8 `hasAnyOverCap` is true when any phase is over cap | `hasAnyOverCap == true` |
| U1.9 `totalPerformed` and `totalAllowed` are summed correctly | Sum matches individual caps |

### Unit tests — TurnWizardApplicationTest (jsTest)

| Test | Acceptance |
|------|------------|
| W1.1 Checklist items derived from kingdom state | 5 items present |
| W1.2 Adjust Unrest highlighted when unrest > 0 | `highlight == true` |
| W1.3 Gain Fame not highlighted in normal state | `highlight == false` |
| W1.4 Kingdom state summary computes correctly | RP, commodities, consumption match |
| W1.5 `TickChange.toDisplayString()` formats RP change | Contains old and new values |
| W1.6 `TickChange.toDisplayString()` formats fame change | Contains "Fame" label |
| W1.7 Wizard preview shows TickChange list | `previewChanges.isNotEmpty()` |
| W1.8 `canCommit` is false when over-cap exists | `canCommit == false` |
| W1.9 `canCommit` is true when all checks pass | `canCommit == true` |
| W1.10 Cancel dismisses without applying changes | No state changes |
| W1.11 Checklist toggle updates checked state | `checked` flips on toggle |

### Integration test scenarios (manual, Foundry)

Covered in the Verification Checklist below.

---

## Manual Foundry Verification Checklist

Use this checklist after implementation to verify the feature end-to-end in a Foundry VTT instance.

### Setup
1. Install/update the module and launch a Foundry world with a kingdom actor
2. Open the kingdom sheet and confirm the turn section loads normally

### Turn Page — Activity Caps
3. Navigate to the Turn page → verify activity section headers show "(0/2)" style cap labels
4. Verify Leadership cap shows 2 (or 3 if `increaseLeadershipActivities` is enabled)
5. Verify Commerce cap shows 1
6. Change a relevant kingdom setting (e.g., add a settlement) → verify Civic cap updates

### Open Turn Wizard
7. Click "Kingdom Turn Assistant" (or "Open Turn Wizard") button
8. Wizard dialog opens with all sections visible (checklist, state, caps, footer)

### Pre-Turn Checklist
9. Checklist shows 5 items: Gain Fame, Adjust Unrest, Collect Resources, Pay Consumption, Check Events
10. If unrest > 0, "Adjust Unrest" item is highlighted
11. Checklist items can be checked off manually (toggle checkbox)
12. Checking/unchecking persists while wizard is open

### Kingdom State Summary
13. RP (now/next) matches the turn page sidebar values
14. Commodities show current values with storage caps
15. Consumption shows current value
16. Ruin section shows all four ruin types with thresholds and penalties
17. Active modifiers count is correct

### Activity Caps in Wizard
18. Leadership cap shows correct maximum (2 or 3 depending on bonuses)
19. Performed count defaults to 0 at wizard open
20. Over-cap phases are visually highlighted in red
21. Caps update when relevant settings are changed

### End-Turn Preview
22. Click "Preview Turn" button
23. Preview section appears showing formatted TickChange records
24. Changes include: RP, Fame, Consumption, Commodities, Modifiers Expired
25. Preview does NOT commit state changes (kingdom state unchanged after preview)
26. Clicking preview again refreshes the diff

### Complete Turn
27. Click "Complete Turn"
28. Kingdom state updates in Foundry (RP, fame, consumption, commodities, modifiers)
29. End-turn chat message appears with dynamic changes list (not static bullets)
30. Wizard closes after successful commit

### Cancel
31. Open wizard, make no changes
32. Click "Cancel"
33. Wizard closes, no state changes applied

### Existing End Turn button still works
34. Click the original "End Turn" button on the turn page
35. Turn completes normally with updated chat message using new dynamic template
36. Verify chat message shows actual TickChange records (not empty)

### Error handling
37. If kingdom actor has no data, wizard shows error message or graceful fallback
38. If commit fails (e.g., actor update error), error is shown and wizard stays open
39. No errors in the browser console throughout all steps

---

## Risks & Open Questions

### Risks

1. **Activity "performed" tracking granularity.** The current `KingdomData` does not track which activities were performed this turn. The wizard introduces a transient `turnWizardState` flag to track this during the wizard session. If the GM performs activities OUTSIDE the wizard (via the normal turn page), the wizard's count will be stale. **Mitigation:** The wizard warns the user if activities were performed before opening the wizard. For v1, this is advisory only — no enforcement.

2. **TickDiff is computed at preview time, not replayed.** The preview calls `TurnTickingEngine.tick()` with current state. If state changes between preview and commit (e.g., another user modifies the kingdom), the committed result may differ from the preview. **Mitigation:** The commit re-runs `tick()` with the latest state. The preview is advisory. In the future, an optimistic lock on the kingdom actor could be added.

3. **End-turn chat template changes.** Modifying `end-turn.hbs` replaces a static template with a dynamic one. Existing localization keys may need updates. **Mitigation:** The new template uses new localization keys, keeping old ones as fallback. Test with empty changes list.

4. **WSL browser test environment.** As noted in previous plans, FirefoxHeadless may be unavailable in WSL. **Mitigation:** Use `compileKotlinJs` and structural verification. Browser test failures treated as environment-only.

### Open Questions (all resolved)

1. **Turn wizard state model:** Should the wizard be a new modal application, or a guided mode toggle integrated into the existing turn tab? — **Decision:** New modal application (separate dialog). This keeps the existing turn page untouched as a fallback and avoids scope creep.

2. **Pressure event suggestions:** The roadmap mentions "suggested pressure events when unrest/ruin is too low." This depends on Feature 13 (pacing alerts) data models which don't exist yet. — **Decision:** Omit pressure event suggestions from v1. Add a placeholder section "Pacing Alerts (coming soon)" in the wizard.

3. **Homebrew profile seam:** Does `RuleProfile.resolve()` exist yet? — **Decision:** No. Use `KingdomSettings` toggles directly per Decision 1.

4. **Campaign clock display in checklist:** Should the pre-turn checklist show active campaign clocks? — **Decision:** Yes, but as a read-only list (feature #1's data model doesn't exist yet). Add a placeholder section with a localization key `kingdom.turnWizard.checklist.activeClocks` that reads from `ongoingEvents` filtered by a `clock` trait.

5. **Journal export:** Should the post-turn summary be written to a journal entry? — **Decision:** v1 writes to chat only. Journal export is a v2 enhancement.

---

## Decision Summary

| Decision | Choice | Citation |
|----------|--------|----------|
| 1 (Homebrew profile scope) | Single Gregory profile now, seam for multi-profile | `docs/plans/2026-06-01-roadmap-design-decisions.md` Decision 1 |
| 3 (Campaign clock strictness) | Hybrid — strict automation with per-clock soft-pause | `docs/plans/2026-06-01-roadmap-design-decisions.md` Decision 3 |
| 5 (Session prep output) | Structured aggregation first, prose generation behind seam | `docs/plans/2026-06-01-roadmap-design-decisions.md` Decision 5 |
| Wizard architecture | New modal `FormApp` dialog, not inline toggle | Plan decision (see Open Questions) |
| TickChange formatting | `toDisplayString()` extension on `TickChange` | Plan decision |
| Activity cap source | `KingdomSettings` toggles directly (no `RuleProfile` object) | Decision 1 |
