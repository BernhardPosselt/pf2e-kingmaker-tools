# Implementation Plan: Porting KingmakerOne & Spreadsheet Features

This plan outlines the migration and alignment of features from the standalone `KingmakerOne` React/TS application and the `CURRENT.xlsx` Excel sheet into the `pf2e-kingmaker-tools` Kotlin JS Foundry VTT module.

---

## User Direction & Design Decisions

### 1. Turn-Triggered Ticking Engine
- **Decision:** Ticks (for travel times, injury durations, weather shifts, etc.) will be processed automatically during **turn changes** (e.g. at the End Turn action).
- **Impact:** No background real-time timers will run in Foundry.

### 2. Native VTT Actor Integration
- **Decision:** Companion/NPC tracking will map to native Foundry VTT **Actor documents** of the `character` type.
- **Impact:** We will register a custom schema flag to sync travel, ETAs, and actions directly on the actor sheets.

### 3. Hex Grid Synchronization
- **Decision:** Hex status (explored, claimed, cleared, roads) will be bi-directionally synchronized with the actual Foundry Scene drawings and grid layers.

### 4. Settlements Sheet Matrix (Spreadsheet Alignment)
- **Decision:** Track and present the detailed settlement statistics side-by-side on the Settlements page, matching the columns from `/home/grego/code/pf2e-kingmaker-tools/Copy of Royal Kingdom Sheet for Pathfinder 2E - CURRENT.xlsx`.
- **Impact:** Expose population, blocks/lots counts, consumption, influence, maximum item bonuses, and individual item purchase levels (Base, Alchemical, Arcane, Divine, Primal, Occult) in a side-by-side matrix view.

---

## Proposed Changes

### Component 1: Settlements Sheet Grid Matrix

#### [MODIFY] [SettlementsContext.kt](file:///home/grego/code/pf2e-kingmaker-tools/src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/contexts/SettlementsContext.kt)
- Expand `SettlementsContext` properties to hold all Excel-comparable stats:
  ```kotlin
  val population: String
  val blocks: Int
  val maxItemBonus: Int
  val influence: Int
  val consumption: Int
  val baseItemLevel: Int
  val alchemicalItemLevel: Int
  val magicItemLevel: Int
  val arcaneItemLevel: Int
  val divineItemLevel: Int
  val primalItemLevel: Int
  val occultItemLevel: Int
  ```

- Update `Array<RawSettlement>.toContext(...)` to calculate these values:
  - Add `feats: List<ChosenFeat>` parameter.
  - Calculate magical item level increases from feats.
  - Parse available item limits for each category (Alchemical, Magic, Arcane, Divine, Primal, Occult) and set properties.

#### [MODIFY] [KingdomSheet.kt](file:///home/grego/code/pf2e-kingmaker-tools/src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/KingdomSheet.kt)
- Update the call to `kingdom.settlements.toContext` to pass `feats`.

#### [MODIFY] [page.hbs](file:///home/grego/code/pf2e-kingmaker-tools/src/jsMain/resources/applications/kingdom/sections/settlements/page.hbs)
- Add a tab toggle under Settlements: `"Overview"` vs `"Detailed Matrix"`.
- Detailed Matrix displays a comparison table comparing all settlements side-by-side:
  - Row headers (Level, Population, Blocks, Lots, Overcrowded, Consumption, Max Item Bonus, Influence, Item Levels).
  - Columns represent each settlement.

---

### Component 2: Native Companion Roster

#### [NEW] [RawCharacter.kt](file:///home/grego/code/pf2e-kingmaker-tools/src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/data/RawCharacter.kt)
- Define schema to hold companion state on actor flags (destination grid coords, speed, ETA, plot hooks).

#### [NEW] [RosterPanel.kt](file:///home/grego/code/pf2e-kingmaker-tools/src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/dialogs/RosterPanel.kt)
- Port companion display tab and GM commands to native VTT Actor sheets.

---

### Component 3: Hex Grid Syncing

#### [NEW] [HexGridSync.kt](file:///home/grego/code/posselt/pfrpg2e/kingdom/map/HexGridSync.kt)
- Add sync hooks when hex state is edited to redraw scene map layers.

---

## Verification Plan

### Automated Build
- Compile code:
  ```bash
  JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew clean assemble
  ```

### Manual Verification
1. Reload VTT client sheet.
2. Open **Settlements** page. Toggle `"Detailed Matrix"`.
3. Verify that all values (Population, Level, Base, Alchemical, Arcane, Divine, Primal item levels) are correctly calculated and compared side-by-side.
