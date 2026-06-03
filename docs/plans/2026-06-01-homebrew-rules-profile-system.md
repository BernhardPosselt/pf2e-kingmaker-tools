# Homebrew Rules Profile System Implementation Plan

> **Status:** Draft — pending Gregory review
> **Date:** 2026-06-02
> **Feature:** feature-roadmap.md #9 — Homebrew rules profile system

---

## Executive Summary

This plan describes a **homebrew rules profile system** for the pf2e-kingmaker-tools Foundry module. The system lets the GM switch between RAW (rules as written) and "Gregory's House Rules" via a single settings toggle, with a `RuleResolutionHelper` that all consuming code calls instead of reading `KingdomSettings` booleans directly.

**Key capabilities:**

- **Single "Gregory" profile** now, with clear seam for multi-profile later (Decision 1, design-decisions.md)
- **RuleResolutionHelper** in `commonMain` — all camping/kingdom/hex systems call this instead of direct `KingdomSettings` boolean reads
- **Profile manager dialog** (`CrudApplication` subclass) — view, activate, deactivate, import, export
- **Schema-validated import/export** — versioned JSON with draft 2020-12 JSON Schema
- **Migration27** — adds new fields, seeds default Gregory profile, idempotent and safe to re-run
- **Profile activation is GM-triggered** (button click), not automatic on migration

**Relationship to gear-settings profile system:** The homebrew rules profile system defines the *resolution layer* for kingdom-rule fields. A future gear-settings profile system stores the *full settings state*. They share the same resolution helper, but differ in payload structure: gear settings use a nested grouped object; homebrew rules use a flat `rules` data class.

---

## Dependency and Sequencing Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│  PREREQS (all done via parent kanban tasks):                        │
│  ✅ Architecture patterns  (t_b83ba522)                              │
│  ✅ Design decisions        (t_16c242aa, t_27b234eb)                 │
│  ✅ Data model spec         (t_158fcaf9)                             │
│  ✅ UI spec + test plan     (t_6a24abe3)                             │
└───────────────────────────┬─────────────────────────────────────────┘
                            │
                            ▼
┌───────────────────────────────────────────────────────────────────┐
│  PHASE 1: Core Data Models (commonMain)                          │
│  Kotlin data classes + enum + resolver. No Foundry deps.         │
│  Tests: U1–U4 (40+ tests, pure `commonTest`)                      │
└───────────────────────────┬───────────────────────────────────────┘
                            │
                            ▼
┌───────────────────────────────────────────────────────────────────┐
│  PHASE 2: Foundry Settings Registration (jsMain)                 │
│  ClientSettings registration for homebrew preset/profile.         │
│  Tests: I4 (settings registration integration)                    │
└───────────────────────────┬───────────────────────────────────────┘
                            │
                            ▼
┌───────────────────────────────────────────────────────────────────┐
│  PHASE 3: Migration27 (jsMain)                                   │
│  New fields on KingdomData/CampingData, seed default profile.     │
│  Tests: compile check + existing migration pipeline               │
└───────────────────────────┬───────────────────────────────────────┘
                            │
                            ▼
┌───────────────────────────────────────────────────────────────────┐
│  PHASE 4: UI — Localization + Kingdom Sheet Tab (jsMain)         │
│  en.json keys, Homebrew tab, kingdom-homebrew.hbs partial.       │
│  Tests: compile check                                              │
└───────────────────────────┬───────────────────────────────────────┘
                            │
                            ▼
┌───────────────────────────────────────────────────────────────────┐
│  PHASE 5: Profile Manager Dialog (jsMain)                        │
│  HomebrewProfileManager (CrudApplication), wire to settings.     │
│  Tests: compile check                                              │
└───────────────────────────────────────────────────────────────────┘

  PHASE 6: Scattered Boolean Site Refactor (S1–S11) can begin
           in parallel with Phase 4+5 since it depends only on
           Phase 1 (resolver) and Phase 2 (active profile
           available in settings).
```

**Parallel work streams:** Phase 1 must complete first. Phases 2, 3, 4, 5, and 6 can proceed in parallel after Phase 1 since they interact through well-defined interfaces.

---

## Source of Truth

- Feature spec: `docs/feature-roadmap.md` section 9 (line 213)
- Design decisions: `docs/plans/2026-06-01-roadmap-design-decisions.md` (Decision 1)
- House rules values: `docs/house-rules.md` (House Rules sections)
- Data model spec (parent task t_158fcaf9): kanban workspace — see HomebrewRules, HomebrewRulesProfile data classes
- UI spec + test plan (parent task t_6a24abe3): kanban workspace — see S1–S11 site map

## Non-goals

- No multi-profile import/export UI at this stage (schema is versioned for future; export is clipboard/download only)
- No per-kingdom profile selection (world-scoped active profile only)
- No changes to non-homebrew kingdom/camping/hex systems beyond wiring through RuleResolutionHelper
- No UI for creating/editing custom profile content (Gregory profile is data, not UI-editable)
- Do not modify files unrelated to the homebrew profile feature
- No gear settings profile system yet (separate roadmap feature)

---

## Affected Files

### New files to create (19)

| # | File | Purpose | Phase |
|---|------|---------|-------|
| N1 | `src/commonMain/kotlin/.../homebrew/HomebrewPreset.kt` | `enum class HomebrewPreset { NONE, GREGORY }` with `ValueEnum` | 1 |
| N2 | `src/commonMain/kotlin/.../homebrew/HomebrewRules.kt` | Value object — all homebrew rule fields | 1 |
| N3 | `src/commonMain/kotlin/.../homebrew/HomebrewRulesProfile.kt` | Profile data class | 1 |
| N4 | `src/commonMain/kotlin/.../homebrew/HomebrewProfileRegistry.kt` | Registry data class | 1 |
| N5 | `src/commonMain/kotlin/.../homebrew/RuleResolutionHelper.kt` | Centralized resolver — all consuming code calls this | 1 |
| N6 | `src/jsMain/kotlin/.../homebrew/HomebrewRulesProfileDataModel.kt` | Foundry `DataModel` for settings registration | 2 |
| N7 | `src/jsMain/kotlin/.../homebrew/HomebrewProfileManager.kt` | `CrudApplication` subclass for profile management dialog | 5 |
| N8 | `src/jsMain/resources/applications/kingdom/homebrew-profile-manager.hbs` | Handlebars template for profile manager | 5 |
| N9 | `src/jsMain/resources/applications/kingdom/sections/kingdom-homebrew.hbs` | Partial — homebrew section in kingdom sheet | 4 |
| N10 | `src/jsMain/kotlin/.../migrations/migrations/Migration27.kt` | Adds fields to KingdomData/CampingData, seeds default profile | 3 |
| N11 | `src/commonMain/resources/schemas/homebrew-profile.json` | JSON Schema (draft 2020-12) for import validation | 7 |
| N12 | `src/commonTest/kotlin/.../homebrew/HomebrewPresetTest.kt` | Unit tests for enum serialization (U1) | 1 |
| N13 | `src/commonTest/kotlin/.../homebrew/HomebrewRulesTest.kt` | Unit tests for value object defaults (U2) | 1 |
| N14 | `src/commonTest/kotlin/.../homebrew/RuleResolutionHelperTest.kt` | Unit tests for resolver methods (U3) | 1 |
| N15 | `src/commonTest/kotlin/.../homebrew/HomebrewProfileSchemaTest.kt` | JSON Schema validation tests (U4) | 7 |
| N16 | `src/jsTest/kotlin/.../homebrew/SettingsRegistrationIntegrationTest.kt` | Settings registration test (I4) | 2 |
| N17 | `src/jsTest/kotlin/.../homebrew/CampingActivityCountIntegrationTest.kt` | Integration test I1 | 8 |
| N18 | `src/jsTest/kotlin/.../homebrew/RuinThresholdIntegrationTest.kt` | Integration test I2 | 8 |
| N19 | `src/jsTest/kotlin/.../homebrew/ImportExportIntegrationTest.kt` | Integration test I5 | 8 |

### Existing files to modify (6)

| # | File | Change | Phase |
|---|------|--------|-------|
| M1 | `src/jsMain/kotlin/.../settings/Pfrpg2eKingdomCampingWeatherSettings.kt` | Add `homebrewPreset` (enum), `homebrewRulesProfile` (DataModel), `homebrewRulesProfileRegistry` (Object) registrations in the `register()` block | 2 |
| M2 | `src/jsMain/kotlin/.../kingdom/dialogs/KingdomSettings.kt` | Add "Homebrew Profile" section: active profile display + "Manage Profiles..." button that opens `HomebrewProfileManager` | 5 |
| M3 | `src/jsMain/kotlin/.../kingdom/sheet/KingdomSheet.kt` | In the sheet's context/data-building, resolve active profile and expose `homebrewProfileName` / `homebrewProfileActive` fields; add "Homebrew" tab navigation entry | 4 |
| M4 | `src/jsMain/kotlin/.../kingdom/sheet/KingdomSheetDataModel.kt` | Add `homebrewProfileName: String?` and `homebrewProfileActive: Boolean` fields (in `companion object` or top-level) | 4 |
| M5 | `src/jsMain/resources/applications/kingdom/kingdom-sheet.hbs` | Add "Homebrew" tab `<li>` to nav bar + `<section data-tab="homebrew">` including the `kingdom-homebrew.hbs` partial | 4 |
| M6 | `src/jsMain/resources/lang/en.json` | Add 11 localization keys (see Localization Keys section below) | 4 |
| M7 | `src/jsMain/kotlin/.../migrations/Migrations.kt` | Add `Migration27()` to the migrations list | 3 |

### Scattered boolean sites to refactor (S1–S11)

Each currently reads a `KingdomSettings` boolean directly. Change to call `RuleResolutionHelper` instead, passing the active profile resolved from `game.settings.getObject("homebrewRulesProfileRegistry")`:

| Site | Rule Resolution Helper method | Setting replaced | Primary lookup term |
|------|-------------------------------|-----------------|-------------------|
| S1 | `useVanceAndKerensharaXp(profile)` | `vanceAndKerensharaXP` | search codebase for `vanceAndKerensharaXP` |
| S2 | `getRuinThreshold(profile)` | `ruinThreshold` | search codebase for `ruinThreshold` |
| S3 | `getCampingActivityCount(profile, partySize)` | Party-size-based camping activity count | search codebase for `campingActivityCount` or `activityCount` |
| S4 | `getLeadershipActivityCap(profile, hasTownhall)` | Leadership activity cap | search codebase for `leadershipActivityCap` or `activityCap` |
| S5 | `isRandomCombatSuppressedInClaimedHexes(profile)` | `noRandomCombatInClaimedHexes` | search codebase for `noRandomCombatInClaimedHexes` |
| S6 | `isStructureBonusCappedAtKingdomLevel(profile)` | `capStructureBonusAtKingdomLevel` | search codebase for `capStructureBonusAtKingdomLevel` |
| S7 | `canUpgradeNonCapitalSettlement(profile)` | "Improve Settlement" civic activity | search codebase for `canUpgradeNonCapital` or settlement upgrade logic |
| S8 | `getSettlementInfluenceRadius(profile)` | Settlement influence radius | search codebase for `settlementInfluenceRadius` |
| S9 | `doPavedStreetsReduceTravelCost(profile)` | Paved streets travel cost | search codebase for `pavedStreetsReduceTravelCost` |
| S10 | `canCapitalGrowOneSizeLarger(profile)` | `capitalCanGrowOneSizeLarger` | search codebase for `capitalCanGrowOneSizeLarger` |
| S11 | `getCultOfTheBloomEvents(profile)` | `cultOfTheBloomEvents` | search codebase for `cultOfTheBloomEvents` |

**Refactoring pattern for each site:**
1. At the call site, obtain the active profile:
   ```kotlin
   val registry = game.settings.getObject("homebrewRulesProfileRegistry") as? HomebrewProfileRegistry
   val activeProfile = RuleResolutionHelper.getActiveProfile(registry)
   ```
2. Replace direct `KingdomSettings.xxx` boolean reads with `RuleResolutionHelper.getXxx(activeProfile, ...)`.
3. Remove the old `KingdomSettings` import if no longer used in that file.

---

## Data Models

### HomebrewPreset (commonMain)

```kotlin
package at.posselt.pfrpg2e.homebrew

import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.fromCamelCase

enum class HomebrewPreset(override val value: String) : ValueEnum {
    NONE("none"),
    GREGORY("gregory");

    companion object {
        fun fromString(value: String) = fromCamelCase<HomebrewPreset>(value)
    }
}
```

Note: `HomebrewPreset` must also implement `Translatable` to be used with `registerEnum<T>()`. Add an `i18nKey` property and implement `Translatable`:

```kotlin
enum class HomebrewPreset(
    override val value: String,
    override val i18nKey: String,
) : ValueEnum, Translatable {
    NONE("none", "pf2e-kingmaker-tools.enums.homebrewPreset.none"),
    GREGORY("gregory", "pf2e-kingmaker-tools.enums.homebrewPreset.gregory");

    companion object {
        fun fromString(value: String) = fromCamelCase<HomebrewPreset>(value)
    }
}
```

Add the corresponding i18n keys to `en.json`:
```json
"pf2e-kingmaker-tools.enums.homebrewPreset.none": "None (RAW)",
"pf2e-kingmaker-tools.enums.homebrewPreset.gregory": "Gregory's House Rules"
```

### HomebrewRules (commonMain)

```kotlin
package at.posselt.pfrpg2e.homebrew

data class HomebrewRules(
    val useVanceAndKerenshara: Boolean = false,
    val ruinThreshold: Int = 10,
    val eventDc: Int = 15,
    val eventDcStep: Int = 0,
    val leadershipActivityCap: Int = 6,
    val leadershipActivityCapWithTownhall: Int = 8,
    val canUpgradeNonCapital: Boolean = false,
    val campingActivityCountByPartySize: Boolean = false,
    val noRandomCombatInClaimedHexes: Boolean = false,
    val capStructureBonusAtKingdomLevel: Boolean = false,
    val capitalCanGrowOneSizeLarger: Boolean = false,
    val cultOfTheBloomEvents: Boolean = false,
    val travelCostRiverNoBridgeAdditional: Int = 0,
    val pavedStreetsReduceTravelCost: Boolean = false,
    val settlementInfluenceRadius: Int = 0,
) {
    companion object {
        fun none() = HomebrewRules()
        fun gregory() = HomebrewRules(
            useVanceAndKerenshara = true,
            ruinThreshold = 5,
            eventDc = 5,
            leadershipActivityCap = 8,
            leadershipActivityCapWithTownhall = 12,
            canUpgradeNonCapital = true,
            campingActivityCountByPartySize = true,
            noRandomCombatInClaimedHexes = true,
            capStructureBonusAtKingdomLevel = true,
            capitalCanGrowOneSizeLarger = true,
            cultOfTheBloomEvents = true,
            travelCostRiverNoBridgeAdditional = 1,
            pavedStreetsReduceTravelCost = true,
            settlementInfluenceRadius = 1,
        )
    }
}
```

### HomebrewRulesProfile (commonMain)

```kotlin
package at.posselt.pfrpg2e.homebrew

data class HomebrewRulesProfile(
    val id: String,
    val name: String,
    val version: Int = 1,
    val isActive: Boolean = false,
    val createdAt: String,
    val updatedAt: String,
    val description: String? = null,
    val rules: HomebrewRules,
)
```

### HomebrewProfileRegistry (commonMain)

```kotlin
package at.posselt.pfrpg2e.homebrew

data class HomebrewProfileRegistry(
    val version: Int = 1,
    val activeProfileId: String? = null,
    val profiles: List<HomebrewRulesProfile> = emptyList(),
)
```

### RuleResolutionHelper (commonMain)

```kotlin
package at.posselt.pfrpg2e.homebrew

object RuleResolutionHelper {
    fun getRuinThreshold(profile: HomebrewRulesProfile?): Int =
        profile?.rules?.ruinThreshold ?: HomebrewRules.none().ruinThreshold

    fun getEventDc(profile: HomebrewRulesProfile?): Int =
        profile?.rules?.eventDc ?: HomebrewRules.none().eventDc

    fun getEventDcStep(profile: HomebrewRulesProfile?): Int =
        profile?.rules?.eventDcStep ?: HomebrewRules.none().eventDcStep

    fun getLeadershipActivityCap(profile: HomebrewRulesProfile?, hasTownhallOrHigher: Boolean): Int {
        val rules = profile?.rules ?: return if (hasTownhallOrHigher) 8 else 6
        return if (hasTownhallOrHigher) rules.leadershipActivityCapWithTownhall else rules.leadershipActivityCap
    }

    fun getCampingActivityCount(profile: HomebrewRulesProfile?, partySize: Int): Int {
        val rules = profile?.rules ?: return 4 // RAW default: 4
        return if (rules.campingActivityCountByPartySize) partySize else 4
    }

    fun isRandomCombatSuppressedInClaimedHexes(profile: HomebrewRulesProfile?): Boolean =
        profile?.rules?.noRandomCombatInClaimedHexes ?: false

    fun isStructureBonusCappedAtKingdomLevel(profile: HomebrewRulesProfile?): Boolean =
        profile?.rules?.capStructureBonusAtKingdomLevel ?: false

    fun canUpgradeNonCapitalSettlement(profile: HomebrewRulesProfile?): Boolean =
        profile?.rules?.canUpgradeNonCapital ?: false

    fun canCapitalGrowOneSizeLarger(profile: HomebrewRulesProfile?): Boolean =
        profile?.rules?.capitalCanGrowOneSizeLarger ?: false

    fun useVanceAndKerensharaXp(profile: HomebrewRulesProfile?): Boolean =
        profile?.rules?.useVanceAndKerenshara ?: false

    fun getCultOfTheBloomEvents(profile: HomebrewRulesProfile?): Boolean =
        profile?.rules?.cultOfTheBloomEvents ?: false

    fun getTravelCostRiverNoBridgeAdditional(profile: HomebrewRulesProfile?): Int =
        profile?.rules?.travelCostRiverNoBridgeAdditional ?: 0

    fun doPavedStreetsReduceTravelCost(profile: HomebrewRulesProfile?): Boolean =
        profile?.rules?.pavedStreetsReduceTravelCost ?: false

    fun getSettlementInfluenceRadius(profile: HomebrewRulesProfile?): Int =
        profile?.rules?.settlementInfluenceRadius ?: 0

    fun getActiveProfile(registry: HomebrewProfileRegistry?): HomebrewRulesProfile? {
        if (registry == null) return null
        return registry.profiles.find { it.id == registry.activeProfileId }
    }
}
```

---

## Localization Keys

Add to `src/jsMain/resources/lang/en.json`:

```json
"pf2e-kingmaker-tools.homebrewProfile.title": "Homebrew Rules Profile",
"pf2e-kingmaker-tools.homebrewProfile.active": "Active Profile",
"pf2e-kingmaker-tools.homebrewProfile.manage": "Manage Profiles...",
"pf2e-kingmaker-tools.homebrewProfile.import": "Import Profile",
"pf2e-kingmaker-tools.homebrewProfile.export": "Export Profile",
"pf2e-kingmaker-tools.homebrewProfile.importSuccess": "Profile imported successfully",
"pf2e-kingmaker-tools.homebrewProfile.importError": "Invalid profile: {reason}",
"pf2e-kingmaker-tools.homebrewProfile.activate": "Activate",
"pf2e-kingmaker-tools.homebrewProfile.deactivate": "Deactivate",
"pf2e-kingmaker-tools.enums.homebrewPreset.none": "None (RAW)",
"pf2e-kingmaker-tools.enums.homebrewPreset.gregory": "Gregory's House Rules"
```

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
| Run common unit tests | `commonTest --tests "at.posselt.pfrpg2e.homebrew.*Test"` |
| Run JS integration tests | `jsTest --tests "at.posselt.pfrpg2e.homebrew.*IntegrationTest"` |
| Validate JSON data | `validateStructures validateKingdomActivities` |

---

## Implementation Tasks

### Phase 1: Core Data Models (commonMain)

---

#### Task 1: Add failing test for HomebrewPreset enum

**Objective:** Lock in enum serialization and Translatable behavior before implementation.

**Files:**
- Create: `src/commonTest/kotlin/at/posselt/pfrpg2e/homebrew/HomebrewPresetTest.kt` (N12)

**Step 1: Write failing test**

```kotlin
package at.posselt.pfrpg2e.homebrew

import kotlin.test.Test
import kotlin.test.assertEquals

class HomebrewPresetTest {
    @Test
    fun `NONE serializes to lowercase string`() {
        assertEquals("none", HomebrewPreset.NONE.value)
    }

    @Test
    fun `GREGORY serializes to lowercase string`() {
        assertEquals("gregory", HomebrewPreset.GREGORY.value)
    }

    @Test
    fun `fromString parses gregory`() {
        assertEquals(HomebrewPreset.GREGORY, HomebrewPreset.fromString("gregory"))
    }

    @Test
    fun `fromString parses none`() {
        assertEquals(HomebrewPreset.NONE, HomebrewPreset.fromString("none"))
    }
}
```

**Step 2: Run test to verify failure**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew commonTest --tests "at.posselt.pfrpg2e.homebrew.HomebrewPresetTest"
```
Expected: FAIL — `HomebrewPreset` not found

---

#### Task 2: Implement HomebrewPreset enum

**Objective:** Create the `HomebrewPreset` enum class with `ValueEnum` + `Translatable`.

**Files:**
- Create: `src/commonMain/kotlin/at/posselt/pfrpg2e/homebrew/HomebrewPreset.kt` (N1)

Use the data model defined in the Data Models section above. Key: implement both `ValueEnum` and `Translatable` so it works with `registerEnum<T>()`.

**Step 2: Run test to verify pass**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew commonTest --tests "at.posselt.pfrpg2e.homebrew.HomebrewPresetTest"
```
Expected: PASS

**Step 3: Commit**

```bash
git add src/commonMain/kotlin/at/posselt/pfrpg2e/homebrew/HomebrewPreset.kt \
        src/commonTest/kotlin/at/posselt/pfrpg2e/homebrew/HomebrewPresetTest.kt
git commit -m "feat(homebrew): add HomebrewPreset enum with ValueEnum + Translatable"
```

---

#### Task 3: Add failing test for HomebrewRules data class

**Objective:** Lock in field values and defaults for Gregory vs NONE presets.

**Files:**
- Create: `src/commonTest/kotlin/at/posselt/pfrpg2e/homebrew/HomebrewRulesTest.kt` (N13)

**Step 1: Write failing test**

```kotlin
package at.posselt.pfrpg2e.homebrew

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomebrewRulesTest {
    @Test
    fun `NONE preset returns RAW defaults`() {
        val rules = HomebrewRules.none()
        assertFalse(rules.useVanceAndKerenshara)
        assertEquals(10, rules.ruinThreshold)
        assertEquals(15, rules.eventDc)
        assertEquals(0, rules.eventDcStep)
        assertEquals(6, rules.leadershipActivityCap)
        assertEquals(8, rules.leadershipActivityCapWithTownhall)
        assertFalse(rules.canUpgradeNonCapital)
        assertFalse(rules.campingActivityCountByPartySize)
        assertFalse(rules.noRandomCombatInClaimedHexes)
        assertFalse(rules.capStructureBonusAtKingdomLevel)
        assertEquals(0, rules.travelCostRiverNoBridgeAdditional)
        assertFalse(rules.pavedStreetsReduceTravelCost)
        assertEquals(0, rules.settlementInfluenceRadius)
    }

    @Test
    fun `GREGORY preset values match house-rules`() {
        val rules = HomebrewRules.gregory()
        assertTrue(rules.useVanceAndKerenshara)
        assertEquals(5, rules.ruinThreshold)
        assertEquals(5, rules.eventDc)
        assertEquals(8, rules.leadershipActivityCap)
        assertEquals(12, rules.leadershipActivityCapWithTownhall)
        assertTrue(rules.canUpgradeNonCapital)
        assertTrue(rules.campingActivityCountByPartySize)
        assertTrue(rules.noRandomCombatInClaimedHexes)
        assertTrue(rules.capStructureBonusAtKingdomLevel)
        assertTrue(rules.capitalCanGrowOneSizeLarger)
        assertTrue(rules.cultOfTheBloomEvents)
        assertEquals(1, rules.travelCostRiverNoBridgeAdditional)
        assertTrue(rules.pavedStreetsReduceTravelCost)
        assertEquals(1, rules.settlementInfluenceRadius)
    }

    @Test
    fun `default constructor returns RAW-equivalent values`() {
        val rules = HomebrewRules()
        assertFalse(rules.useVanceAndKerenshara)
        assertEquals(10, rules.ruinThreshold)
    }
}
```

**Step 2: Run test to verify failure**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew commonTest --tests "at.posselt.pfrpg2e.homebrew.HomebrewRulesTest"
```
Expected: FAIL

---

#### Task 4: Implement HomebrewRules data class

**Objective:** Create the `HomebrewRules` value object with factory methods for NONE and GREGORY.

**Files:**
- Create: `src/commonMain/kotlin/at/posselt/pfrpg2e/homebrew/HomebrewRules.kt` (N2)

Use the data model defined in the Data Models section above.

**Step 2: Run test to verify pass**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew commonTest --tests "at.posselt.pfrpg2e.homebrew.HomebrewRulesTest"
```
Expected: PASS

**Step 3: Commit**

```bash
git add src/commonMain/kotlin/at/posselt/pfrpg2e/homebrew/HomebrewRules.kt \
        src/commonTest/kotlin/at/posselt/pfrpg2e/homebrew/HomebrewRulesTest.kt
git commit -m "feat(homebrew): add HomebrewRules value object with NONE and GREGORY presets"
```

---

#### Task 5: Add failing test for RuleResolutionHelper

**Objective:** Lock in that every resolve method returns correct Gregory values and RAW defaults.

**Files:**
- Create: `src/commonTest/kotlin/at/posselt/pfrpg2e/homebrew/RuleResolutionHelperTest.kt` (N14)

**Step 1: Write failing test** — test every resolve method for both profiles:

```kotlin
package at.posselt.pfrpg2e.homebrew

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RuleResolutionHelperTest {
    private val gregoryProfile = HomebrewRulesProfile(
        id = "gregory-v1",
        name = "Gregory's House Rules",
        isActive = true,
        createdAt = "2026-06-01T00:00:00Z",
        updatedAt = "2026-06-01T00:00:00Z",
        rules = HomebrewRules.gregory(),
    )

    @Test fun `ruin threshold - Gregory returns 5`() =
        assertEquals(5, RuleResolutionHelper.getRuinThreshold(gregoryProfile))
    @Test fun `ruin threshold - NONE returns 10`() =
        assertEquals(10, RuleResolutionHelper.getRuinThreshold(null))

    @Test fun `eventDc - Gregory returns 5`() =
        assertEquals(5, RuleResolutionHelper.getEventDc(gregoryProfile))
    @Test fun `eventDc - NONE returns 15`() =
        assertEquals(15, RuleResolutionHelper.getEventDc(null))

    @Test fun `eventDcStep - Gregory returns 0`() =
        assertEquals(0, RuleResolutionHelper.getEventDcStep(gregoryProfile))
    @Test fun `eventDcStep - NONE returns 0`() =
        assertEquals(0, RuleResolutionHelper.getEventDcStep(null))

    @Test fun `camping activity count - Gregory uses party size`() {
        assertEquals(3, RuleResolutionHelper.getCampingActivityCount(gregoryProfile, 3))
        assertEquals(5, RuleResolutionHelper.getCampingActivityCount(gregoryProfile, 5))
    }
    @Test fun `camping activity count - NONE returns 4`() {
        assertEquals(4, RuleResolutionHelper.getCampingActivityCount(null, 3))
        assertEquals(4, RuleResolutionHelper.getCampingActivityCount(null, 5))
    }

    @Test fun `random combat suppressed - Gregory true`() =
        assertTrue(RuleResolutionHelper.isRandomCombatSuppressedInClaimedHexes(gregoryProfile))
    @Test fun `random combat suppressed - NONE false`() =
        assertFalse(RuleResolutionHelper.isRandomCombatSuppressedInClaimedHexes(null))

    @Test fun `structure bonus cap - Gregory true`() =
        assertTrue(RuleResolutionHelper.isStructureBonusCappedAtKingdomLevel(gregoryProfile))
    @Test fun `structure bonus cap - NONE false`() =
        assertFalse(RuleResolutionHelper.isStructureBonusCappedAtKingdomLevel(null))

    @Test fun `can upgrade non-capital - Gregory true`() =
        assertTrue(RuleResolutionHelper.canUpgradeNonCapitalSettlement(gregoryProfile))
    @Test fun `can upgrade non-capital - NONE false`() =
        assertFalse(RuleResolutionHelper.canUpgradeNonCapitalSettlement(null))

    @Test fun `leadership activity cap - Gregory base 8 townhall 12`() {
        assertEquals(8, RuleResolutionHelper.getLeadershipActivityCap(gregoryProfile, false))
        assertEquals(12, RuleResolutionHelper.getLeadershipActivityCap(gregoryProfile, true))
    }
    @Test fun `leadership activity cap - NONE base 6 townhall 8`() {
        assertEquals(6, RuleResolutionHelper.getLeadershipActivityCap(null, false))
        assertEquals(8, RuleResolutionHelper.getLeadershipActivityCap(null, true))
    }

    @Test fun `vance and kerenshara XP - Gregory true`() =
        assertTrue(RuleResolutionHelper.useVanceAndKerensharaXp(gregoryProfile))
    @Test fun `vance and kerenshara XP - NONE false`() =
        assertFalse(RuleResolutionHelper.useVanceAndKerensharaXp(null))

    @Test fun `cult of the bloom - Gregory true`() =
        assertTrue(RuleResolutionHelper.getCultOfTheBloomEvents(gregoryProfile))
    @Test fun `cult of the bloom - NONE false`() =
        assertFalse(RuleResolutionHelper.getCultOfTheBloomEvents(null))

    @Test fun `capital grow larger - Gregory true`() =
        assertTrue(RuleResolutionHelper.canCapitalGrowOneSizeLarger(gregoryProfile))
    @Test fun `capital grow larger - NONE false`() =
        assertFalse(RuleResolutionHelper.canCapitalGrowOneSizeLarger(null))

    @Test fun `paved streets reduce cost - Gregory true`() =
        assertTrue(RuleResolutionHelper.doPavedStreetsReduceTravelCost(gregoryProfile))
    @Test fun `paved streets reduce cost - NONE false`() =
        assertFalse(RuleResolutionHelper.doPavedStreetsReduceTravelCost(null))

    @Test fun `travel cost river additional - Gregory returns 1`() =
        assertEquals(1, RuleResolutionHelper.getTravelCostRiverNoBridgeAdditional(gregoryProfile))
    @Test fun `travel cost river additional - NONE returns 0`() =
        assertEquals(0, RuleResolutionHelper.getTravelCostRiverNoBridgeAdditional(null))

    @Test fun `settlement influence radius - Gregory returns 1`() =
        assertEquals(1, RuleResolutionHelper.getSettlementInfluenceRadius(gregoryProfile))
    @Test fun `settlement influence radius - NONE returns 0`() =
        assertEquals(0, RuleResolutionHelper.getSettlementInfluenceRadius(null))

    @Test fun `getActiveProfile returns correct profile`() {
        val registry = HomebrewProfileRegistry(
            version = 1,
            activeProfileId = "gregory-v1",
            profiles = listOf(gregoryProfile),
        )
        assertEquals(gregoryProfile, RuleResolutionHelper.getActiveProfile(registry))
    }

    @Test fun `getActiveProfile returns null for empty registry`() {
        val registry = HomebrewProfileRegistry()
        assertEquals(null, RuleResolutionHelper.getActiveProfile(registry))
    }

    @Test fun `getActiveProfile returns null for null registry`() {
        assertEquals(null, RuleResolutionHelper.getActiveProfile(null))
    }
}
```

**Step 2: Run test to verify failure**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew commonTest --tests "at.posselt.pfrpg2e.homebrew.RuleResolutionHelperTest"
```
Expected: FAIL

---

#### Task 6: Implement RuleResolutionHelper + supporting data classes

**Objective:** Create the centralized rule resolver object and supporting data classes.

**Files:**
- Create: `src/commonMain/kotlin/at/posselt/pfrpg2e/homebrew/RuleResolutionHelper.kt` (N5)
- Create: `src/commonMain/kotlin/at/posselt/pfrpg2e/homebrew/HomebrewRulesProfile.kt` (N3)
- Create: `src/commonMain/kotlin/at/posselt/pfrpg2e/homebrew/HomebrewProfileRegistry.kt` (N4)

Use the data models defined in the Data Models section above.

**Step 2: Run test to verify pass**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew commonTest --tests "at.posselt.pfrpg2e.homebrew.RuleResolutionHelperTest"
```
Expected: PASS

**Step 3: Commit**

```bash
git add src/commonMain/kotlin/at/posselt/pfrpg2e/homebrew/RuleResolutionHelper.kt \
        src/commonMain/kotlin/at/posselt/pfrpg2e/homebrew/HomebrewRulesProfile.kt \
        src/commonMain/kotlin/at/posselt/pfrpg2e/homebrew/HomebrewProfileRegistry.kt \
        src/commonTest/kotlin/at/posselt/pfrpg2e/homebrew/RuleResolutionHelperTest.kt
git commit -m "feat(homebrew): add RuleResolutionHelper and profile data models"
```

---

### Phase 2: Foundry Settings Registration (jsMain)

---

#### Task 7: Register homebrew settings in Pfrpg2eKingdomCampingWeatherSettings

**Objective:** Add `homebrewPreset` enum setting, `homebrewRulesProfile` DataModel setting, and `homebrewRulesProfileRegistry` Object setting to the module settings.

**Files:**
- Modify: `src/jsMain/kotlin/at/posselt/pfrpg2e/settings/Pfrpg2eKingdomCampingWeatherSettings.kt` (M1)
- Create: `src/jsMain/kotlin/at/posselt/pfrpg2e/homebrew/HomebrewRulesProfileDataModel.kt` (N6)

**Step 1: Create HomebrewRulesProfileDataModel**

```kotlin
package at.posselt.pfrpg2e.homebrew

import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.abstract.DocumentConstructionContext
import com.foundryvtt.core.data.dsl.buildSchema

class HomebrewRulesProfileDataModel(
    value: AnyObject,
    options: DocumentConstructionContext?,
) : DataModel(value, options) {
    companion object {
        @JsStatic
        fun defineSchema() = buildSchema {
            string("id")
            string("name")
            int("version")
            boolean("isActive")
            string("createdAt")
            string("updatedAt")
            string("description", nullable = true)
            schema("rules") {
                boolean("useVanceAndKerenshara")
                int("ruinThreshold")
                int("eventDc")
                int("eventDcStep")
                int("leadershipActivityCap")
                int("leadershipActivityCapWithTownhall")
                boolean("canUpgradeNonCapital")
                boolean("campingActivityCountByPartySize")
                boolean("noRandomCombatInClaimedHexes")
                boolean("capStructureBonusAtKingdomLevel")
                boolean("capitalCanGrowOneSizeLarger")
                boolean("cultOfTheBloomEvents")
                int("travelCostRiverNoBridgeAdditional")
                boolean("pavedStreetsReduceTravelCost")
                int("settlementInfluenceRadius")
            }
        }
    }
}
```

**Step 2: Add setting registrations**

In the `register()` block of `Pfrpg2eKingdomCampingWeatherSettings` (alongside other `register`/`registerEnum`/`registerScalar` calls), add:

```kotlin
// Homebrew rules preset selector (world scope, GM-only)
game.settings.registerEnum<HomebrewPreset>(
    key = "homebrewPreset",
    hint = "Select active homebrew rules profile. None = RAW rules.",
    default = HomebrewPreset.NONE,
)

// Active homebrew rules profile data
game.settings.registerDataModel<HomebrewRulesProfileDataModel>(
    key = "homebrewRulesProfile",
    name = t("pf2e-kingmaker-tools.homebrewProfile.active"),
)

// Profile registry (stores all profiles, including future additions)
game.settings.registerScalar<Any>(
    key = "homebrewRulesProfileRegistry",
    name = "Homebrew Profile Registry",
    default = emptyMap<Any, Any>(),
    hidden = true,
)
```

**Important:** The `registerEnum` call uses the existing `registerEnum<T>()` extension function defined in the same file (line 109). It requires `T : Enum<T>, T : Translatable, T : ValueEnum`. The `registerDataModel` call uses the existing extension at line 37. The `registerScalar` call uses the extension at line 83.

**Step 3: Verify compilation**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew compileKotlinJs
```
Expected: PASS

**Step 4: Commit**

```bash
git add src/jsMain/kotlin/at/posselt/pfrpg2e/settings/Pfrpg2eKingdomCampingWeatherSettings.kt \
        src/jsMain/kotlin/at/posselt/pfrpg2e/homebrew/HomebrewRulesProfileDataModel.kt
git commit -m "feat(homebrew): register homebrew settings in module settings"
```

---

#### Task 8: Add settings registration integration test

**Objective:** Verify the `homebrewPreset` setting appears correctly in Foundry module settings.

**Files:**
- Create: `src/jsTest/kotlin/at/posselt/pfrpg2e/homebrew/SettingsRegistrationIntegrationTest.kt` (N16)

**Step 1: Write test** following the existing `unsafeJso {}` mock pattern used in the project for settings tests. The test should:
1. Create a mock `game.settings` with `unsafeJso {}`
2. Call `Pfrpg2eKingdomCampingWeatherSettings.register()`
3. Verify the `homebrewPreset` key is registered with default `"none"`

**Step 2: Run test**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew jsTest --tests "at.posselt.pfrpg2e.homebrew.SettingsRegistrationIntegrationTest"
```
Expected: PASS

**Step 3: Commit**

```bash
git add src/jsTest/kotlin/at/posselt/pfrpg2e/homebrew/SettingsRegistrationIntegrationTest.kt
git commit -m "test(homebrew): add settings registration integration test"
```

---

### Phase 3: Migration27

---

#### Task 9: Create Migration27

**Objective:** Add migration that creates new fields on KingdomData/CampingData and seeds the default Gregory profile.

**Files:**
- Create: `src/jsMain/kotlin/at/posselt/pfrpg2e/migrations/migrations/Migration27.kt` (N10)
- Modify: `src/jsMain/kotlin/at/posselt/pfrpg2e/migrations/Migrations.kt` (M7)

**Step 1: Create Migration27**

```kotlin
package at.posselt.pfrpg2e.migrations.migrations

import at.posselt.pfrpg2e.homebrew.HomebrewRules
import at.posselt.pfrpg2e.homebrew.HomebrewRulesProfile
import at.posselt.pfrpg2e.settings.pfrpg2eKingdomCampingWeather
import com.foundryvtt.core.Game

class Migration27 : Migration(27) {

    override suspend fun migrateKingdom(game: Game, kingdom: dynamic) {
        if (kingdom.homebrewStructures == null) {
            kingdom.homebrewStructures = emptyArray<Any>()
        }
        if (kingdom.profileId == null) {
            kingdom.profileId = js("undefined")
        }
        if (kingdom.profileAppliedAt == null) {
            kingdom.profileAppliedAt = js("undefined")
        }
    }

    override suspend fun migrateCamping(game: Game, camping: dynamic) {
        if (camping.homebrewCampingActivityBlacklist == null) {
            camping.homebrewCampingActivityBlacklist = emptyArray<String>()
        }
        if (camping.profileAppliedAt == null) {
            camping.profileAppliedAt = js("undefined")
        }
    }

    override suspend fun migrateOther(game: Game) {
        // Seed default Gregory profile if registry is empty
        val existing = game.settings.getObject("homebrewRulesProfileRegistry")
        if (existing == null || (existing.asDynamic().profiles as? Array<*>)?.isEmpty() != false) {
            val now = Date().toISOString()
            val gregoryProfile = HomebrewRulesProfile(
                id = "gregory-v1",
                name = "Gregory's House Rules",
                version = 1,
                isActive = false,
                createdAt = now,
                updatedAt = now,
                description = "Pre-configured house rules for Gregory's campaign",
                rules = HomebrewRules.gregory(),
            )
            val registry = mapOf(
                "version" to 1,
                "activeProfileId" to null,
                "profiles" to arrayOf(gregoryProfile),
            )
            game.settings.setObject("homebrewRulesProfileRegistry", registry)
        }
    }
}
```

**Step 2: Register in Migrations.kt**

Add `Migration27()` to the migrations list alongside Migration17–Migration26:

```kotlin
private val migrations = listOf(
    Migration17(),
    // ... existing migrations ...
    Migration26(),
    Migration27(),
)
```

**Step 3: Verify compilation**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew compileKotlinJs compileTestKotlinJs
```
Expected: PASS

**Step 4: Commit**

```bash
git add src/jsMain/kotlin/at/posselt/pfrpg2e/migrations/migrations/Migration27.kt \
        src/jsMain/kotlin/at/posselt/pfrpg2e/migrations/Migrations.kt
git commit -m "feat(homebrew): add Migration27 with new fields and default Gregory profile seed"
```

---

### Phase 4: UI — Localization + Kingdom Sheet Homebrew Tab

---

#### Task 10: Add localization keys

**Objective:** Add all homebrew localization keys to en.json.

**Files:**
- Modify: `src/jsMain/resources/lang/en.json` (M6)

**Step 1: Add keys** — use the Localization Keys section above.

**Step 2: Verify compilation**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew compileKotlinJs
```
Expected: PASS

**Step 3: Commit**

```bash
git add src/jsMain/resources/lang/en.json
git commit -m "feat(homebrew): add localization keys for profile system"
```

---

#### Task 11: Add homebrew fields to KingdomSheetDataModel

**Objective:** Expose `homebrewProfileName` and `homebrewProfileActive` for template binding.

**Files:**
- Modify: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/KingdomSheetDataModel.kt` (M4)

**Step 1: Add fields**

Add to the `defineSchema()` block:

```kotlin
string("homebrewProfileName", nullable = true)
boolean("homebrewProfileActive")
```

**Step 2: Populate in KingdomSheet.kt**

In the sheet's context/data-building method (where other fields are resolved from game settings), add:

```kotlin
val registry = game.settings.getObject("homebrewRulesProfileRegistry") as? HomebrewProfileRegistry
val activeProfile = RuleResolutionHelper.getActiveProfile(registry)
// Set homebrewProfileName = activeProfile?.name
// Set homebrewProfileActive = activeProfile?.isActive == true
```

**Step 3: Verify compilation**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew compileKotlinJs
```
Expected: PASS

**Step 4: Commit**

```bash
git add src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/KingdomSheetDataModel.kt \
        src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/KingdomSheet.kt
git commit -m "feat(homebrew): expose profile fields in kingdom sheet data model"
```

---

#### Task 12: Add Homebrew tab to kingdom sheet

**Objective:** Add a "Homebrew" tab to the kingdom sheet navigation and create the section partial.

**Files:**
- Modify: `src/jsMain/resources/applications/kingdom/kingdom-sheet.hbs` (M5)
- Create: `src/jsMain/resources/applications/kingdom/sections/kingdom-homebrew.hbs` (N9)

**Step 1: Add tab to kingdom-sheet.hbs**

Add alongside existing tab `<li>` entries:

```handlebars
<li class="item" data-tab="homebrew">{{localize "pf2e-kingmaker-tools.homebrewProfile.title"}}</li>
```

And the corresponding tab content section:

```handlebars
<section class="tab" data-tab="homebrew">
    {{> "applications/kingdom/sections/kingdom-homebrew.hbs"}}
</section>
```

**Step 2: Create kingdom-homebrew.hbs partial**

```handlebars
<div class="homebrew-profile-section">
    <h3>{{localize "pf2e-kingmaker-tools.homebrewProfile.active"}}</h3>
    <div class="profile-info">
        <span class="profile-name">
            {{#if homebrewProfileActive}}
                {{homebrewProfileName}}
            {{else}}
                {{localize "pf2e-kingmaker-tools.enums.homebrewPreset.none"}}
            {{/if}}
        </span>
        <button type="button" class="manage-profiles-btn">
            {{localize "pf2e-kingmaker-tools.homebrewProfile.manage"}}
        </button>
    </div>
</div>
```

**Step 3: Wire the "Manage Profiles..." button**

In `KingdomSheet.kt`, add a click handler for `.manage-profiles-btn` that opens `HomebrewProfileManager`.

**Step 4: Verify compilation**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew compileKotlinJs
```
Expected: PASS

**Step 5: Commit**

```bash
git add src/jsMain/resources/applications/kingdom/kingdom-sheet.hbs \
        src/jsMain/resources/applications/kingdom/sections/kingdom-homebrew.hbs \
        src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/KingdomSheet.kt
git commit -m "feat(homebrew): add Homebrew tab to kingdom sheet"
```

---

### Phase 5: Profile Manager Dialog

---

#### Task 13: Create HomebrewProfileManager dialog

**Objective:** Build the `CrudApplication` subclass for managing profiles (view, activate, deactivate, import, export).

**Files:**
- Create: `src/jsMain/kotlin/at/posselt/pfrpg2e/homebrew/HomebrewProfileManager.kt` (N7)
- Create: `src/jsMain/resources/applications/kingdom/homebrew-profile-manager.hbs` (N8)
- Modify: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/dialogs/KingdomSettings.kt` (M2)

**Step 1: Implement HomebrewProfileManager**

Key features:
- Extends `CrudApplication` (existing pattern from `CrudApplication.kt`)
- Displays list of profiles from the registry with active status
- "Activate" button on each profile (sets `activeProfileId` in registry, sets `isActive` flag)
- "Deactivate" button on active profile
- "Export" button: serializes active profile to JSON, triggers download
- "Import" button: opens text input, validates against `homebrew-profile.json` schema, adds to registry
- GM-only visibility (inherited from `CrudApplication` with `restricted = true` or equivalent)

**Step 2: Implement the Handlebars template**

Standard CrudApplication layout with:
- Profile list (name, version, active badge)
- Action buttons (activate, deactivate, import, export)

**Step 3: Wire the "Manage Profiles..." button in KingdomSettings.kt**

In the settings dialog, add a section that displays the active profile name and a "Manage Profiles..." button that opens `HomebrewProfileManager`.

**Step 4: Verify compilation**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew compileKotlinJs
```
Expected: PASS

**Step 5: Commit**

```bash
git add src/jsMain/kotlin/at/posselt/pfrpg2e/homebrew/HomebrewProfileManager.kt \
        src/jsMain/resources/applications/kingdom/homebrew-profile-manager.hbs \
        src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/dialogs/KingdomSettings.kt
git commit -m "feat(homebrew): add profile manager dialog and wire to kingdom settings"
```

---

### Phase 6: Refactor Scattered Boolean Sites (S1–S11)

---

#### Task 14: Refactor S1–S4 to use RuleResolutionHelper

**Objective:** Replace direct `KingdomSettings.boolean` reads in the first 4 scattered sites.

**Step 1: For each site:**
1. Find the current code reading the `KingdomSettings` boolean (use the lookup terms in the Scattered Boolean Sites table)
2. At the top of the function/method, resolve the active profile:
   ```kotlin
   val registry = game.settings.getObject("homebrewRulesProfileRegistry") as? HomebrewProfileRegistry
   val activeProfile = RuleResolutionHelper.getActiveProfile(registry)
   ```
3. Replace the direct boolean read with the corresponding `RuleResolutionHelper.getXxx(activeProfile, ...)` call
4. Remove the old `KingdomSettings` import if no longer used in that file

**Step 2: Verify compilation**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew compileKotlinJs compileTestKotlinJs
```
Expected: PASS

---

#### Task 15: Refactor S5–S8 to use RuleResolutionHelper

**Objective:** Replace reads in sites S5 (random combat), S6 (structure bonus cap), S7 (upgrade non-capital), S8 (settlement radius).

Same pattern as Task 14.

---

#### Task 16: Refactor S9–S11 to use RuleResolutionHelper

**Objective:** Final batch — S9 (paved streets), S10 (capital growth), S11 (cult of bloom).

Same pattern as Task 14.

---

#### Task 17: Verify no direct KingdomSettings boolean reads remain for homebrew fields

**Objective:** Confirm all 11 sites are refactored.

**Step 1: Search for remaining references**

```bash
cd /home/grego/code/pf2e-kingmaker-tools && grep -rn \
  "ruinThreshold\|vanceAndKerensharaXP\|noRandomCombatInClaimedHexes\|capStructureBonusAtKingdomLevel\|capitalCanGrowOneSizeLarger\|cultOfTheBloomEvents\|leadershipActivityCap\|campingActivityCount\|settlementInfluenceRadius\|pavedStreetsReduceTravelCost\|canUpgradeNonCapital" \
  src/jsMain/kotlin/ --include="*.kt" | grep -v "RuleResolutionHelper\|homebrewProfile\|defineSchema\|Migration27\|Settings\|HomebrewRules\|HomebrewPreset"
```

Expected: No matches (all reads now go through `RuleResolutionHelper`).

**Step 2: Run full test suite**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew compileTestKotlinJs
```
Expected: PASS

**Step 3: Commit**

```bash
git add -A
git commit -m "refactor(homebrew): complete boolean site refactoring to RuleResolutionHelper (S1-S11)"
```

---

### Phase 7: JSON Profile Schema

---

#### Task 18: Create homebrew-profile JSON schema

**Objective:** Create `src/commonMain/resources/schemas/homebrew-profile.json` for import validation.

**Files:**
- Create: `src/commonMain/resources/schemas/homebrew-profile.json` (N11)

**Step 1: Create schema**

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "title": "Homebrew Rules Profile",
  "type": "object",
  "required": ["id", "name", "version", "rules"],
  "properties": {
    "id": { "type": "string", "minLength": 1 },
    "name": { "type": "string", "minLength": 1 },
    "version": { "type": "integer", "minimum": 1 },
    "isActive": { "type": "boolean" },
    "description": { "type": ["string", "null"] },
    "rules": {
      "type": "object",
      "properties": {
        "useVanceAndKerenshara": { "type": "boolean" },
        "ruinThreshold": { "type": "integer", "minimum": 1 },
        "eventDc": { "type": "integer", "minimum": 1 },
        "eventDcStep": { "type": "integer", "minimum": 0 },
        "leadershipActivityCap": { "type": "integer", "minimum": 1 },
        "leadershipActivityCapWithTownhall": { "type": "integer", "minimum": 1 },
        "canUpgradeNonCapital": { "type": "boolean" },
        "campingActivityCountByPartySize": { "type": "boolean" },
        "noRandomCombatInClaimedHexes": { "type": "boolean" },
        "capStructureBonusAtKingdomLevel": { "type": "boolean" },
        "capitalCanGrowOneSizeLarger": { "type": "boolean" },
        "cultOfTheBloomEvents": { "type": "boolean" },
        "travelCostRiverNoBridgeAdditional": { "type": "integer", "minimum": 0 },
        "pavedStreetsReduceTravelCost": { "type": "boolean" },
        "settlementInfluenceRadius": { "type": "integer", "minimum": 0 }
      },
      "additionalProperties": false
    }
  },
  "additionalProperties": false
}
```

**Step 2: Write schema validation test**

In `src/commonTest/kotlin/at/posselt/pfrpg2e/homebrew/HomebrewProfileSchemaTest.kt` (N15):

```kotlin
class HomebrewProfileSchemaTest {
    @Test fun `valid Gregory profile passes schema`()
    @Test fun `profile missing required field fails schema`()
    @Test fun `profile with wrong type fails schema`()
    @Test fun `profile with invalid version fails schema`()
}
```

Use the existing JSON schema validation pattern in the project (check how other schema tests work in `src/commonTest/`).

**Step 3: Verify compilation + tests**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew commonTest --tests "at.posselt.pfrpg2e.homebrew.HomebrewProfileSchemaTest"
```
Expected: PASS

**Step 4: Commit**

```bash
git add src/commonMain/resources/schemas/homebrew-profile.json \
        src/commonTest/kotlin/at/posselt/pfrpg2e/homebrew/HomebrewProfileSchemaTest.kt
git commit -m "feat(homebrew): add JSON schema for profile import validation"
```

---

### Phase 8: Integration Tests

---

#### Task 19: Write integration tests I1–I5

**Objective:** Cover camping activity count, ruin threshold, structure bonus cap, settings registration, and import/export round-trip.

**Files:**
- Create: `src/jsTest/kotlin/at/posselt/pfrpg2e/homebrew/CampingActivityCountIntegrationTest.kt` (N17)
- Create: `src/jsTest/kotlin/at/posselt/pfrpg2e/homebrew/RuinThresholdIntegrationTest.kt` (N18)
- Create: `src/jsTest/kotlin/at/posselt/pfrpg2e/homebrew/ImportExportIntegrationTest.kt` (N19)

(SettingsRegistrationIntegrationTest already written in Task 8.)

**Step 1: Write tests** following the `unsafeJso {}` mock pattern used in the project. Each test:
1. Sets up a mock `KingdomData`/`CampingData` with `unsafeJso {}`
2. Activates a profile or sets preset
3. Calls the consuming method
4. Asserts the resolved value matches the active profile

**Step 2: Run tests**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew jsTest --tests "at.posselt.pfrpg2e.homebrew.*IntegrationTest"
```
Expected: PASS

**Step 3: Commit**

```bash
git add src/jsTest/kotlin/at/posselt/pfrpg2e/homebrew/*IntegrationTest.kt
git commit -m "test(homebrew): add integration tests I1-I5"
```

---

### Phase 9: Build Verification

---

#### Task 20: Full build verification

**Objective:** Ensure everything compiles and tests pass.

**Step 1: Run compile checks**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew compileKotlinJs compileTestKotlinJs
```
Expected: PASS

**Step 2: Run schema/data validation**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew validateStructures validateKingdomActivities
```
Expected: PASS (these validate existing JSON data, should not be affected)

**Step 3: Run common tests**

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew commonTest
```
Expected: PASS — all U1–U4 tests pass

**Step 4: Note environment limitations**

If `jsBrowserTest` or full `assemble` fails due to FirefoxHeadless in WSL, record it as environment-only — not a code failure.

**Final check:**

```bash
git status --short
```
Verify: only homebrew-related files appear in the working tree.

---

## Manual Foundry Verification Checklist

These require a running Foundry VTT with the module loaded. GM role required.

| # | Test | Steps | Expected Result |
|---|------|-------|-----------------|
| M1 | Profile activation from settings | 1. Open Module Settings 2. Select "Gregory's House Rules" | Setting saves. Kingdom sheet shows active profile name |
| M2 | Profile deactivation | 1. Active = Gregory 2. Change to "None (RAW)" | Kingdom sheet shows "None (RAW)". All rules revert |
| M3 | Management dialog opens | 1. Open Kingdom Sheet → Homebrew tab 2. Click "Manage Profiles..." | Profile manager dialog appears showing Gregory profile |
| M4 | Export profile | 1. In Profile Manager, click "Export" | Browser downloads `gregory-profile.json` or copies to clipboard |
| M5 | Import profile | 1. Click "Import" 2. Paste valid JSON 3. Confirm | Success notification shown |
| M6 | Import invalid profile | 1. Click "Import" 2. Paste malformed JSON | Error notification: "Invalid profile: [reason]" |
| M7 | Camping count with Gregory | 1. Party of 5 PCs 2. Activate Gregory 3. Open camping sheet | 5 camping activities (not 4) |
| M8 | Camping count in RAW | 1. Deactivate profile 2. Open camping sheet | 4 camping activities |
| M9 | Ruin threshold at 5 with Gregory | 1. Set kingdom ruin = 5 2. Activate Gregory | Ruin warning triggers |
| M10 | Ruin threshold at 5 in RAW | 1. Set kingdom ruin = 5 2. Deactivate profile | No ruin warning (threshold = 10) |
| M11 | Structure cap with Gregory | 1. Activate Gregory 2. Build structure bonus > kingdom level | Bonus capped at kingdom level |
| M12 | Switch mid-session | 1. Activate Gregory 2. Switch to NONE without reload | All systems use RAW values immediately |
| M13 | GM-only visibility | 1. Log in as player 2. Open kingdom sheet | Homebrew settings NOT visible to players |

---

## Verification Summary

- [ ] All unit tests pass (U1–U4): `commonTest`
- [ ] All integration tests pass (I1–I5): `jsTest`
- [ ] Compilation passes: `compileKotlinJs compileTestKotlinJs`
- [ ] Schema validation passes: `validateStructures validateKingdomActivities`
- [ ] All 11 scattered boolean sites refactored to `RuleResolutionHelper`
- [ ] No remaining direct `KingdomSettings` reads for homebrew fields (grep verification)
- [ ] Migration27 registered in migration list
- [ ] Localization keys resolve for all new strings
