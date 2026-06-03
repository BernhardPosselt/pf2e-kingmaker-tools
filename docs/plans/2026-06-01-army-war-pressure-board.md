# Army & War Pressure Board Implementation Plan

> **Status:** Draft — pending Gregory review
> **Date:** 2026-06-01
> **Roadmap item:** #12 (Army and war pressure board)
> **Design decisions:** Decision 3 (Campaign Clock strictness — strict automation with per-clock soft-pause)

---

## Executive Summary

This plan describes the implementation of the **Army & War Pressure Board** for the pf2e-kingmaker-tools Foundry VTT module. The board gives Game Masters a dedicated section inside the Kingdom Sheet to track enemy war threats, army deployments, and a global war pressure meter — automating threat escalation, ETA countdowns, and pressure-based kingdom consequences.

**Key capabilities:**

- **War Threat tracking:** Create, edit, and resolve enemy army threats with escalation clocks, ETA countdowns, settlement binding, and soft-pause overrides (Decision 3)
- **Army Deployment management:** Assign player armies to threats or garrisoned settlements, track deployment status and costs
- **War Pressure meter:** Auto-calculated 0-100 scale from active threats and deployed armies, with configurable unrest/ruin thresholds
- **Turn integration:** Threat ETAs, escalation levels, and war pressure all tick automatically via `TurnTickingEngine`
- **Navigation tab:** New "Army & Pressure" tab in KingdomSheet following the existing quests/turn/modifiers section pattern
- **Backward compatible:** All new fields are nullable; feature opt-in via `enableArmyPressureBoard` setting

**Relationship to camping/kingdom architecture:** The board lives entirely within the existing kingdom-data-on-actor-flag pattern (`setAppFlag("kingdom-sheet", ...)`). No new actor types or app flags needed. All new data is additive to `KingdomData` and `KingdomSettings`, following the same nullable-array pattern used by `quests`, `ongoingEvents`, and `modifiers`.

---

## Affected Files

### New Kotlin Files (13 files)

| File | Purpose |
|------|---------|
| `src/jsMain/kotlin/.../kingdom/data/RawWarThreat.kt` | `@JsPlainObject` data interface for war threats |
| `src/jsMain/kotlin/.../kingdom/data/RawArmyDeployment.kt` | `@JsPlainObject` data interface for army deployments |
| `src/jsMain/kotlin/.../kingdom/data/RawWarPressure.kt` | `@JsPlainObject` data interface for global war pressure |
| `src/jsMain/kotlin/.../kingdom/data/WarThreatStatus.kt` | Enum: `ACTIVE`, `DEFEATED`, `EVADED`, `EXPIRED` |
| `src/jsMain/kotlin/.../kingdom/data/ArmyDeploymentStatus.kt` | Enum: `DEPLOYED`, `BATTLE`, `RETREATED`, `DESTROYED` |
| `src/jsMain/kotlin/.../kingdom/sheet/contexts/ArmyPressureContext.kt` | UI context data class + builder function |
| `src/jsMain/kotlin/.../kingdom/dialogs/AddWarThreat.kt` | Dialog for creating new war threats |
| `src/jsMain/kotlin/.../kingdom/dialogs/EditWarThreat.kt` | Dialog for editing existing threats |
| `src/jsMain/kotlin/.../kingdom/dialogs/BattleCheckDialog.kt` | Dialog for resolving battle checks |
| `src/jsMain/kotlin/.../kingdom/dialogs/ThreatResolutionDialog.kt` | Dialog for GM to resolve expired threats |

### New Handlebars Templates (5 files)

| File | Purpose |
|------|---------|
| `src/jsMain/resources/applications/kingdom/sections/army-pressure/page.hbs` | Main army pressure section with sub-nav tabs and war pressure meter |
| `src/jsMain/resources/applications/kingdom/sections/army-pressure/threat-card.hbs` | Reusable threat card component |
| `src/jsMain/resources/applications/kingdom/sections/army-pressure/enemy-army-table.hbs` | Enemy army/deployment table |
| `src/jsMain/resources/applications/kingdom/sections/army-pressure/battle-check-dialog.hbs` | Battle check dialog template |
| `src/jsMain/resources/chatmessages/army-threat.hbs` | Threat arrival/escalation chat message |

### Modified Kotlin Files (8 files)

| File | Changes |
|------|---------|
| `kingdom/KingdomData.kt` | Add `warThreats`, `armyDeployments`, `warPressure` to `KingdomData`; add 4 settings to `KingdomSettings` |
| `kingdom/sheet/navigation/MainNavEntry.kt` | Add `ARMY_PRESSURE` enum entry + i18n key |
| `kingdom/TurnTickingEngine.kt` | Extend `tick()` with threat processing + pressure recalculation; add fields to `TickResult` |
| `kingdom/sheet/KingdomSheet.kt` | Register tab, wire actions (`add-war-threat`, `edit-war-threat`, `delete-war-threat`, `deploy-army`, `battle-check`, `resolve-threat`), add context builder call, make section scrollable |
| `kingdom/sheet/KingdomSheetDataModel.kt` | Add schema for army pressure form elements |
| `kingdom/sheet/KingdomSheetContext.kt` | Add `armyPressureContext` field to context |
| `kingdom/dialogs/ArmyBrowser.kt` | Add `Deployment` action to army browser for assigning armies to threats |

### Modified Handlebars Files (1 file)

| File | Changes |
|------|---------|
| `src/jsMain/resources/applications/kingdom/kingdom-sheet.hbs` | Add `{{> kingdom-army-pressure}}` section include |

### Modified Localization (1 file)

| File | Changes |
|------|---------|
| `lang/en.json` | Add ~30 localization keys under `kingdom.armyPressure.*`, `warThreat.*`, `armyDeployment.*`, `warPressure.*` |

---

## Data Models

### RawWarThreat

File: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/data/RawWarThreat.kt`

```kotlin
@JsPlainObject
external interface RawWarThreat {
    var id: String                          // UUID v4
    var name: String                        // "Goblin Horde"
    var description: String                 // GM-facing notes
    var enemyFaction: String?               // "Bandits", "Tuskers", etc.

    // Threat clock
    var escalationLevel: Int                // 0 = just spotted
    var maxEscalation: Int                  // Threshold for invasion
    var eta: Int?                           // Turns until arrival (null = unknown)

    // Binding
    var targetSettlementSceneId: String?    // Which settlement is threatened
    var targetHexLocation: String?          // Hex description for display
    var linkedQuestId: String?              // Links to RawQuest.id
    var linkedEventId: String?              // Links to ongoing event

    // Soft-pause (Decision 3)
    var pauseOnExpiry: Boolean              // GM must manually confirm if true

    // Status
    var status: String                      // "active", "defeated", "evaded", "expired"
    var triggeredTurn: Int?                 // Kingdom turn when escalation hit max
}
```

### RawArmyDeployment

File: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/data/RawArmyDeployment.kt`

```kotlin
@JsPlainObject
external interface RawArmyDeployment {
    var id: String                          // UUID v4
    var armyActorUuid: String               // Foundry PF2EArmy actor UUID
    var armyName: String                    // Cached display name
    var armyType: String                    // Cached ArmyType enum string

    // Assignment
    var assignedThreatId: String?           // Links to RawWarThreat.id
    var garrisonedSettlementId: String?     // Settlement scene ID
    var status: String                      // "deployed", "battle", "retreated", "destroyed"

    // Turn tracking
    var deployedTurn: Int                   // Kingdom turn when deployed
}
```

### RawWarPressure

File: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/data/RawWarPressure.kt`

```kotlin
@JsPlainObject
external interface RawWarPressure {
    var currentPressure: Int                // 0-100
    var pressurePerTurn: Int                // Auto-calculated
    var unrestModifier: Int                 // Applied to unrest
    var consumptionModifier: Int            // Extra consumption
    var unrestThreshold: Int                // Pressure level for +1 unrest/turn
    var ruinThreshold: Int                  // Pressure level for ruin
    var lastChange: Int?                    // Last tick's delta
}
```

### KingdomData Extensions

In `KingdomData.kt`, add after existing array fields:

```kotlin
// Army & war pressure board (roadmap #12)
var warThreats: Array<RawWarThreat>?
var armyDeployments: Array<RawArmyDeployment>?
var warPressure: RawWarPressure?
```

### KingdomSettings Extensions

In `KingdomSettings`, add:

```kotlin
var enableArmyPressureBoard: Boolean     // Master toggle (default: false)
var autoCalculateWarPressure: Boolean    // Auto-calc (default: true)
var showThreatDistance: Boolean          // Display ETA (default: true)
var armyPressureBoardMode: String        // "basic" | "advanced" (default: "basic")
```

### Enums

**WarThreatStatus** (`WarThreatStatus.kt`): `ACTIVE`, `DEFEATED`, `EVADED`, `EXPIRED`

**ArmyDeploymentStatus** (`ArmyDeploymentStatus.kt`): `DEPLOYED`, `BATTLE`, `RETREATED`, `DESTROYED`

Both follow the existing `ValueEnum` + `Translatable` pattern with `fromCamelCase()` and `toCamelCase()`.

---

## Migration Plan

**No breaking migration required.** All new fields on `KingdomData` are nullable (`Array<T>?`, `RawWarPressure?`), following the existing pattern used by all other kingdom arrays. Existing kingdom actors without these fields deserialize them as `undefined`/`null` without errors.

### Migration steps

1. **Code deployment only.** No data migration script, no new app flags.
2. **Default settings on first load:**
   - `enableArmyPressureBoard`: `false` (opt-in)
   - `autoCalculateWarPressure`: `true`
   - `showThreatDistance`: `true`
   - `armyPressureBoardMode`: `"basic"`
3. **Default war pressure on first activation** (when GM toggles the feature on):
   ```
   RawWarPressure(currentPressure=0, pressurePerTurn=0, unrestModifier=0,
                  consumptionModifier=0, unrestThreshold=50, ruinThreshold=75,
                  lastChange=null)
   ```
4. **Feature toggle:** The army pressure tab only renders when `enableArmyPressureBoard == true`. All `TurnTickingEngine` tick functions use `emptyArray()` defaults for threats and deployments, so existing kingdoms without data tick normally.

### Backward compatibility guarantees

- Existing kingdom actors: no crash, no data loss, no migration
- `TurnTickingEngine.tick()`: all new parameters have defaults
- UI: gated behind `enableArmyPressureBoard` setting
- `@JsPlainObject` interfaces: nullable fields handle missing data gracefully

---

## UI / Template Changes

### KingdomSheet Integration

Following the existing quests/turn/modifiers section pattern:

1. **Navigation:** Add `ARMY_PRESSURE` to `MainNavEntry` enum with i18n key `kingdomMainNav.armyPressure`
2. **Context builder:** Create `ArmyPressureContext` with parsed domain types for threats, deployments, war pressure, and settings flags
3. **Template include:** Add `{{> kingdom-army-pressure}}` to `kingdom-sheet.hbs`
4. **Scrollable:** Set `scrollable = true` on the army pressure section in `KingdomSheet.kt`

### Section Template (`army-pressure/page.hbs`)

The section uses sub-navigation tabs (following CampingSheet's `CampingSheetSection` pattern):

| Tab | Content |
|-----|---------|
| **Threats** | Active threat cards with escalation bars, ETA, target settlement, status, action buttons (Edit, Resolve, Delete) |
| **Armies** | Deployed armies table with name, type, assignment, status, action buttons (Reassign, Retreat, Battle) |
| **Battles** | Active battle checks with army vs threat matchup, resolution button |
| **History** | Log of resolved/expired threats and completed battles |

Plus a **global war pressure meter** displayed above the tabs (0-100 bar with threshold markers and current modifiers).

### Dialog Templates

| Dialog | Purpose |
|--------|---------|
| `AddWarThreat` / `EditWarThreat` | Form for name, description, faction, escalation max, ETA, target settlement, linked quest/event, soft-pause toggle |
| `BattleCheckDialog` | Army vs threat check with d20 roll, modifiers, degree-of-success outcome |
| `ThreatResolutionDialog` | GM pick consequence: spawn invasion event, apply unrest spike, spawn linked quest, mark evaded |

### Action Handlers

Wire in `KingdomSheet._onClickAction`:

| Handler | Action |
|---------|--------|
| `add-war-threat` | Open `AddWarThreat` dialog |
| `edit-war-threat` | Open `EditWarThreat` dialog with threat ID |
| `delete-war-threat` | Confirm + remove threat from `warThreats` array |
| `deploy-army` | Open army browser pre-filtered for deployment |
| `battle-check` | Open `BattleCheckDialog` with army + threat IDs |
| `resolve-threat` | Open `ThreatResolutionDialog` for expired threat |

### CSS Classes

New styles in `kingdom-sheet.css`:

`.km-army-pressure-container`, `.km-pressure-meter`, `.km-pressure-fill`, `.km-pressure-threshold`, `.km-threat-card`, `.km-threat-escalation`, `.km-threat-eta`, `.km-deployment-status`, `.km-army-pressure-tabs`, `.km-army-pressure-tab`

### Localization Keys (30 keys)

| Key Pattern | Examples |
|-------------|----------|
| `kingdom.armyPressure.*` | `title`, `pressureMeter`, `unrestModifier`, `consumptionModifier` |
| `warThreat.*` | `name`, `description`, `escalationLevel`, `eta`, `pauseOnExpiry` |
| `warThreatStatus.active` | Status labels |
| `armyDeployment.*` | `assignedThreat`, `garrisonedSettlement`, `deployedTurn` |
| `armyDeploymentStatus.deployed` | Status labels |
| `kingdomMainNav.armyPressure` | Tab label: "Army & Pressure" |

---

## Test Strategy

### Test Files to Create (17 files, ~113 tests)

**commonTest — Data Enums (2 files, ~12 tests)**

| File | Coverage |
|------|----------|
| `WarThreatStatusTest.kt` | Enum parsing, camelCase round-trip, i18n key generation |
| `ArmyDeploymentStatusTest.kt` | Enum parsing, camelCase round-trip, i18n key generation |

**commonTest — Data Class Contracts (3 files, ~27 tests)**

| File | Coverage |
|------|----------|
| `RawWarThreatTest.kt` | Default values, JSON serialization round-trip |
| `RawArmyDeploymentTest.kt` | Default values, JSON serialization round-trip |
| `RawWarPressureTest.kt` | Default values, JSON serialization round-trip |

**jsTest — JSON Serialization Round-trips (3 files, ~11 tests)**

| File | Coverage |
|------|----------|
| `RawWarThreatSerializationTest.kt` | JS JSON.parse → Kotlin → JSON.stringify fidelity |
| `RawArmyDeploymentSerializationTest.kt` | JS serialization fidelity |
| `RawWarPressureSerializationTest.kt` | JS serialization fidelity |

**jsTest — War Pressure Ticking (1 file, ~12 tests)**

| File | Coverage |
|------|----------|
| `WarThreatTickTest.kt` | ETA decrement, escalation increment, expiry trigger, soft-pause behavior, status transitions |

**jsTest — Context & Navigation (2 files, ~11 tests)**

| File | Coverage |
|------|----------|
| `ArmyPressureContextTest.kt` | Context builder with empty data, context with threats/deployments, settings flags |
| `ArmyPressureNavigationTest.kt` | ARMY_PRESSURE nav entry, i18n key exists, tab renders |

**jsTest — Dialogs (4 files, ~22 tests)**

| File | Coverage |
|------|----------|
| `AddWarThreatDialogTest.kt` | Form validation, UUID generation, default status |
| `EditWarThreatDialogTest.kt` | Pre-population, save updates threat in array |
| `BattleCheckDialogTest.kt` | Roll calculation, degree-of-success outcomes |
| `ThreatResolutionDialogTest.kt` | Consequence application, status update to resolved |

**jsTest — Integration (1 file, ~10 tests)**

| File | Coverage |
|------|----------|
| `ArmyPressureIntegrationTest.kt` | Full tick with threats + deployments + pressure, turn advancement with pressure modifiers |

**jsTest — Handlebars Templates (1 file, ~8 tests)**

| File | Coverage |
|------|----------|
| `ArmyPressureTemplateTest.kt` | Template renders with empty context, template with data, sub-nav tab switching |

### Test Patterns

- Follow existing `ArmyStatsTest.kt` pattern for data class + table + finder tests
- Follow `TurnTickingEngineTest.kt` for engine tick + change tracking tests
- Follow `CampingUpdateBuilderTest.kt` for JS builder pattern tests

### Running Tests

```bash
cd /home/grego/code/pf2e-kingmaker-tools
./gradlew jsTest        # JS target tests (dialogs, templates, integration)
./gradlew commonTest    # Common target tests (enums, data classes)
```

---

## Manual Verification Checklist

### Board Appearance (5 steps)

1. Open Kingdom Sheet → verify "Army & Pressure" tab appears in main navigation
2. Click tab → verify section renders with war pressure meter and sub-navigation tabs
3. Verify sub-nav tabs: Threats, Armies, Battles, History
4. Verify empty state shows "No active threats" and "No deployed armies" messages
5. Verify war pressure meter shows 0/100 with threshold markers at configured positions

### War Threats CRUD (9 steps)

6. Click "Add Threat" → verify dialog opens with all form fields
7. Fill form (name, escalation max 5, ETA 3, target settlement) → Save → verify threat card appears
8. Verify threat card shows: name, escalation bar (0/5), ETA (3 turns), target, status badge
9. Click Edit → modify escalation max to 8 → verify card updates
10. Add second threat → verify both cards appear in list
11. Click Verify on threat → verify resolution dialog opens with consequence options
12. Mark threat as "Defeated" → verify status changes to "Defeated"
13. Click Delete on defeated threat → verify confirmation → verify removal
14. Add threat with pauseOnExpiry=true → advance turns until escalation hits max → verify status becomes "paused" (not auto-resolved)

### Threat Resolution Flow (7 steps)

15. Create threat with escalation max 2, no soft-pause
16. Advance 2 kingdom turns via TurnTickingEngine → verify escalation hits max and status becomes "expired"
17. Open resolution dialog → verify consequence options shown
18. Choose "Spawn Unrest Spike" → verify unrest increases by configured modifier
19. Choose "Spawn Linked Quest" → verify new quest appears in quest list
20. Verify linked event created if "Spawn Invasion Event" chosen
21. Verify threat moves to History tab after resolution

### Army Deployment CRUD (8 steps)

22. Click "Deploy Army" → verify army browser opens
23. Select army from browser → verify deployment dialog opens
24. Assign to existing threat → Save → verify army appears in Armies tab
25. Verify deployment row shows: army name, type, assigned threat name, status "Deployed"
26. Click Reassign → select different threat → verify assignment updates
27. Click Retreat → verify status changes to "Retreat" and threat assignment clears
28. Mark army as "Destroyed" → verify it filters from active deployments
29. Advance turns → verify deployed armies contribute to consumption modifier

### Battle Checks (8 steps)

30. With army deployed against threat, go to Battles tab → verify matchup displayed
31. Click "Battle Check" → verify dialog opens with army stats and threat info
32. Verify modifiers shown (army tactics, terrain, leader bonuses)
33. Roll check → verify degree-of-success calculation (critical success / success / failure / critical failure)
34. On success → verify threat escalation reduced or threat status can be set to "Defeated"
35. On failure → verify army status can be set to "Retreated" or "Destroyed"
36. Verify battle result logged to History tab
37. Verify chat message displayed with battle outcome

### History Tab (4 steps)

38. Resolve multiple threats → verify all appear in History tab with resolution type
39. Verify history shows: threat name, resolution, turn number, outcome details
40. Deploy and resolve armies → verify completed deployments appear in history
41. Verify history is read-only (no action buttons)

### Turn Ticking Integration (7 steps)

42. Create threat with ETA 3, escalation max 5 → advance 1 turn → verify ETA=2
43. Advance 1 more turn → verify ETA=1, escalation incremented by 1
44. Advance 1 more turn → verify ETA=0 (null), escalation incremented again
45. Verify each turn's TickChange log includes threat tick details
46. Verify war pressure recalculated each turn (active threats * 5 - deployed armies * 2)
47. Verify pressure modifiers applied to kingdom unrest and consumption on tick
48. Verify chat summary includes war pressure change when non-zero

### War Pressure Display (5 steps)

49. Verify pressure meter updates visually after each kingdom turn
50. Verify unrest threshold marker shown on meter at configured value (default 50)
51. Verify ruin threshold marker shown at configured value (default 75)
52. When pressure crosses unrest threshold → verify "+1 Unrest from War Pressure" in tick summary
53. When pressure crosses ruin threshold → verify ruin modifier in tick summary

### Kingdom Settings (4 steps)

54. Disable `enableArmyPressureBoard` → verify Army & Pressure tab disappears
55. Re-enable → verify tab reappears with all data intact
56. Toggle `showThreatDistance` → verify ETA column hidden/shown in threat table
57. Set `armyPressureBoardMode` to "advanced" → verify additional columns/options appear

### Localization / CSS (3 steps)

58. Verify all UI text uses i18n keys (no hardcoded English)
59. Verify CSS styles render correctly (pressure meter gradient, threat card borders, status badges)
60. Verify responsive layout: section usable at Kingdom Sheet's default width

### Backward Compatibility (4 steps)

61. Open existing kingdom actor (without army data) → verify no crash, section shows empty state
62. Verify `TurnTickingEngine.tick()` with no threats/deployments → no errors
63. Enable board on existing kingdom → verify defaults initialize correctly
64. Verify `kingdomData.warThreats` is `undefined` on existing actors (not `[]`)

### Performance & Stress (4 steps)

65. Create 50+ threats → verify render time < 500ms
66. Advance turn with 50 threats + 30 deployments → verify tick completes < 100ms
67. Open section with full data → verify no UI freeze
68. Run 100 turn advances → verify no memory leak (pressure data stable)

---

## Timeline Estimates

| Phase | Scope | Estimated Effort |
|-------|-------|-----------------|
| **Phase 1: Data Models** | RawWarThreat, RawArmyDeployment, RawWarPressure interfaces; 2 enums; KingdomData + KingdomSettings extensions | 1–2 days |
| **Phase 2: Turn Ticking** | Extend TurnTickingEngine with threat tick + pressure recalculation; TickResult extensions; helper functions | 1–2 days |
| **Phase 3: UI — Section & Templates** | ArmyPressureContext, page.hbs + sub-components, KingdomSheet tab registration, CSS | 2–3 days |
| **Phase 4: UI — Dialogs & Actions** | AddWarThreat, EditWarThreat, BattleCheck, ThreatResolution dialogs; action handlers | 2–3 days |
| **Phase 5: Chat Templates** | army-threat.hbs, war pressure in turn summary | 0.5 day |
| **Phase 6: Localization** | All i18n keys in lang/en.json | 0.5 day |
| **Phase 7: Testing** | 17 test files (~113 tests) | 3–4 days |
| **Phase 8: Manual Verification** | 68-step Foundry checklist | 1 day |
| **Total** | | **11–16 days** |

---

## Implementation Order

```
Phase 1: Data Models
    ├── RawWarThreat, RawArmyDeployment, RawWarPressure interfaces
    ├── WarThreatStatus, ArmyDeploymentStatus enums
    └── KingdomData + KingdomSettings extensions
        │
        ▼
Phase 2: Turn Ticking
    ├── tickWarThreats() helper
    ├── recalculateWarPressure() helper
    ├── Extend TurnTickingEngine.tick() + TickResult
    └── TickChange records for army events
        │
        ▼
Phase 3: UI — Section & Templates
    ├── ArmyPressureContext + builder
    ├── MainNavEntry.ARMY_PRESSURE
    ├── page.hbs + sub-component templates
    ├── KingdomSheet tab registration + scrollable
    └── CSS classes
        │
        ▼
Phase 4: UI — Dialogs & Actions
    ├── AddWarThreat dialog
    ├── EditWarThreat dialog
    ├── BattleCheckDialog
    ├── ThreatResolutionDialog
    └── Action handlers in KingdomSheet._onClickAction
        │
        ▼
Phase 5: Chat Output
    ├── army-threat.hbs template
    └── War pressure in end-turn summary
        │
        ▼
Phase 6: Localization
    └── All 30 i18n keys
        │
        ▼
Phase 7: Testing
    ├── Enum tests (2 files)
    ├── Data class tests (3 files)
    ├── Serialization tests (3 files)
    ├── Ticking tests (1 file)
    ├── Context & navigation tests (2 files)
    ├── Dialog tests (4 files)
    ├── Integration tests (1 file)
    └── Template smoke tests (1 file)
        │
        ▼
Phase 8: Manual Verification (Foundry)
    └── 68-step checklist in live Foundry VTT
```

---

## Relationship to Other Roadmap Items

| Item | Integration |
|------|-------------|
| #1 Campaign Clocks | War threats ARE the campaign clock for war; escalation = clock progress; Decision 3 soft-pause applies |
| #2 Quest/Event Generator | `RawWarThreat.linkedQuestId` links threats to auto-generated quests; resolution spawns quests/events |
| #9 Homebrew Rules | `KingdomSettings` toggles allow homebrew army rules profiles |
| #10 Session Prep Dashboard | Surface active threats + pressure in prep view |
| #11 Random Encounters | Threats can spawn random encounter modifiers in threatened hexes |
| #13 Balance Alerts | High war pressure triggers pacing warnings |

---

## Future Extensions (not in v1)

- **Multi-phase invasions:** Extend `WarThreat` with `stages[]` mirroring `RawKingdomEvent.stages`
- **Army battle resolver:** Sub-app for resolving battles using PF2EArmy statblock system
- **Threat templates:** Compendium of pre-built threat templates for quick GM setup
- **Hex-map threat markers:** Visual markers on hex grid showing threat locations (extends HexGridSync)
- **Army supply lines:** Track supply route length for deployed armies (consumption scaling with distance)
- **Advanced mode:** `armyPressureBoardMode = "advanced"` adds threat comparison view, bulk operations, and pressure forecasting

---

## Open Questions for Gregory

1. **Army browser deployment flow:** Should "Deploy Army" open the existing `ArmyBrowser` with a deployment mode, or create a dedicated deployment picker? (Recommendation: extend ArmyBrowser with a `mode` parameter — reuse existing recruitment UI, just change the action on selection from "recruit" to "deploy")
2. **Pressure feedback visibility:** Should war pressure modifiers be visible to players on the kingdom sheet, or GM-only? (Recommendation: GM-only by default, with a toggle)
3. **Battle check automation:** Should battle checks auto-advance the turn tick, or should they be resolved manually before End Turn? (Recommendation: manual resolution — GM initiates battle check from the Battles tab, resolves it, then normal End Turn flow applies)
4. **Threat history retention:** Should resolved threats be kept indefinitely in history, auto-cleaned after N turns, or manually archived? (Recommendation: manual archive — keep by default, GM moves to archive)
