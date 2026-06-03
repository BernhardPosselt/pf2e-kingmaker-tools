# Kingdom Turn Assistant Implementation Plan

> **For Hermes:** Use subagent-driven-development skill to implement this plan task-by-task.

**Goal:** Implement a Kingdom Turn Assistant (roadmap item #5) that guides the GM and players through a kingdom turn with fewer missed steps. The assistant adds a pre-turn checklist, kingdom state summary, activity cap enforcement, end-turn diff preview, and post-turn chat/journal summary — all built on top of the existing `TurnTickingEngine` and kingdom sheet architecture.

**Architecture:** Add a new `TurnWizardApplication` dialog (Foundry `FormApp` subclass) that reads current kingdom state, presents a guided step-by-step flow, and on confirmation applies the turn via the existing `end-turn` action handler. The `TurnTickingEngine` already produces `List<TickChange>` — the wizard consumes this as a preview before commit. Activity cap enforcement is added as a pure helper function checked at render time in both the wizard and the existing turn page.

**Tech Stack:** Kotlin Multiplatform jsMain/jsTest, Foundry `FormApp` / `CrudApplication` dialogs, Handlebars templates, Gradle `compileKotlinJs` / `jsBrowserTest`.

---

## Source of truth

- Roadmap item #5: `docs/feature-roadmap.md`
- Design decisions (card f0): `docs/plans/2026-06-01-roadmap-design-decisions.md` — Decisions 1 and 3 are most relevant (single Gregory profile seam, hybrid strict+soft-pause clocks)
- Existing turn page: `src/jsMain/resources/applications/kingdom/sections/turn/page.hbs`
- Existing engine: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/TurnTickingEngine.kt`
- Existing sheet: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/KingdomSheet.kt`
- Existing end-turn chat: `src/jsMain/resources/chatmessages/end-turn.hbs`
- Existing test: `src/jsTest/kotlin/at/posselt/pfrpg2e/kingdom/TurnTickingEngineTest.kt`

## Non-goals

- Do NOT modify the camping system or camping activity logic.
- Do NOT add campaign clock data models or UI (roadmap item #1, separate feature).
- Do NOT add pacing/balance alert thresholds (roadmap item #13, separate feature).
- Do NOT add homebrew profile import/export or multi-profile support (roadmap item #9, Decision 1: single Gregory profile seam only).
- Do NOT modify `TurnTickingEngine.tick()` itself — the engine is complete; we build on top of its `TickResult.changes` output.
- Do NOT implement a separate turn state machine. The wizard is a UI layer only; all state remains on `KingdomData`.

---

## Affected Files

### New files

| File | Purpose |
|------|---------|
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/dialogs/TurnWizardApplication.kt` | Main wizard dialog: pre-turn checklist, state summary, activity cap display, end-turn preview, commit |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/ActivityCapCalculator.kt` | Pure helper: computes activity caps by phase from `KingdomData`, homebrew settings, and `RawActivity` rules |
| `src/jsMain/resources/applications/kingdom/turn-wizard.hbs` | Wizard Handlebars template: wizard layout with sections for checklist, summary, caps, preview, and action buttons |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/contexts/TurnWizardContext.kt` | JS context interface for the wizard template |
| `src/jsTest/kotlin/at/posselt/pfrpg2e/kingdom/ActivityCapCalculatorTest.kt` | Tests for activity cap calculation with RAW, homebrew toggles, and phase restrictions |
| `src/jsTest/kotlin/at/posselt/pfrpg2e/kingdom/TurnWizardApplicationTest.kt` | Tests for wizard state computation (checklist items, turn summary data, TickChange formatting) |

### Modified files

| File | Change |
|------|--------|
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/KingdomSheet.kt` | Add `data-action="open-turn-wizard"` button handler; move `end-turn` action logic into a reusable `performEndTurn(kingdom)` method callable from both the existing end-turn button and the wizard; threading `TickResult.changes` into end-turn chat |
| `src/jsMain/resources/applications/kingdom/sections/turn/page.hbs` | Add "Open Turn Wizard" button next to existing End Turn button; show activity caps in section headers (e.g., "Leadership (2/2)") |
| `src/jsMain/resources/chatmessages/end-turn.hbs` | Replace static bullet list with dynamic `{{#each changes}}` iteration over `TickChange` records |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/contexts/ActivitiesContext.kt` | Add `performedCount` and `cap` fields to `ActivitiesContext` so the template can display "(2/3)" style labels |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/dialogs/KingdomEventManagement.kt` | Add campaign clock listing to pre-turn checklist data (read-only for now; full clock system is feature #1) |

### Localization keys to add

All in `src/jsMain/resources/lang/en.json` under a `kingdom.turnWizard` namespace:

- `kingdom.turnWizard.title` — "Kingdom Turn Assistant"
- `kingdom.turnWizard.checklist` — "Pre-Turn Checklist"
- `kingdom.turnWizard.checklist.gainFame` — "Gain 1 Fame point"
- `kingdom.turnWizard.checklist.adjustUnrest` — "Adjust Unrest"
- `kingdom.turnWizard.checklist.collectResources` — "Collect Resource Dice"
- `kingdom.turnWizard.checklist.payConsumption` — "Pay Consumption"
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
- `kingdom.turnWizard.journalSummary` — "Turn summary written to journal"

---

## Data Models

### ActivityCapCalculator (pure helper, no state)

```kotlin
// src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/ActivityCapCalculator.kt

data class ActivityCap(
    val phase: String,           // "leadership", "civic", "region", "army", "commerce"
    val current: Int,            // activities performed this turn (from KingdomData or running count)
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

- **Leadership cap**: base 2, +1 if `globalBonuses.increaseLeadershipActivities` (already computed in `KingdomSheet.kt` line 1904).
- **Civic cap**: equal to the number of settlements that have a Civic-phase activity available (computed dynamically).
- **Region cap**: number of claimed hexes with region activities available (computed from work sites and hex state).
- **Army cap**: 1 per army, or `Integer.MAX_VALUE` if army activities are not constrained.
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

---

## Migrations

No database or data migrations are required. All data lives on `KingdomData` which is already persisted via Foundry app flags (`setAppFlag`/`getAppFlag`).

The only state addition is a lightweight `turnWizardState` object stored on the kingdom actor as a transient flag:

- Key: `kingdom-sheet.turn-wizard-state`
- Shape: `{ activitiesPerformed: Record<String, Int>, currentPhase: String, lastTurnTimestamp: Number? }`
- Purpose: Track activities performed during the current wizard session so the wizard can show "(1/2) Leadership" counters.

**Rollback:** If the wizard is cancelled, the flag is never committed. On commit, the flag is cleared. No rollback steps needed.

---

## UI / Template Changes

### 1. Turn page (`page.hbs`)

In the End Turn section (`id="{{actorUuid}}-km-turn-end"`), add a new button before the existing End Turn button:

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
2. **Pre-Turn Checklist section** — checklist items (Gain Fame, Adjust Unrest, Collect Resources, Pay Consumption) with checkboxes; each item shows its current status
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

### 4. Modified `ActivitiesContext.kt`

Add to `ActivitiesContext`:
```kotlin
val performedCount: Int
val cap: Int
val isOverCap: Boolean
```

---

## Tests

### Task-level tests (TDD)

Each implementation task follows the RED-GREENEREFACTOR pattern: write failing test first, implement, verify green.

- `ActivityCapCalculatorTest.kt` (jsTest):
  - Leadership cap is 2 with default settings, 3 with `increaseLeadershipActivities`
  - Civic cap scales with settlement count
  - Region cap respects hex/work site availability
  - Homebrew settings toggles affect caps correctly
  - Over-cap detection flags correctly

- `TurnWizardApplicationTest.kt` (jsTest):
  - Checklist items are correctly derived from kingdom state (unrest > 0 → Adjust Unrest is highlighted)
  - Kingdom state summary computes correctly from sample `KingdomData`
  - `TickChange.toDisplayString()` formats all categories correctly
  - Wizard can only commit when all checklist items are addressed and no over-cap violations exist
  - Cancel dismisses without applying changes

- Modified `TurnTickingEngineTest.kt`:
  - When `TickResult.changes` contains entries for all modified fields, `toDisplayString()` produces non-empty strings
  - Empty changes list produces appropriate "no changes" output

### Integration test scenarios (manual, Foundry)

Covered in the Verification Checklist below.

### Build verification

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew compileKotlinJs compileTestKotlinJs
```

Expected: PASS. Browser tests in WSL may fail due to FirefoxHeadless — record as environment-only blocker.

---

## Manual Foundry Verification Checklist

Use this checklist after implementation to verify the feature works in Foundry:

1. **Open Turn Wizard**
   - [ ] Navigate to a kingdom actor's Turn page
   - [ ] Click "Kingdom Turn Assistant" (or "Open Turn Wizard") button
   - [ ] Wizard dialog opens with all sections visible

2. **Pre-Turn Checklist**
   - [ ] Checklist shows 5 items: Gain Fame, Adjust Unrest, Collect Resources, Pay Consumption, Check Events
   - [ ] Items relevant to current state are highlighted (e.g., Adjust Unrest shows current unrest value)
   - [ ] Checklist items can be checked off manually

3. **Kingdom State Summary**
   - [ ] RP (now/next) matches the turn page sidebar values
   - [ ] Commodities show current values with storage caps
   - [ ] Consumption shows current value
   - [ ] Ruin section shows all four ruin types with thresholds and penalties
   - [ ] Active modifiers count is correct

4. **Activity Caps**
   - [ ] Leadership cap shows correct maximum (2 or 3 depending on bonuses)
   - [ ] Performed count defaults to 0 at wizard open
   - [ ] Over-cap phases are visually highlighted in red
   - [ ] Caps update when relevant settings are changed (V&K toggles, etc.)

5. **End-Turn Preview**
   - [ ] Click "Preview Turn" button
   - [ ] Preview section appears showing formatted TickChange records
   - [ ] Changes include: RP, Fame, Consumption, Commodities, Modifiers Expired
   - [ ] Preview does NOT commit state changes

6. **Complete Turn**
   - [ ] Click "Complete Turn"
   - [ ] Kingdom state updates in Foundry (RP, fame, consumption, commodities, modifiers)
   - [ ] End-turn chat message appears with dynamic changes list
   - [ ] Wizard closes after successful commit

7. **Cancel**
   - [ ] Open wizard, make no changes
   - [ ] Click "Cancel"
   - [ ] Wizard closes, no state changes applied

8. **Existing End Turn button still works**
   - [ ] Click the original "End Turn" button on the turn page
   - [ ] Turn completes normally with updated chat message using new dynamic template

9. **Error handling**
   - [ ] If kingdom actor has no data, wizard shows error message
   - [ ] If commit fails (e.g., actor update error), error is shown and wizard stays open

---

## Risks & Open Questions

### Risks

1. **Activity "performed" tracking granularity.** The current `KingdomData` does not track which activities were performed this turn. The wizard introduces a transient `turnWizardState` flag to track this during the wizard session. If the GM performs activities OUTSIDE the wizard (via the normal turn page), the wizard's count will be stale. **Mitigation:** The wizard warns the user if activities were performed before opening the wizard. For v1, this is advisory only — no enforcement.

2. **TickDiff is computed at preview time, not replayed.** The preview calls `TurnTickingEngine.tick()` with current state. If state changes between preview and commit (e.g., another user modifies the kingdom), the committed result may differ from the preview. **Mitigation:** The commit re-runs `tick()` with the latest state. The preview is advisory. In the future, an optimistic lock on the kingdom actor could be added.

3. **End-turn chat template changes.** Modifying `end-turn.hbs` replaces a static template with a dynamic one. Existing localization keys may need updates. **Mitigation:** The new template uses new localization keys, keeping old ones as fallback. Test with empty changes list.

4. **WSL browser test environment.** As noted in previous plans, FirefoxHeadless may be unavailable in WSL. **Mitigation:** Use `compileKotlinJs` and structural verification. Browser test failures treated as environment-only.

### Open Questions

1. **Turn wizard state model:** Should the wizard be a new modal application, or a guided mode toggle integrated into the existing turn tab? — **Decision:** New modal application (separate dialog). This keeps the existing turn page untouched as a fallback and avoids scope creep. A "guided mode" toggle can be added later as a v2 improvement.

2. **Pressure event suggestions:** The roadmap mentions "suggested pressure events when unrest/ruin is too low." This depends on Feature 13 (pacing alerts) data models which don't exist yet. — **Decision:** Omit pressure event suggestions from v1. Add a placeholder section "Pacing Alerts (coming soon)" in the wizard. Implement when Feature 13 ships.

3. **Homebrew profile seam:** Does `RuleProfile.resolve()` exist yet? — **Decision:** No. Use `KingdomSettings` toggles directly per Decision 1. The calculator reads `kingdomSkillIncreaseEveryLevel`, V&K toggles, and other relevant settings. When a proper `RuleProfile` seam is built, the calculator becomes a thin wrapper around `RuleProfile.resolve()`.

4. **Campaign clock display in checklist:** Should the pre-turn checklist show active campaign clocks? — **Decision:** Yes, but as a read-only list (feature #1's data model doesn't exist yet). Add a placeholder section with a localization key `kingdom.turnWizard.checklist.activeClocks` that reads from `ongoingEvents` filtered by a `clock` trait. When Feature 1 ships, this section lights up.

5. **Journal export:** Should the post-turn summary be written to a journal entry? — **Decision:** v1 writes to chat only. Journal export is a v2 enhancement. Add a localization key `kingdom.turnWizard.journalSummary` now for future use.

---

## Decision References

| Decision | Reference | Application |
|----------|-----------|-------------|
| Decision 1 | Single Gregory profile seam | Calculator reads `KingdomSettings` toggles directly; no `RuleProfile` object |
| Decision 3 | Hybrid strict+soft-pause clocks | Pre-turn checklist shows advisory clock warnings; no auto-consequence |
| Decision 5 | Structured aggregation first | Wizard state summary is structured data, not prose |
