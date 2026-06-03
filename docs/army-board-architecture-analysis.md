# Army & War Pressure Board — Architecture Analysis

**Date**: 2026-06-01
**Task**: Analyze existing camping/kingdom architecture for army board integration
**Roadmap item**: #12 (Army and war pressure board)

---

## 1. Codebase Overview

The project is a **Foundry VTT module** ("Kingdom Building, Camping & Weather", v6.3.1) written in **Kotlin/JS** (compiled to JavaScript via Kotlin Multiplatform). It uses:

- **Foundry VTT** as the host platform (actors, tokens, scenes, items, Handlebars templates)
- **PF2E system** (d20pf2e / pf2e) as the underlying game system
- **Handlebars** for HTML templates
- **DataModel** (Foundry's built-in) for form data validation
- **Kotlin coroutines** for async operations

### Source layout

```
src/
  commonMain/kotlin/.../data/          # Shared data enums & data classes (multiplatform)
    armies/                            # Army type enums, workbook data, army stats
    events/                            # Kingdom event data classes
    kingdom/                           # Kingdom domain data (skills, abilities, etc.)
  jsMain/kotlin/.../
    camping/                           # Camping system (data, sheet, dialogs)
    camping/dialogs/                   # Camping-specific dialogs
    kingdom/                           # Kingdom system (data, sheet, dialogs, structures)
    kingdom/armies/                    # Army-specific logic (browsers, consumption, tactics)
    kingdom/data/                      # Raw data interfaces for KingdomData fields
    kingdom/dialogs/                   # Kingdom dialog forms (50+ files)
    kingdom/map/                       # Hex grid scene sync
    kingdom/scenes/                    # Scene/layer helpers
    kingdom/sheet/                     # KingdomSheet + DataModel + context builders
    kingdom/sheet/contexts/            # Context builders per sheet section
    kingdom/sheet/navigation/          # Navigation entry enums
    kingdom/structures/               # Structure actors (buildings in settlements)
  jsMain/resources/
    applications/camping/*.hbs         # Camping sheet templates
    applications/kingdom/*.hbs         # Kingdom sheet + dialog templates
    applications/kingdom/sections/*.hbs # Kingdom sheet tab sections
    chatmessages/*.hbs                 # Chat message templates
    components/**/*.hbs                # Reusable form components
```

---

## 2. Data Model Architecture

### 2.1 Actor Storage Pattern

Both camping and kingdom use **PF2EParty actors** as their data containers, storing JSON data in Foundry app flags:

- **Kingdom**: Stored on `PF2EParty` via `setAppFlag("kingdom-sheet", data)` / `getAppFlag("kingdom-sheet")`
  - File: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/Kingdom.kt:14`
- **Camping**: Stored on `PF2EParty` via `setAppFlag("camping-sheet", data)` / `getAppFlag("camping-sheet")`
  - File: `src/jsMain/kotlin/at/posselt/pfrpg2e/camping/CampingData.kt:457`

### 2.2 KingdomData (`kingdom/KingdomData.kt`)

The central data model — 190 fields stored as `@JsPlainObject` external interfaces:

| Key Field | Type | Notes |
|-----------|------|-------|
| `atWar` | Boolean | Existing army/war flag |
| `settings` | KingdomSettings | 35+ boolean/config toggles |
| `fame` | RawFame | Now/next fame points |
| `level`, `xp`, `size` | Int | Kingdom progression |
| `unrest` | Int | Kingdom unrest |
| `resourcePoints`, `resourceDice` | RawResources | now/next pattern |
| `consumption` | RawConsumption | now/next + armies subfield |
| `commodities` | RawCurrentCommodities | now/next pattern (food, lumber, luxuries, ore, stone) |
| `ruin` | RawRuin | corruption/crime/decay/strife with value/threshold/penalty |
| `leaders` | RawLeaders | 8 leader slots (ruler through warden) |
| `ongoingEvents` | Array<RawOngoingKingdomEvent> | Multi-stage event tracking |
| `quests` | Array<RawQuest> | Quest tracking with rewards |
| `modifiers` | Array<RawModifier> | Turn-duration modifiers with expression system |
| `settlements` | Array<RawSettlement> | Settlement data |
| `notes` | RawNotes | GM + public notes |

The `atWar` field on `KingdomData` is the most direct existing integration point for war pressure.

### 2.3 KingdomSettings (`kingdom/KingdomData.kt:64`)

35+ configuration booleans. Key ones for army board:

```kotlin
var recruitableArmiesFolderId: String?   // folder where army actors live
var autoCalculateArmyConsumption: Boolean // auto-calc army consumption
var enableLeadershipModifiers: Boolean   // leadership bonus system
var kingdomEventRollMode: String         // event roll mode
var eventDc: Int, eventDcStep: Int       // event DC scaling
```

This is the natural place to add army board toggles (e.g., `enableArmyPressureBoard`).

### 2.4 Modifier System (`kingdom/KingdomModifier.kt`)

A rich modifier system with:
- Typed modifiers (`ModifierType`: UNTYPED, STATUS, CIRCUMSTANCE, ITEM)
- Conditional expressions (`applyIf` with eq/gt/lt/gte/lte/in/some/all/not/when)
- Turn duration (`turns` field — decremented by `TurnTickingEngine`)
- Button labels for one-click application
- Value expressions for dynamic values

This system is **directly reusable** for army war pressure effects. Army threat modifiers could be added as timed modifiers with expression conditions.

### 2.5 TurnTickingEngine (`kingdom/TurnTickingEngine.kt`)

A pure-functional engine (no Foundry deps, unit-testable) that processes:

1. Reset solution counters
2. Advance fame (next → now)
3. Advance resource points (next → now)
4. Advance resource dice (next → now)
5. Advance consumption (next → now)
6. Merge commodities with storage cap
7. Tick down council cooldowns
8. Tick down modifier durations (expire finished)
9. Tick down injury durations (expire finished)
10. Resolve weather shift

**Army board integration point**: The tick engine already processes modifier durations. Army war pressure ticks (threat clocks increasing, ETA countdowns) should be added as new TickResult fields and processing steps.

### 2.6 Quest Model (`kingdom/data/RawQuest.kt`)

```kotlin
var id: String
var title: String
var description: String
var giver: String
var status: String      // "active" | "completed"
var type: String        // "explore_hex" | "claim_hex" | "build_structure" | ...
var target: String?
var rewards: RawQuestRewards  // rp, xp, unrest, food, lumber, stone, ore, luxuries
var flavorTextCompleted: String
```

Army board quests could extend this with new types like `"defend_settlement"` or `"army_campaign"`.

### 2.7 Ongoing Event System (`kingdom/KingdomEvent.kt`)

Multi-stage events with:
- `stage: Int` (current stage index)
- `settlementSceneId: String?` (location binding)
- `secretLocation: Boolean?`
- `becameContinuous: Boolean?`

This pattern is reusable for army threat events (e.g., "Goblin Horde Approaching" with multiple stages).

---

## 3. UI Architecture

### 3.1 Sheet Hierarchy

All UI extends Foundry's application classes:

```
FormApp<Context, FormData>          # Base for data-driven forms with DataModel validation
  ├── KingdomSheet                  # Main kingdom management tabbed sheet
  ├── CampingSheet                  # Camping management sheet
  ├── AddEvent                      # Event browser
  └── ... (50+ dialog classes)

SimpleApp<Context>                  # Base for read-only/simple apps
  ├── ArmyBrowser                   # Army recruitment browser
  ├── ArmyRecruitmentBrowser        # Workbook army reference
  └── ArmyTacticsBrowser            # Tactics reference
```

### 3.2 KingdomSheet (`kingdom/sheet/KingdomSheet.kt`)

The main UI component — 2087 lines. Key patterns:

- **Tabbed navigation** via `MainNavEntry` enum (KINGDOM, TURN, etc.)
- **Data-driven forms** via `KingdomSheetDataModel` (Foundry DataModel with buildSchema DSL)
- **Context builders**: Section-specific context objects built from raw kingdom data
- **Menu controls**: Gear icons for GM-only actions (configure activities, events, etc.)
- **Drag-and-drop**: Token/actor drops for leader assignment
- **Hooks**: Reacts to scene/tile/drawing/token changes for live updates

**Army board integration**: A new tab or section in KingdomSheet following the existing pattern:
- Add new `MainNavEntry` or section
- Create context builder in `kingdom/sheet/contexts/`
- Add new `.hbs` template in `sections/`
- Wire up `_onClickAction` handlers

### 3.3 CampingSheet (`camping/CampingSheet.kt`)

1423 lines. Similar pattern to KingdomSheet but with:
- Section-based navigation (`CampingSheetSection` enum: PREPARE_CAMPSITE, CAMPING_ACTIVITIES, EATING, SET_WATCHES)
- Complex drag-and-drop (actors to activities, recipes, watch slots)
- Night mode calculation based on world time
- Rest mechanics integration

### 3.4 Handlebars Templates

Sheet sections use individual `.hbs` files:
- `src/jsMain/resources/applications/kingdom/sections/quests/page.hbs` — Quest section
- `src/jsMain/resources/applications/kingdom/sections/turn/page.hbs` — Turn section
- `src/jsMain/resources/applications/kingdom/army-browser.hbs` — Army browser
- `src/jsMain/resources/applications/kingdom/army-recruitment-browser.hbs` — Army recruitment
- `src/jsMain/resources/applications/kingdom/army-tactics-browser.hbs` — Army tactics

Army board needs a new template following this pattern.

### 3.5 Reusable Form Components

```
components/forms/application-form.hbs    # Base form wrapper
components/forms/crud-form.hbs          # Create/read/update/delete form
components/forms/form-element.hbs       # Individual form element
components/forms/form.hbs               # Form container
components/tabs/tabs.hbs                # Tab navigation
components/skill-picker/                # Skill selection component
```

---

## 4. Army System — Existing Code

### 4.1 Army Actors (`kingdom/armies/Armies.kt`)

Uses `PF2EArmy` (Foundry VTT's army actor type):
- `Game.getSelectedArmies()` — get controlled token armies on canvas
- `Game.getRecruitableArmies(folder)` — get army actors in the designated folder
- `Game.setupArmies()` — initial army folder setup + compendium import
- `PF2EArmy.isSpecial` — rarity != common

### 4.2 Army Consumption (`kingdom/armies/ArmyConsumption.kt`)

Auto-calculates army consumption from tokens on scenes:
- `updateArmyConsumption(game)` — sums consumption of all army tokens in the recruitable folder
- `registerArmyConsumptionHooks()` — hooks into token/item/actor/scene CRUD events

### 4.3 Army Data (`commonMain/.../data/armies/`)

- `ArmyType` enum: INFANTRY, CAVALRY, SIEGE, SKIRMISHER
- `WorkbookBasicArmy`: 11 basic armies with stats (consumption, HP, level, attacks, etc.)
- `WorkbookArmyTactic`: 20+ tactical abilities
- `WorkbookSpecializedArmyModifier`: Stat modifiers for specialized armies

### 4.4 Army Browser (`kingdom/dialogs/ArmyBrowser.kt`)

Uses `SimpleApp` pattern with `ArmyContext`:
- Lists recruitable armies sorted by level
- Shows workbook army reference data
- Handles recruitment via `recruit-army` action → `kingdomCheckDialog`

### 4.5 Army Tactics Browser (`kingdom/armies/Tactics.kt`)

- Queries compendium + world items for `PF2ECampaignFeature` with `isArmyTactic`
- Checked via `PF2EArmy.hasTactic(tactic)`

---

## 5. State Management Pattern

### 5.1 Centralized Data

All kingdom state lives on a single `KingdomData` object stored on the `KingdomActor` (PF2EParty). Updates follow:

```kotlin
val kingdom = actor.getKingdom()     // deep clone from flag
kingdom.someField = newValue        // modify
actor.setKingdom(kingdom)           // write back
```

### 5.2 Reactive UI Updates

Sheets/hooks trigger `render()` on relevant Foundry events:
- World time changes → re-render (time-dependent displays)
- Actor/token changes → re-render (leader stats, structure status)
- Custom actions via `ActionDispatcher` → cross-sheet communication

### 5.3 Action Dispatcher Pattern

```kotlin
ActionDispatcher.dispatch(ActionMessage(
    action = "openKingdomSheet",
    data = OpenKingdomSheetAction(actorUuid = actor.uuid)
))
```

Used for showing sheets to players and cross-component coordination.

---

## 6. Architectural Patterns for Army Board Reuse

### 6.1 Pattern: Data Model Extension

Add new fields to existing `KingdomData`:
- Add war threat array, enemy army ETAs, threat clocks to KingdomData
- Add settings to `KingdomSettings` for toggling
- Extend `KingdomSheetDataModel` with new schema fields

**Files to modify**:
- `kingdom/KingdomData.kt` — add new fields to `KingdomData` and `KingdomSettings`
- `kingdom/Kingdom.kt` — add helper functions
- `kingdom/sheet/KingdomSheetDataModel.kt` — extend schema

### 6.2 Pattern: Modifier-Based Pressure

The modifier system can model war pressure:
- Army threats = timed modifiers with conditions
- Use `TurnTickingEngine.tickInjuryDurations` pattern for threat clock countdowns
- Expression system (`applyIf`) for kingdom-level/hex-based conditions

**Files to modify**:
- `kingdom/TurnTickingEngine.kt` — add threat tick processing
- `kingdom/KingdomModifier.kt` — add army threat modifier helpers

### 6.3 Pattern: Sheet Section

Add a new section to KingdomSheet following the quests/turn pattern:
1. Create context data class in `kingdom/sheet/contexts/ArmyPressureContext.kt`
2. Add context builder function
3. Add `.hbs` template
4. Add navigation entry
5. Wire handlers in `_onClickAction`

**New files**:
- `kingdom/sheet/contexts/ArmyPressureContext.kt`
- `src/jsMain/resources/applications/kingdom/sections/army-pressure/page.hbs`
- Extend `kingdom/sheet/navigation/MainNavEntry.kt`

### 6.4 Pattern: Dialog-Based Sub-Apps

For complex interactions (recruit, deploy, battle):
- Create `ArmyPressureDialog` following `AddEvent` pattern (extends `FormApp`)
- Use `SimpleApp` for read-only displays (like `ArmyrecruitmentBrowser`)
- Use `kingdomCheckDialog` for kingdom checks with war context

### 6.5 Pattern: Turn Integration

Army board effects integrate into the turn flow:
- Add threat ETA countdown to `TurnTickingEngine.tick()`
- Add `TickChange` records for army events (threat reached kingdom, etc.)
- Hook into existing `end-turn.hbs` chat template for turn summaries

### 6.6 Pattern: Chat Output

Chat messages use Handlebars templates:
- `src/jsMain/resources/chatmessages/end-turn.hbs` — turn summary
- `src/jsMain/resources/chatmessages/event.hbs` — event resolution
- `src/jsMain/resources/chatmessages/unrest.hbs` — unrest changes

Add new templates for army events (threat arrival, battle outcome).

### 6.7 Pattern: Campaign Clock Equivalent

The existing `OngoingEvent` + staged resolution system is a proto-clock:
- Multi-stage progression
- Settlement binding
- Degree-of-success outcomes with modifiers

Army threat clocks should extend this pattern:
- Replace/adapt with dedicated clock data model
- Support ETA turns, pressure thresholds, auto-trigger events

---

## 7. Key File Reference

| File | Purpose | Lines |
|------|---------|-------|
| `kingdom/KingdomData.kt` | Central data model definitions | ~550 |
| `kingdom/Kingdom.kt` | Actor-flag helpers | 35 |
| `kingdom/KingdomModifier.kt` | Modifier system | 361 |
| `kingdom/TurnTickingEngine.kt` | Turn processing engine | 338 |
| `kingdom/sheet/KingdomSheet.kt` | Main kingdom UI | ~2087 |
| `kingdom/sheet/KingdomSheetDataModel.kt` | Form schema | 308 |
| `camping/CampingData.kt` | Camping data model | ~656 |
| `camping/CampingSheet.kt` | Camping UI | ~1423 |
| `kingdom/armies/Armies.kt` | Army actor helpers | 108 |
| `kingdom/armies/ArmyConsumption.kt` | Auto-consumption | 75 |
| `kingdom/armies/Tactics.kt` | Tactics helpers | 24 |
| `kingdom/dialogs/ArmyBrowser.kt` | Army recruitment UI | 186 |
| `kingdom/dialogs/ArmyRecruitmentBrowser.kt` | Army reference | 123 |
| `kingdom/KingdomEvent.kt` | Event data + parsing | 171 |
| `kingdom/data/RawQuest.kt` | Quest model | 28 |
| `data/events/KingdomEvent.kt` | Event data class | 19 |
| `data/armies/WorkbookArmyData.kt` | Army/tactic data | ~533 |
| `docs/feature-roadmap.md` | Feature roadmap | 358 |

---

## 8. Recommendations for Army Board Implementation

1. **Data model**: Extend `KingdomData` with `warThreats: Array<RawWarThreat>` in a new file `kingdom/data/RawWarThreat.kt`, following the `RawQuest`/`RawModifier` pattern (JsPlainObject interfaces).

2. **Turn ticking**: Add threat processing to `TurnTickingEngine.tick()` — decrement ETAs, trigger events on arrival. New fields on `TickResult`.

3. **UI section**: Add army pressure as a new tab/section in KingdomSheet, not a standalone dialog. This matches how quests, events, and turn management live together. Reuse `FormApp`/`DataModel` pattern.

4. **Threat clocks**: Model as timed modifiers with expression conditions — the modifier system already supports turn-duration countdowns and conditional application.

5. **Quest integration**: Army threats should link to quests (`RawQuest.type = "army_campaign"`) so completing army objectives grants standard rewards.

6. **Event integration**: Reuse the `OngoingEvent` staged system for multi-phase army invasions. Each stage can represent escalating threat levels.

7. **Chat output**: New `chatmessages/army-threat.hbs` template for turn-summary integration and GM alerts.

8. **Settings**: Add `enableArmyPressureBoard: Boolean` to `KingdomSettings` for toggle control. Follow existing pattern of per-feature settings.

9. **Army actor integration**: The existing `PF2EArmy` type and `ArmyBrowser` pattern already handle army CRUD. The war pressure board operates at the kingdom-data level (threat tracking), separate from individual army actors.

10. **Homebrew compatibility**: The war pressure board should work with the homebrew rules profile system (roadmap item #9) — different settings for raw vs homebrew army rules (consumption, available army types, etc.).
