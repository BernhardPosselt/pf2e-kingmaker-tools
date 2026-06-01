# Workbook Army Data Continuation Implementation Plan

> **For Hermes:** Use subagent-driven-development skill to implement this plan task-by-task.

**Goal:** Finish the next high-value portion of the workbook migration by moving army data from `Copy of Royal Kingdom Sheet for Pathfinder 2E - CURRENT.xlsx` into typed Kotlin data with regression tests.

**Architecture:** Keep the migration small and non-invasive: add commonMain data classes/constants under `data.armies` and commonTest coverage that verifies the workbook rows. Do not wire this into Foundry UI yet; this phase preserves the missing source data so later UI/compendium integration has a reliable in-repo source of truth.

**Tech Stack:** Kotlin Multiplatform commonMain/commonTest, Gradle, kotlin.test.

---

## Gap Summary From Workbook Cross-Reference

Workbook sheets checked: README, Kingdom Sheet, Turn Tracker, History, Settlements, Urban Grids, Armies, Creation, Advancement, ⚙️, Structures, 🪲, Map Experiment, Tables, Army Template, Urban Grid Template.

Already covered or mostly covered:
- `Structures` sheet: all 74 workbook structure names are present in `data/structures/*.json`.
- `Tables` kingdom sizes: covered by `KingdomSize.kt`.
- `Tables` settlement sizes: covered by `SettlementSize.kt`.
- `Tables` army level statistics: covered by `ArmyStatistic.kt`.
- `Tables` army types: covered by `ArmyType.kt`, including `SIEGE`.
- Turn tracker activities: most actual kingdom activities are represented by `data/kingdom-activities/*.json`; several apparent misses are sheet headings/prose, not activities.

Definitive missing workbook data:
- `Tables` basic armies are not represented as structured repo data: Infantry, Cavalry, Sootscale Warriors, Skirmishers, Lizardfolk Defenders, Siege Engines, Greengripe Bombardiers, Nomen Scouts, M'Botuu Frog Riders, Tok-Nikrat Scouts, Tiger Lord Berserkers.
- `Tables` army tactics are not represented as structured repo data: 40 rows from Ambush through Warmongers.
- `Tables` specialized army modifiers are not represented as structured repo data: 8 rows, including Sootscale Warriors, Skirmisher, Lizardfolk Defenders, Greengripe Bombardiers, Nomen Scouts, M'Botuu Frog Riders, Tok-Nikrat Scouts, Tiger Lord Berserkers.
- `⚙️` workbook settings are only partially implemented as behavior/settings; they need a later separate settings-profile plan.
- `Army Template` and blank `Armies` sheet are workbook UI/template artifacts; do not migrate until there is a UI integration plan.

Chosen next migration slice: `Tables` army catalog data. It is high-value because the repo already has `ArmyStatistic.kt`, `ArmyType.kt`, army recruitment, and tactic browser code, but lacks the workbook's army catalog rows.

---

### Task 1: Add failing test for workbook basic armies

**Objective:** Prove the repo lacks structured workbook basic army data before implementation.

**Files:**
- Create: `src/commonTest/kotlin/at/posselt/pfrpg2e/data/armies/WorkbookArmyDataTest.kt`
- Later create: `src/commonMain/kotlin/at/posselt/pfrpg2e/data/armies/WorkbookArmyData.kt`

**Step 1: Write failing test**

Add tests that assert:
- `workbookBasicArmies.size == 11`
- army names match workbook order
- `Cavalry` has type `ArmyType.CAVALRY`, consumption 2, level 3, melee attacks, high maneuver save, accessible, starting tactic `Overrun`
- `Sootscale Warriors` are inaccessible and include `Accustomed to Panic` and `Darkvision`

**Step 2: Run test to verify failure**

Run: `JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew commonTest --tests at.posselt.pfrpg2e.data.armies.WorkbookArmyDataTest`

Expected: FAIL because `workbookBasicArmies` and related types do not exist.

---

### Task 2: Implement workbook basic armies

**Objective:** Add typed basic army rows copied from the workbook.

**Files:**
- Create: `src/commonMain/kotlin/at/posselt/pfrpg2e/data/armies/WorkbookArmyData.kt`

**Step 1: Create minimal implementation**

Add:
- `enum class ArmyAttackMode { MELEE, RANGED, BOTH }`
- `enum class ArmyManeuverSave { HIGH, LOW }`
- `data class WorkbookBasicArmy(...)`
- `val workbookBasicArmies = listOf(...)` containing the 11 workbook rows.

**Step 2: Run test to verify pass**

Run: `JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew commonTest --tests at.posselt.pfrpg2e.data.armies.WorkbookArmyDataTest`

Expected: PASS for basic army assertions.

---

### Task 3: Add failing tests for workbook tactics and specialized modifiers

**Objective:** Lock down the remaining army table rows from the workbook.

**Files:**
- Modify: `src/commonTest/kotlin/at/posselt/pfrpg2e/data/armies/WorkbookArmyDataTest.kt`
- Modify: `src/commonMain/kotlin/at/posselt/pfrpg2e/data/armies/WorkbookArmyData.kt`

**Step 1: Write failing tests**

Add tests that assert:
- `workbookArmyTactics.size == 40`
- first tactic `Ambush` has min level 8 and allowed type `SKIRMISHER`
- `Darkvision` is allowed for all four army types
- `Trample` has action `Trample [three-action]` and trait `Attack`
- `Warmongers` has three action grants
- `workbookSpecializedArmyModifiers.size == 8`
- `Greengripe Bombardiers` has scouting -2, DC +5, AC -2, attack +1
- `Skirmisher` base modifier has AC -2, high save +2, low save +2

**Step 2: Run test to verify failure**

Run the same `commonTest --tests ...` command.

Expected: FAIL because tactics/modifier data do not exist yet.

---

### Task 4: Implement workbook tactics and specialized modifiers

**Objective:** Add the remaining workbook army table data.

**Files:**
- Modify: `src/commonMain/kotlin/at/posselt/pfrpg2e/data/armies/WorkbookArmyData.kt`

**Step 1: Add data types**

Add:
- `data class WorkbookArmyTactic(...)`
- `data class WorkbookSpecializedArmyModifier(...)`
- `val workbookArmyTactics = listOf(...)` with all 40 rows.
- `val workbookSpecializedArmyModifiers = listOf(...)` with all 8 rows.

**Step 2: Run test to verify pass**

Run the same `commonTest --tests ...` command.

Expected: PASS.

---

### Task 5: Verify build and update migration docs

**Objective:** Make sure the data compiles and the migration state is documented.

**Files:**
- Modify: `docs/migration_tasks.md` or add a short note to the plan if avoiding churn in existing docs.

**Step 1: Run targeted test**

Run: `JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew commonTest --tests at.posselt.pfrpg2e.data.armies.WorkbookArmyDataTest`

Expected: PASS.

**Step 2: Run full assemble**

Run: `JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew clean assemble`

Expected: PASS.

**Step 3: Final check**

Run: `git status --short`

Expected: only the new plan, new test, and new data file should be attributable to this task; pre-existing unrelated working tree changes remain untouched.
