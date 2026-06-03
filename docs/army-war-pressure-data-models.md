# Army & War Pressure Board — Data Models & Migration Plan

**Date**: 2026-06-01
**Task**: t_46263e23 — Design data models and migrations for army/war pressure entities
**Roadmap item**: #12 (Army and war pressure board)
**Parent architecture analysis**: `docs/army-board-architecture-analysis.md`
**Design decisions ref**: Decision 3 — Campaign Clock strictness (strict automation with per-clock soft-pause)

---

## 1. Overview

The Army & War Pressure Board (roadmap #12) needs the following new data entities:

| Entity | Scope | Storage |
|--------|-------|---------|
| `WarThreat` | Enemy armies approaching the kingdom, threat clocks, invasion phases | `KingdomData.warThreats[]` |
| `ArmyDeployment` | Player army assignments to threats/hexes | `KingdomData.armyDeployments[]` |
| `WarPressure` | Global war pressure track (unrest/commodity drain from active threats) | `KingdomData.warPressure` |
| Army board settings | Feature toggle, display preferences | `KingdomSettings` (3 new booleans) |
| Army pressure context | UI context object for the sheet section | New `ArmyPressureContext.kt` |
| Threat turn ticking | ETA countdowns, escalation, auto-trigger | Extend `TurnTickingEngine.tick()` + `TickResult` |

All data lives on the existing `PF2EParty` actor via the `"kingdom-sheet"` app flag — no new actor types or app flags needed. This follows the existing pattern of `KingdomData` as the single source of truth.

---

## 2. New Data Interfaces

### 2.1 WarThreat

File: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/data/RawWarThreat.kt`

Represents an enemy army or invasion force threatening the kingdom.

```kotlin
@JsPlainObject
external interface RawWarThreat {
    var id: String                          // UUID v4
    var name: String                        // "Goblin Horde", "Varnhold Reinforcements"
    var description: String                 // GM-facing description
    var enemyFaction: String?               // "Bandits", "Tuskers", "Lone Lions"

    // Threat clock (campaign clock equivalent)
    var escalationLevel: Int                // 0 = just spotted, escalates each turn
    var maxEscalation: Int                  // Threshold where invasion triggers
    var eta: Int?                           // Turns until arrival at capital (null = unknown)

    // Binding
    var targetSettlementSceneId: String?    // Which settlement this threatens
    var targetHexLocation: String?          // Hex description for display
    var linkedQuestId: String?              // Links to a RawQuest of type "army_campaign"
    var linkedEventId: String?              // Links to ongoing event if spawned from event

    // Soft-pause override (Decision 3: strict automation with opt-in soft-pause)
    var pauseOnExpiry: Boolean              // If true, GM must manually confirm escalation trigger

    // Status
    var status: String                      // "active", "defeated", "evaded", "expired"
    var triggeredTurn: Int?                 // Kingdom turn number when escalation hit max
}
```

**Design rationale**:
- Mirrors `RawOngoingKingdomEvent` pattern: has ID, status, settlement binding.
- `escalationLevel` / `maxEscalation` is the threat clock — a simple integer pair that is easy to tick in `TurnTickingEngine` and easy to render in UI as a progress bar.
- `eta` is separate from escalation ETA is "turns until they reach us" while escalation is "how bad things get". Both tick down but at different rates.
- `pauseOnExpiry` implements Decision 3: when escalation hits max, the consequence (invasion event, unrest spike) fires unless this is true.
- Links to `RawQuest` and `RawOngoingKingdomEvent` enable the "Links between warfare, quests, and kingdom events" roadmap requirement.

### 2.2 ArmyDeployment

File: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/data/RawArmyDeployment.kt`

Represents a player army that has been deployed to counter a threat or garrison a hex.

```kotlin
@JsPlainObject
external interface RawArmyDeployment {
    var id: String                          // UUID v4
    var armyActorUuid: String               // Foundry actor UUID (PF2EArmy type)
    var armyName: String                    // Cached display name for UI
    var armyType: String                    // Cached ArmyType enum value string

    // Assignment
    var assignedThreatId: String?           // Links to RawWarThreat.id
    var garrisonedSettlementId: String?     // Settlement scene ID
    var status: String                      // "deployed", "battle", "retreated", "destroyed"

    // Turn tracking
    var deployedTurn: Int                   // Kingdom turn number when deployed
}
```

**Design rationale**:
- `armyActorUuid` links to the Foundry `PF2EArmy` actor — existing army CRUD is untouched.
- `armyName` and `armyType` are cached for UI display without needing to fetch the Foundry actor.
- `assignedThreatId` creates the many-to-one relationship: multiple armies can counter one threat.
- `garrisonedSettlementId` allows armies to be assigned to defend settlements without an active threat.
- `deployedTurn` enables turn-based cost calculations (consumption while deployed).

### 2.3 WarPressure

File: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/data/RawWarPressure.kt`

Global war pressure track — represents the cumulative effect of active threats on the kingdom.

```kotlin
@JsPlainObject
external interface RawWarPressure {
    var currentPressure: Int                // 0-100 scale
    var pressurePerTurn: Int                // How much pressure increases per tick

    // Consequence locks
    var unrestModifier: Int                 // Modifier applied to unrest from pressure
    var consumptionModifier: Int            // Extra consumption from supporting armies

    // Thresholds
    var unrestThreshold: Int                // Pressure level that adds +1 unrest/turn
    var ruinThreshold: Int                  // Pressure level that adds ruin (corruption)

    // History for display
    var lastChange: Int?                    // Last tick's pressure delta
}
```

**Design rationale**:
- Separated from `WarThreat` because pressure is a single global value derived from all active threats plus deployed armies.
- Integer 0-100 scale mirrors the ruin system's familiarity.
- `pressurePerTurn` is auto-calculated from active threats + deployments (not user-editable).
- Thresholds give GMs control over when pressure starts hurting the kingdom.
- `unrestModifier` and `consumptionModifier` are applied by `TurnTickingEngine` during the standard tick.

---

## 3. KingdomData Extensions

### 3.1 New fields on `KingdomData`

File: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/KingdomData.kt`

Add to the `KingdomData` interface (after line 189, before the closing brace):

```kotlin
// Army & war pressure board (roadmap #12)
var warThreats: Array<RawWarThreat>?
var armyDeployments: Array<RawArmyDeployment>?
var warPressure: RawWarPressure?
```

**Note**: All three fields are nullable (`?`) following the existing pattern where most arrays use `Array<T>?`. This ensures backward compatibility — existing kingdom actors without these fields will deserialize them as `undefined`/`null` without errors.

### 3.2 New fields on `KingdomSettings`

File: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/KingdomData.kt`

Add to the `KingdomSettings` interface (after line 101):

```kotlin
// Army & war pressure board settings
var enableArmyPressureBoard: Boolean     // Master toggle for the feature
var autoCalculateWarPressure: Boolean    // Auto-calc pressure from active threats
var showThreatDistance: Boolean          // Display ETA or just escalation
var armyPressureBoardMode: String        // "basic" | "advanced" (future-proofing)
```

---

## 4. Enum / Value Type Additions

### 4.1 WarThreatStatus

File: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/data/WarThreatStatus.kt`

```kotlin
package at.posselt.pfrpg2e.kingdom.data

import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase

enum class WarThreatStatus : Translatable, ValueEnum {
    ACTIVE,
    DEFEATED,
    EVADED,
    EXPIRED;

    companion object {
        fun fromString(value: String) = fromCamelCase<WarThreatStatus>(value)
    }

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "warThreatStatus.$value"
}
```

### 4.2 ArmyDeploymentStatus

File: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/data/ArmyDeploymentStatus.kt`

```kotlin
package at.posselt.pfrpg2e.kingdom.data

import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase

enum class ArmyDeploymentStatus : Translatable, ValueEnum {
    DEPLOYED,
    BATTLE,
    RETREATED,
    DESTROYED;

    companion object {
        fun fromString(value: String) = fromCamelCase<ArmyDeploymentStatus>(value)
    }

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "armyDeploymentStatus.$value"
}
```

### 4.3 Army pressure board mode on KingdomSettings — no enum yet

The `armyPressureBoardMode` string field on `KingdomSettings` can be extended later. For now, only `"basic"` is implemented. This follows the same pattern as `kingdomEventRollMode` and `proficiencyMode` which are stored as raw strings.

---

## 5. TurnTickingEngine Extensions

### 5.1 New input parameter: `warThreats`

Add to `TurnTickingEngine.tick()`:

```kotlin
fun tick(
    // ... existing params ...
    warThreats: Array<RawWarThreat> = emptyArray(),
    armyDeployments: Array<RawArmyDeployment> = emptyArray(),
    warPressure: RawWarPressure? = null,
    currentTurn: Int = 0,
): TickResult { ... }
```

### 5.2 New output on TickResult

```kotlin
data class TickResult(
    // ... existing fields ...
    val warThreats: Array<RawWarThreat> = emptyArray(),
    val armyDeployments: Array<RawArmyDeployment> = emptyArray(),
    val warPressure: RawWarPressure? = null,
)
```

### 5.3 Processing steps to add (after step 10 weather, as steps 11-13)

**Step 11: Tick threat ETAs**
- Decrement each threat's `eta` by 1 (if not null and > 0).
- Increment `escalationLevel` by 1 if `eta` reaches 0.
- If `escalationLevel >= maxEscalation`:
  - If `pauseOnExpiry == true`: mark status as "paused" (new pseudo-status), record `triggeredTurn`, add change log. GM must manually resolve.
  - If `pauseOnExpiry == false`: mark as "expired", trigger consequences (unrest spike via modifier, spawn linked quest if not exists).

**Step 12: Release defeated/retired deployments**
- Armies with status "destroyed" that were deployed for > N turns release (remove from array, or keep with "returned" status for history — recommendation: filter to active only).
- Armies with status "retreated" release from assignment.

**Step 13: Recalculate war pressure**
- Base pressure = count of active threats * 5 (5 pressure per threat).
- Subtract pressure: each deployed army reduces by 2.
- Apply pressure thresholds to modifiers (if pressure >= unrestThreshold, add +1 unrest modifier; if >= ruinThreshold, add ruin modifier).
- Update `warPressure.pressurePerTurn` and `warPressure.lastChange`.

### 5.4 New helper function

```kotlin
fun tickWarThreats(
    threats: Array<RawWarThreat>,
    currentTurn: Int,
    changes: MutableList<TickChange>,
): Array<RawWarThreat> = threats.mapNotNull { threat -> ... }.toTypedArray()

fun recalculateWarPressure(
    threats: Array<RawWarThreat>,
    deployments: Array<RawArmyDeployment>,
    current: RawWarPressure?,
): RawWarPressure { ... }
```

---

## 6. KingdomSheet Integration Points

### 6.1 Navigation entry

Add to `MainNavEntry` enum:
```kotlin
ARMY_PRESSURE
```

i18n key: `kingdomMainNav.armyPressure`

### 6.2 Context builder

New file: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/contexts/ArmyPressureContext.kt`

```kotlin
data class ArmyPressureContext(
    val threats: List<WarThreat>,
    val deployments: List<ArmyDeployment>,
    val warPressure: WarPressure?,
    val enableArmyPressureBoard: Boolean,
    val autoCalculateWarPressure: Boolean,
    val showThreatDistance: Boolean,
    val warsheet: KingdomActor?,
)
```

(Where `WarThreat` and `ArmyDeployment` are parsed domain types — follows the pattern of `QuestContext` / `TurnContext`)

### 6.3 Handlebars template

New file: `src/jsMain/resources/applications/kingdom/sections/army-pressure/page.hbs`

Sections:
1. **Global war pressure meter** (0-100 bar with threshold markers)
2. **Active threats table** (Name, Escalation/Max, ETA, Target, Status, Actions)
3. **Deployed armies table** (Name, Type, Assignment, Status, Actions)
4. **Add Threat button** (GM-only)
5. **Deploy Army button** (GM-only, opens army browser pre-filtered)

---

## 7. Data Seeding Requirements

No data packs or compendium entries needed — all data is created dynamically by the GM through the UI.

### 7.1 Default settings

On first load after migration:
- `enableArmyPressureBoard`: `false` (opt-in, GM must toggle on)
- `autoCalculateWarPressure`: `true` (sensible default)
- `showThreatDistance`: `true`
- `armyPressureBoardMode`: `"basic"`

### 7.2 Default war pressure

On first activation (when GM toggles `enableArmyPressureBoard` on):
```kotlin
RawWarPressure(
    currentPressure = 0,
    pressurePerTurn = 0,
    unrestModifier = 0,
    consumptionModifier = 0,
    unrestThreshold = 50,
    ruinThreshold = 75,
    lastChange = null,
)
```

### 7.3 Localization keys needed

| Key | Description |
|-----|-------------|
| `warThreat.*` | Threat form labels and descriptions |
| `warThreatStatus.*` | Status labels (active, defeated, evaded, expired) |
| `armyDeployment.*` | Deployment form labels |
| `armyDeploymentStatus.*` | Status labels |
| `warPressure.*` | Pressure meter labels, threshold markers |
| `kingdomMainNav.armyPressure` | Tab label |
| `armyPressureBoardMode.*` | Mode labels |

---

## 8. File Manifest

### New files to create

| File | Purpose |
|------|---------|
| `src/jsMain/kotlin/.../kingdom/data/RawWarThreat.kt` | WarThreat data interface |
| `src/jsMain/kotlin/.../kingdom/data/RawArmyDeployment.kt` | ArmyDeployment data interface |
| `src/jsMain/kotlin/.../kingdom/data/RawWarPressure.kt` | WarPressure data interface |
| `src/jsMain/kotlin/.../kingdom/data/WarThreatStatus.kt` | Threat status enum |
| `src/jsMain/kotlin/.../kingdom/data/ArmyDeploymentStatus.kt` | Deployment status enum |
| `src/jsMain/kotlin/.../kingdom/data/WarThreatConsequence.kt` | Consequence definitions (invasion, unrest spike, etc.) |
| `src/jsMain/kotlin/.../kingdom/sheet/contexts/ArmyPressureContext.kt` | UI context + builder |
| `src/jsMain/kotlin/.../kingdom/sheet/contexts/ArmyPressureContext.kt` | (join function on KingdomData) |
| `src/jsMain/resources/applications/kingdom/sections/army-pressure/page.hbs` | Sheet section template |
| `src/jsMain/resources/applications/kingdom/sections/army-pressure/threat-form.hbs` | Add/edit threat form |
| `src/jsMain/resources/applications/kingdom/sections/army-pressure/deployment-form.hbs` | Deploy army form |
| `src/jsMain/resources/chatmessages/army-threat.hbs` | Threat arrival chat message |
| `src/jsMain/resources/chatmessages/war-pressure.hbs` | Turn tick pressure summary |

### Existing files to modify

| File | Changes |
|------|---------|
| `kingdom/KingdomData.kt` | Add `warThreats`, `armyDeployments`, `warPressure` fields to `KingdomData`; add 4 settings to `KingdomSettings` |
| `kingdom/sheet/navigation/MainNavEntry.kt` | Add `ARMY_PRESSURE` entry |
| `kingdom/TurnTickingEngine.kt` | Add tick processing for threats + pressure; extend `TickResult` |
| `kingdom/sheet/KingdomSheet.kt` | Register tab, wire actions, add context builder call |
| `kingdom/sheet/KingdomSheetDataModel.kt` | Add schema for army pressure form elements (if using DataModel for form) |
| `kingdom/Kingdom.kt` | Update `setKingdom`/`getKingdom` if new app flags needed (none — same flag) |

### Test files to create

| File | Coverage |
|------|----------|
| `src/commonTest/.../kingdom/WarThreatTickTest.kt` | ETA decrement, escalation, expiry, soft-pause |
| `src/commonTest/.../kingdom/ArmyDeploymentLifecycleTest.kt` | Deploy, reassign, retreat, destroy lifecycle |
| `src/commonTest/.../kingdom/WarPressureCalculationTest.kt` | Pressure from N threats, reduction from M armies, thresholds |
| `src/commonTest/.../kingdom/ArmyPressureIntegrationTest.kt` | Full tick with threats + deployments + pressure |

---

## 9. Backward Compatibility

All new fields on `KingdomData` are nullable (`Array<T>?`). Existing kingdom actors without these fields will:
1. Deserialize with `undefined` values (no crash in Kotlin/JS with `@JsPlainObject`).
2. The UI checks `enableArmyPressureBoard` setting before rendering the section.
3. `TurnTickingEngine.tick()` accepts `emptyArray()` defaults for threats and deployments.
4. No migration script needed on app flags — the data is additive only.

---

## 10. Relationship to Other Roadmap Items

| Item | Integration |
|------|-------------|
| #2 Quest/Event Generator | `RawWarThreat.linkedQuestId` links threats to auto-generated quests |
| #1 Campaign Clocks | Threat escalation IS the campaign clock for war (Decision 3 soft-pause) |
| #11 Random Encounters | Threats can spawn random encounter modifiers in threatened hexes |
| #9 Homebrew Rules | `KingdomSettings` toggles allow homebrew army rules profiles |
| #10 Session Prep Dashboard | Surface active threats + pressure in prep view |
| #13 Balance Alerts | High war pressure triggers pacing warnings |

---

## 11. Future Extensions (not in v1)

- **Multi-phase invasions**: Extend `WarThreat` with `stages[]` (mirroring `RawKingdomEvent.stages`).
- **Army battle resolver**: Sub-app for resolving battles between player armies and threats using the PF2EArmy statblock system.
- **Threat templates**: Compendium of pre-built threat templates the GM can quickly add.
- **Hex-map threat markers**: Visual markers on the hex grid showing threat locations (extends HexGridSync).
- **Army supply lines**: Track supply route length for deployed armies (consumption scaling with distance).
