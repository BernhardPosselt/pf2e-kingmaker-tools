# Rule-Resolution Helper: Shared Spec for Camping, Kingdom, and Hex Systems

> **Date:** 2026-06-01
> **Status:** Draft — pending review
> **Cross-reference:** Feature plan `docs/plans/2026-06-01-homebrew-rules-profile-system.md` (the "f1" homebrew rules profile plan) defines the `RuleResolutionHelper` object and its data models (lines 188–243). This spec describes how the camping, kingdom, and hex systems consume that helper.

---

## 1. Purpose

The `RuleResolutionHelper` (`commonMain`) is the **single resolution layer** between the active homebrew rules profile and three consuming systems:

| System | Consumes settings for |
|--------|-----------------------|
| **Camping** | Activity count (party-size vs. fixed-4), cooking/encounter modifiers |
| **Kingdom** | Ruin threshold, event DC, leadership activity cap, structure bonus cap, capital growth, cult events, non-capital upgrades, settlement influence radius |
| **Hex** | Random combat suppression in claimed hexes, travel cost (river/no-bridge, paved streets) |

All three systems previously read `KingdomSettings` booleans/int fields directly (scattered sites S1–S11 from the homebrew plan). The helper centralizes those reads behind a profile-aware interface so that switching between RAW and "Gregory's House Rules" is a single profile activation, not 11 code changes.

---

## 2. Resolution Precedence

The helper resolves any gear/rule setting through a **three-tier precedence chain**:

```
custom profile overrides  >  V&K preset values  >  RAW defaults
```

Concretely:

1. **Custom profile** — If the GM creates a custom profile (future: import or UI-edited) and activates it, the profile's `rules` field wins for every setting it explicitly defines.

2. **Preset (V&K / Gregory)** — If the active profile is the built-in `HomebrewPreset.GREGORY`, the values from `HomebrewRules.gregory()` apply. If `HomebrewPreset.NONE`, RAW defaults apply. The profile's `rules` data class is the source of truth for preset values.

3. **RAW default** — When no profile is active (`profile == null`), every helper method falls back to the `HomebrewRules.none()` companion defaults, which match RAW Pathfinder 2e Kingmaker rules.

The helper is a **pure-function object**: every method is a stateless function of `(profile, ...contextParams)`. No caching, no mutation. The `profile` parameter is always passed in by the caller.

### Precedence per setting

| Setting | Custom profile | Gregory (V&K) | RAW default |
|---------|---------------|---------------|-------------|
| `ruinThreshold` | `profile.rules.ruinThreshold` | `5` | `10` |
| `eventDc` | `profile.rules.eventDc` | `5` | `15` |
| `leadershipActivityCap` | `profile.rules.leadershipActivityCap` | `8` | `6` |
| `leadershipActivityCapWithTownhall` | `profile.rules.leadershipActivityCapWithTownhall` | `12` | `8` |
| `campingActivityCountByPartySize` | `profile.rules.campingActivityCountByPartySize` | `true` → count = party size | `false` → count = 4 |
| `noRandomCombatInClaimedHexes` | `profile.rules.noRandomCombatInClaimedHexes` | `true` | `false` |
| `capStructureBonusAtKingdomLevel` | `profile.rules.capStructureBonusAtKingdomLevel` | `true` | `false` |
| `canUpgradeNonCapital` | `profile.rules.canUpgradeNonCapital` | `true` | `false` |
| `capitalCanGrowOneSizeLarger` | `profile.rules.capitalCanGrowOneSizeLarger` | `true` | `false` |
| `cultOfTheBloomEvents` | `profile.rules.cultOfTheBloomEvents` | `true` | `false` |
| `travelCostRiverNoBridgeAdditional` | `profile.rules.travelCostRiverNoBridgeAdditional` | `1` | `0` |
| `pavedStreetsReduceTravelCost` | `profile.rules.pavedStreetsReduceTravelCost` | `true` | `false` |
| `settlementInfluenceRadius` | `profile.rules.settlementInfluenceRadius` | `1` | `0` |

---

## 3. Helper Interface Contract

### 3.1 Kotlin signature (from f1 plan, lines 188–243)

```kotlin
object RuleResolutionHelper {
    fun getRuinThreshold(profile: HomebrewRulesProfile?): Int
    fun getEventDc(profile: HomebrewRulesProfile?): Int
    fun getLeadershipActivityCap(profile: HomebrewRulesProfile?, hasTownhallOrHigher: Boolean): Int
    fun getCampingActivityCount(profile: HomebrewRulesProfile?, partySize: Int): Int
    fun isRandomCombatSuppressedInClaimedHexes(profile: HomebrewRulesProfile?): Boolean
    fun isStructureBonusCappedAtKingdomLevel(profile: HomebrewRulesProfile?): Boolean
    fun canUpgradeNonCapitalSettlement(profile: HomebrewRulesProfile?): Boolean
    fun canCapitalGrowOneSizeLarger(profile: HomebrewRulesProfile?): Boolean
    fun useVanceAndKerensharaXp(profile: HomebrewRulesProfile?): Boolean
    fun getCultOfTheBloomEvents(profile: HomebrewRulesProfile?): Boolean
    fun getTravelCostRiverNoBridgeAdditional(profile: HomebrewRulesProfile?): Int
    fun doPavedStreetsReduceTravelCost(profile: HomebrewRulesProfile?): Boolean
    fun getSettlementInfluenceRadius(profile: HomebrewRulesProfile?): Int
    fun getActiveProfile(registry: HomebrewProfileRegistry?): HomebrewRulesProfile?
}
```

### 3.2 Calling convention

Every consuming system must:

1. **Resolve the active profile once** per operation (not per-helper-call) using `RuleResolutionHelper.getActiveProfile(registry)`, where `registry` comes from `game.settings.getObject("homebrewRulesProfileRegistry")`.

2. **Pass the resolved `HomebrewRulesProfile?`** to each helper method. If the profile is `null`, the helper returns RAW defaults automatically.

3. **Never read `KingdomSettings.ruinThreshold`, `KingdomSettings.capStructureBonusAtKingdomLevel`, etc. directly.** All_reads must go through the helper. The only exception is `KingdomSettings` fields that are unrelated to homebrew rules (e.g., `settlementsGenerateRd`, `kingdomEventRollMode`).

### 3.3 Handling missing or partial settings

- **Null profile** → Every method falls back to `HomebrewRules.none()` companion defaults (RAW rules). No exceptions thrown.

- **Partial rules object** → `HomebrewRules` is a Kotlin data class with default values on every field. If a partial JSON import omits a field, Kotlin uses the constructor default (which is the RAW value). This means an imported profile that only overrides `ruinThreshold` will get RAW defaults for all other settings without error.

- **Invalid field values** → JSON schema validation (`homebrew-profile.json`, from f1 plan Task 18) enforces minimum values (e.g., `ruinThreshold >= 1`, `version >= 1`) before the profile is stored in the registry. The helper methods assume valid data; they do not re-validate.

---

## 4. Consuming System Integration

### 4.1 Camping System

**Files affected:**
- `src/jsMain/kotlin/at/posselt/pfrpg2e/camping/CampingSheet.kt`
- `src/jsMain/kotlin/at/posselt/pfrpg2e/camping/CampingSheetDataModel.kt`

**Settings consumed:** `campingActivityCountByPartySize`

**Resolution site:** Where the camping sheet determines how many activities the party can perform for the night.

**Before (scattered site S3):**
```kotlin
// Reads KingdomSettings directly
val activityCount = if (kingdomSettings.campingActivityCountByPartySize) {
    party.actorUuids.size
} else {
    4
}
```

**After (via helper):**
```kotlin
val registry = game.settings.getObject("homebrewRulesProfileRegistry") as? HomebrewProfileRegistry
val activeProfile = RuleResolutionHelper.getActiveProfile(registry)
val activityCount = RuleResolutionHelper.getCampingActivityCount(activeProfile, party.actorUuids.size)
```

**Contract:** The camping sheet calls `getCampingActivityCount` once per camping session render. The `partySize` parameter is the number of PC actor UUIDs in `CampingData.actorUuids`. The helper returns either `partySize` (Gregory/custom) or `4` (RAW).

### 4.2 Kingdom System

**Files affected:** Scattered across kingdom advancement, ruin, structure, settlement, leadership, and event logic (sites S1, S2, S4, S6, S7, S8, S9, S10, S11 from the f1 plan).

**Settings consumed:**
- `ruinThreshold` — KingdomSheet, Ruin handling
- `eventDc`, `eventDcStep` — Kingdom event processing
- `leadershipActivityCap`, `leadershipActivityCapWithTownhall` — Leadership activity allocation
- `capStructureBonusAtKingdomLevel` — Structure evaluation
- `canUpgradeNonCapital` — Civic activity "Improve Settlement" availability
- `capitalCanGrowOneSizeLarger` — Capital growth logic
- `cultOfTheBloomEvents` — Event frequency/availability
- `useVanceAndKerenshara` — XP calculation path
- `settlementInfluenceRadius` — Settlement logic

**Resolution pattern (per-site example, site S2 — ruin threshold):**

```kotlin
// Once per kingdom turn/sheet render:
val registry = game.settings.getObject("homebrewRulesProfileRegistry") as? HomebrewProfileRegistry
val activeProfile = RuleResolutionHelper.getActiveProfile(registry)

// At each scattered site, replace the direct read:
val threshold = RuleResolutionHelper.getRuinThreshold(activeProfile)  // was: kingdomData.settings.ruinThreshold
```

**Leadership activity cap (site S4) — special case with context parameter:**
```kotlin
val cap = RuleResolutionHelper.getLeadershipActivityCap(
    profile = activeProfile,
    hasTownhallOrHigher = settlementHasTownhallOrHigher(settlement)
)
```

### 4.3 Hex System

**Files affected:** Hex travel cost logic, random encounter rolling (site S5).

**Settings consumed:**
- `noRandomCombatInClaimedHexes` — Random encounter suppression
- `travelCostRiverNoBridgeAdditional` — River crossing cost
- `pavedStreetsReduceTravelCost` — Paved streets discount

**Resolution pattern (site S5 — random encounter suppression):**

```kotlin
val activeProfile = RuleResolutionHelper.getActiveProfile(registry)
val suppress = RuleResolutionHelper.isRandomCombatSuppressedInClaimedHexes(activeProfile)
if (suppress && hex.isClaimed) {
    // Skip random encounter roll
}
```

**Travel cost (site S9 — paved streets):**
```kotlin
val additionalCost = RuleResolutionHelper.getTravelCostRiverNoBridgeAdditional(activeProfile)
val pavedDiscount = RuleResolutionHelper.doPavedStreetsReduceTravelCost(activeProfile)
// Apply to hex travel cost calculation
```

---

## 5. Test Strategy

### 5.1 Unit Tests (commonMain, `RuleResolutionHelperTest`)

**Location:** `src/commonTest/kotlin/at/posselt/pfrpg2e/homebrew/RuleResolutionHelperTest.kt`

**Coverage:** Every helper method tested with:
- **Gregory profile** → returns house-rule value
- **Null profile** → returns RAW default
- **Custom profile with partial overrides** → returns custom value for overridden field, RAW default for others (future test, stub now)

**Existing test shape (from f1 plan, Task 5):**
```kotlin
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

    @Test fun `camping activity count - Gregory uses party size`() {
        assertEquals(3, RuleResolutionHelper.getCampingActivityCount(gregoryProfile, 3))
        assertEquals(5, RuleResolutionHelper.getCampingActivityCount(gregoryProfile, 5))
    }

    @Test fun `camping activity count - NONE returns 4`() {
        assertEquals(4, RuleResolutionHelper.getCampingActivityCount(null, 3))
        assertEquals(4, RuleResolutionHelper.getCampingActivityCount(null, 5))
    }
    // ... one @Test per method per profile variant
}
```

**Test count target:** ~24–30 tests (12 methods × 2–3 variants each).

### 5.2 Integration Tests (jsMain, per consuming system)

**I1 — CampingActivityCountIntegrationTest**

Tests that the camping sheet actually uses the helper to determine activity count:
1. Set up a mock `CampingData` with 5 actor UUIDs
2. Activate Gregory profile → assert 5 activities rendered
3. Activate NONE profile → assert 4 activities rendered
4. Switch profile mid-session → assert count updates without reload

**I2 — RuinThresholdIntegrationTest**

Tests that the kingdom ruin system reads from the helper:
1. Set up a mock `KingdomData` with ruin = 5
2. Activate Gregory (threshold = 5) → assert ruin warning triggers
3. Switch to NONE (threshold = 10) → assert no warning
4. Set ruin = 10 with Gregory → assert warning triggers

**I3 — StructureBonusCapIntegrationTest**

Tests that structure evaluation respects the helper:
1. Build a structure bonus that exceeds kingdom level
2. Activate Gregory → assert bonus is capped at kingdom level
3. Switch to NONE → assert bonus is uncapped

**I4 — SettingsRegistrationIntegrationTest** (from f1 plan, Task 8)

Tests that the `homebrewRulesProfileRegistry` setting is registered with default NONE and is writable.

**I5 — ImportExportIntegrationTest**

Tests profile round-trip:
1. Export Gregory profile → valid JSON matching `homebrew-profile.json` schema
2. Import exported JSON → profile appears in registry
3. Import invalid JSON → error message per localization key

### 5.3 Build Verification

```
commonTest          → U1 (HomebrewPreset), U2 (HomebrewRules), U3 (enum), U4 (schema)
jsTest              → I1 (camping), I2 (ruin), I3 (structure), I4 (settings), I5 (import/export)
compileKotlinJs     → no errors
grep verification   → no remaining direct KingdomSettings reads for homebrew fields
```

---

## 6. Data Flow Diagram

```
┌─────────────────────────────────────────────────────┐
│  Foundry settings (world scope)                     │
│  homebrewRulesProfileRegistry: HomebrewProfileRegistry│
│  homebrewRulesProfile: HomebrewRulesProfile (active) │
└──────────────────────┬──────────────────────────────┘
                       │ game.settings.getObject()
                       ▼
┌──────────────────────────────────────────────────────┐
│  Consuming code (one-time per operation)             │
│  val registry = settings.getObject("...Registry")    │
│  val activeProfile = RuleResolutionHelper             │
│      .getActiveProfile(registry)                      │
└──────────────────────┬──────────────────────────────┘
                       │ pass activeProfile
          ┌────────────┼────────────────┐
          ▼            ▼                ▼
   ┌───────────┐ ┌──────────┐  ┌────────────┐
   │ Camping   │ │ Kingdom  │  │ Hex        │
   │ Sheet     │ │ Systems  │  │ Travel     │
   │           │ │ (11 sits)│  │ /Encounter │
   └─────┬─────┘ └────┬─────┘  └──────┬─────┘
         │             │               │
         ▼             ▼               ▼
   ┌─────────────────────────────────────────┐
   │  RuleResolutionHelper (object)          │
   │  getCampingActivityCount(profile, size) │
   │  getRuinThreshold(profile)              │
   │  isRandomCombatSuppressedInClaimedHexes │
   │  ... (12 pure-function methods)         │
   └──────────────────┬──────────────────────┘
                      │ profile?.rules ?: HomebrewRules.none()
                      ▼
   ┌──────────────────────────────────────────┐
   │  HomebrewRules (data class, commonMain)  │
   │  — all fields have RAW defaults          │
   │  — HomebrewRules.gregory() for V&K       │
   │  — HomebrewRules.none() for RAW          │
   └──────────────────────────────────────────┘
```

---

## 7. Migration Notes for Consuming Systems

When wiring each system to the helper (f1 plan Tasks 14–16):

1. **Do NOT pass `KingdomSettings` to the helper.** The helper only needs `HomebrewRulesProfile?`.

2. **Resolve `activeProfile` at the highest call site** (sheet render, turn tick, dialog open) and thread it down. Do not resolve it inside every helper call — that re-reads `game.settings` unnecessarily.

3. **If the consuming code has no natural call site for resolution** (e.g., a utility function called from many places), accept `HomebrewRulesProfile?` as a parameter and let the caller resolve it.

4. **HomebrewData arrays are unaffected.** `KingdomData.homebrewMilestones`, `homebrewActivities`, etc. are separate extension-point arrays that store custom kingdom content. The `RuleResolutionHelper` operates on the profile system, not on per-kingdom homebrew data items.

5. **Profile activation is immediate.** No reload required. When the GM activates Gregory in the profile manager dialog, the next read from any consuming system uses the new profile because it re-reads `game.settings` on each resolution.

---

## 8. Shared Resolution Patterns (from f1)

The following patterns are established in the homebrew rules profile plan and reused here:

1. **Define once, consume everywhere:** `RuleResolutionHelper` lives in `commonMain` so it compiles to both JVM test targets and JS. Consuming systems in `jsMain` and `jsTest` import it directly.

2. **Stateless object, not a service:** The helper is a Kotlin `object` with pure functions. No DI, no state, no lifecycle. This makes it safe to call from `DataModel` definitions, `FormApp` subclasses, and utility functions alike.

3. **Null = RAW convention:** Every method's `profile` parameter is nullable. `null` is the canonical "no profile active / RAW rules" signal. Callers never need to check `profile?.isActive` — if an inactive profile is passed, it still carries its `rules` payload; the caller just shouldn't pass inactive profiles (resolved by `getActiveProfile()`).

4. **Data class defaults = RAW safety net:** `HomebrewRules` constructor defaults match RAW rules. If future profile fields are added but an old profile JSON lacks them, the defaults silently apply RAW behavior rather than crashing.

5. **Single read per operation:** The f1 plan's scattered site refactoring (Tasks 14–16) resolves the active profile once per logical operation (kingdom turn, camping session, hex claim) and passes it to all helper calls in that operation.
