# Gear Settings Profile System Implementation Plan

> **Status:** Draft — pending review
> **Date:** 2026-06-01

---

## Executive Summary

This plan describes a **gear settings profile system** for the pf2e-kingmaker-tools module. The system lets Game Masters switch between named configurations of all toggleable module settings — kingdom, camping, hex, army, and UI — via a single profile selection, rather than toggling each setting individually.

**Key capabilities:**

- **Three built-in profiles:** RAW (rules as written), Vance & Kerenshara (community house rules), and Gregory's custom profile
- **Custom profiles:** GMs can create, import, export, and activate custom profiles
- **Profile sharing:** Export to JSON file for sharing with other GMs
- **Rule-resolution helper:** A single `RuleResolutionHelper` in `commonMain` that all camping, kingdom, hex, and army systems call instead of reading settings directly
- **Forward-compatible versioning:** Schema version field enables additive migration when new settings are added
- **Non-breaking migration:** Existing settings continue to work; the profile system sits on top via Migration29 + first-run auto-import

**Relationship to f1 (Homebrew Rules Profile System):** The gear settings profile system stores the *full settings state* (all module toggleable settings), while the f1 homebrew rules profile system defines the *resolution layer* for the overlapping kingdom-rule fields. The two systems are complementary — the gear settings profile is the storage/import/export format; the homebrew rules profile is the resolution engine that consuming code calls. They share the same top-level profile skeleton (metadata fields), but differ in payload structure: gear settings use a **nested grouped object**; homebrew rules use a **flat `rules` data class**.

---

## Dependency and Sequencing Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│  PREREQUISITE: f1 Homebrew Rules Profile System (largely complete)  │
│  docs/plans/2026-06-01-homebrew-rules-profile-system.md             │
└───────────────────────────┬─────────────────────────────────────────┘
                            │
                            ▼
┌───────────────────────────────────────────────────────────────────┐
│  PHASE 1: Schema + Data Model (this doc, Section 1)              │
│  Create JSON Schema files, Kotlin data classes, Foundry settings  │
│  registration. No UI, no migration — just types and validation.   │
└───────────────────────────┬───────────────────────────────────────┘
                            │
                            ▼
┌───────────────────────────────────────────────────────────────────┐
│  PHASE 2: Import/Export (this doc, Section 2)                    │
│  Serialization, Migration29, first-run auto-import, file I/O.    │
│  Depends on Phase 1 (schema validation).                          │
└───────────────────────────┬───────────────────────────────────────┘
                            │
                            ▼
┌───────────────────────────────────────────────────────────────────┐
│  PHASE 3: Settings UI (this doc, Section 3)                      │
│  Profile selection dialog, conflict detection, activation flow.   │
│  Depends on Phase 1+2 (profiles exist, can be imported/exported). │
└───────────────────────────┬───────────────────────────────────────┘
                            │
                            ▼
┌───────────────────────────────────────────────────────────────────┐
│  PHASE 4: Rule-Resolution Helper + Consumer Wiring (this doc,    │
│  Section 4)                                                        │
│  Wire camping/kingdom/hex/army systems to read from the helper   │
│  instead of direct `KingdomSettings` reads. Depends on Phase 1+2  │
│  (profiles stored, registry available). Runs in parallel with     │
│  Phase 3 since the helper interface doesn't depend on UI.         │
└───────────────────────────────────────────────────────────────────┘
```

**Parallel work streams:** Phase 1 must complete first. Phases 2, 3, and 4 can proceed in parallel after Phase 1 since they interact through well-defined interfaces (the schema, the registry key, the helper methods).

**Cross-references to f1 (unless otherwise noted, all f1 refs are to `docs/plans/2026-06-01-homebrew-rules-profile-system.md`):**

| Topic | Gear Settings (this plan) | f1 Homebrew Plan |
|-------|--------------------------|------------------|
| Profile manager dialog | `GearSettingsProfileManager.kt` (Sec 3) | `HomebrewProfileManager.kt` |
| JSON schema | `gear-settings-profile.json` | `homebrew-profile.json` |
| Registry key | `gearSettingsProfileRegistry` | `homebrewRulesProfileRegistry` |
| Active profile key | `gearSettingsActiveProfile` | `homebrewRulesProfile` |
| Export serialization | Reuses f1 `ProfileExportSerializer.kt` base (Sec 2) | `ProfileExportSerializer.kt` |
| Import validation | 3-tier: structural, key whitelist, range (Sec 2) | JSON Schema only |
| Default seeding | First-run auto-import from current settings (Sec 2) | Migration27 seeds Gregory |
| Resolution helper | `RuleResolutionHelper` in `commonMain` (Sec 4) | Same object (shared) |
| Settings payload format | Nested grouped objects (`settings.kingdom.ruinThreshold`) | Flat `rules` object (`rules.ruinThreshold`) |
| Version field name | `schemaVersion` (profile) / `version` (registry) | `version` (both) |

---

## Section 1: Schema and Data Model

### 1.1 Profile Data Model

#### Top-level profile container

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `schemaVersion` | `integer` | yes | Schema format version (current: `1`). Increment when fields are added/removed. |
| `id` | `string` | yes | Unique profile identifier. Builtin: `raw`, `vance-kerenshara`, `gregory`. Custom: any slug matching `^[a-z0-9_-]+$`. |
| `name` | `string` | yes | Display name shown in settings UI. max 128 chars. |
| `description` | `string` | no | Longer description for tooltip. max 512 chars. |
| `author` | `string` | no | Creator name (for shared profiles). max 128 chars. |
| `isBuiltin` | `boolean` | yes | `true` for RAW and V&K (immutable). `false` for custom/Gregory. |
| `createdAt` | `string` (ISO 8601) | yes | Creation timestamp. |
| `updatedAt` | `string` (ISO 8601) | yes | Last modification timestamp. |
| `settings` | `GearSettings` | yes | The actual configuration values. |

#### GearSettings (settings bag)

The `settings` object groups all configurable toggles by subsystem. Each group mirrors the corresponding Foundry `ClientSettings` key.

**Kingdom settings** (`settings.kingdom`):

| Field | Type | Default (RAW) | Description |
|-------|------|---------------|-------------|
| `advancement` | `enum[xp, milestone]` | `xp` | Advancement mode |
| `ruinThreshold` | `integer` (1–20) | `10` | Ruin points before collapse |
| `eventDc` | `integer` (1–30) | `15` | Base DC for kingdom events |
| `eventDcStep` | `integer` (0–10) | `0` | DC increment per tier |
| `leadershipActivityCap` | `integer` (1–20) | `6` | Max leadership activities (no Townhall) |
| `leadershipActivityCapWithTownhall` | `integer` (1–20) | `8` | Max leadership activities (with Townhall+) |
| `canUpgradeNonCapital` | `boolean` | `false` | Allow "Improve Settlement" for non-capital |
| `capitalCanGrowOneSizeLarger` | `boolean` | `false` | Capital can exceed normal size limit |
| `noRandomCombatInClaimedHexes` | `boolean` | `false` | Suppress random combat in claimed hexes |
| `capStructureBonusAtKingdomLevel` | `boolean` | `false` | Cap structure bonuses at kingdom level |
| `settlementInfluenceRadius` | `integer` (0–5) | `0` | Settlement influence radius in hexes (0 = disabled) |
| `cultOfTheBloomEvents` | `boolean` | `false` | Enable Cult of the Bloom daily events |

**Camping settings** (`settings.camping`):

| Field | Type | Default (RAW) | Description |
|-------|------|---------------|-------------|
| `campingActivityCountByPartySize` | `boolean` | `false` | Activities = PC count vs. fixed 4 |
| `enableSheltered` | `boolean` | `false` | Allow sheltered condition from camping |
| `enableWeather` | `boolean` | `true` | Enable weather subsystem |

**Hex settings** (`settings.hex`):

| Field | Type | Default (RAW) | Description |
|-------|------|---------------|-------------|
| `travelCostRiverNoBridgeAdditional` | `integer` (0–5) | `0` | Extra travel cost crossing river without bridge |
| `pavedStreetsReduceTravelCost` | `boolean` | `false` | Paved streets reduce travel cost to 1 |
| `hexMapEnabled` | `boolean` | `true` | Enable hex map/sync features |

**Army settings** (`settings.army`):

| Field | Type | Default (RAW) | Description |
|-------|------|---------------|-------------|
| `enableCombatTracks` | `boolean` | `true` | Play combat music during army battles |

**UI/misc settings** (`settings.ui`):

| Field | Type | Default (RAW) | Description |
|-------|------|---------------|-------------|
| `hideBuiltinKingdomSheet` | `boolean` | `false` | Hide native Foundry kingdom sheet |
| `enablePartyActorIcons` | `boolean` | `true` | Show party member icons on scene |
| `enableWeatherSoundFx` | `boolean` | `true` | Play ambient weather sound effects |
| `enableTokenMapping` | `boolean` | `true` | Enable token-to-actor mapping |
| `disableFirstRunMessage` | `boolean` | `false` | Suppress first-run welcome message |

#### Profile Registry

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `schemaVersion` | `integer` | yes | Registry format version (current: `1`) |
| `activeProfileId` | `string\|null` | yes | Active profile ID. `null` == RAW. |
| `profiles` | `GearSettingsProfile[]` | yes | All profiles. Always includes RAW and V&K. minItems: 2 |

### 1.2 Overlap with f1 HomebrewRules

The f1 homebrew rules profile system defines `HomebrewRules` with the same kingdom-rule field names. **The gear settings profile does NOT duplicate these structures.** Instead:

1. **Shared kingdom rule fields:** `GearSettings.kingdom.*` and `HomebrewRules.*` share field names and semantics. The `RuleResolutionHelper` (Section 4) reads from whichever profile has precedence.
2. **Separate registries, shared resolver:** `GearSettingsProfileRegistry` and `HomebrewProfileRegistry` (f1) are stored as separate Foundry `ClientSettings` keys. `RuleResolutionHelper` reads both.
3. **Independent versioning:** Gear settings `schemaVersion` and homebrew rules `version` are incremented independently.

**Field overlap table:**

| GearSettings.kingdom field | HomebrewRules field | Shared? |
|---|---|---|
| `ruinThreshold` | `ruinThreshold` | yes |
| `eventDc` | `eventDc` | yes |
| `eventDcStep` | `eventDcStep` | yes |
| `leadershipActivityCap` | `leadershipActivityCap` | yes |
| `leadershipActivityCapWithTownhall` | `leadershipActivityCapWithTownhall` | yes |
| `canUpgradeNonCapital` | `canUpgradeNonCapital` | yes |
| `capitalCanGrowOneSizeLarger` | `capitalCanGrowOneSizeLarger` | yes |
| `noRandomCombatInClaimedHexes` | `noRandomCombatInClaimedHexes` | yes |
| `capStructureBonusAtKingdomLevel` | `capStructureBonusAtKingdomLevel` | yes |
| `cultOfTheBloomEvents` | `cultOfTheBloomEvents` | yes |
| `settlementInfluenceRadius` | — | gear-only |
| `advancement` | — | gear-only |
| — | `useVanceAndKerenshara` | f1-only |
| `campingActivityCountByPartySize` | `campingActivityCountByPartySize` | yes |
| — | `travelCostRiverNoBridgeAdditional` | f1-only (in f1 rules) |
| — | `pavedStreetsReduceTravelCost` | f1-only (in f1 rules) |

Fields marked "gear-only" or "f1-only" belong exclusively to their respective profiles. The resolver merges them with the precedence: `custom > V&K > RAW`.

### 1.3 Profile Variant Definitions

#### RAW profile

```json
{
  "schemaVersion": 1,
  "id": "raw",
  "name": "RAW (Rules as Written)",
  "description": "Unmodified Pathfinder 2e Kingmaker rules.",
  "isBuiltin": true,
  "createdAt": "2026-06-01T00:00:00Z",
  "updatedAt": "2026-06-01T00:00:00Z",
  "settings": {
    "kingdom": {
      "advancement": "xp",
      "ruinThreshold": 10,
      "eventDc": 15,
      "eventDcStep": 0,
      "leadershipActivityCap": 6,
      "leadershipActivityCapWithTownhall": 8,
      "canUpgradeNonCapital": false,
      "capitalCanGrowOneSizeLarger": false,
      "noRandomCombatInClaimedHexes": false,
      "capStructureBonusAtKingdomLevel": false,
      "settlementInfluenceRadius": 0,
      "cultOfTheBloomEvents": false
    },
    "camping": {
      "campingActivityCountByPartySize": false,
      "enableSheltered": false,
      "enableWeather": true
    },
    "hex": {
      "travelCostRiverNoBridgeAdditional": 0,
      "pavedStreetsReduceTravelCost": false,
      "hexMapEnabled": true
    },
    "army": {
      "enableCombatTracks": true
    },
    "ui": {
      "hideBuiltinKingdomSheet": false,
      "enablePartyActorIcons": true,
      "enableWeatherSoundFx": true,
      "enableTokenMapping": true,
      "disableFirstRunMessage": false
    }
  }
}
```

#### V&K profile

The V&K profile settings values are identical to RAW for the gear settings covered here. V&K differences primarily live in Kotlin data files (structure bonuses, XP tables) toggled via Foundry data packs, not via the settings profile itself. The V&K profile serves as a marker enabling those data packs.

```json
{
  "schemaVersion": 1,
  "id": "vance-kerenshara",
  "name": "Vance & Kerenshara",
  "description": "Vance & Kerenshara community house rules for kingdom advancement and structure balance.",
  "author": "Vance & Kerenshara",
  "isBuiltin": true,
  "createdAt": "2026-06-01T00:00:00Z",
  "updatedAt": "2026-06-01T00:00:00Z",
  "settings": {
    "kingdom": {
      "advancement": "xp",
      "ruinThreshold": 10,
      "eventDc": 15,
      "eventDcStep": 0,
      "leadershipActivityCap": 6,
      "leadershipActivityCapWithTownhall": 8,
      "canUpgradeNonCapital": false,
      "capitalCanGrowOneSizeLarger": false,
      "noRandomCombatInClaimedHexes": false,
      "capStructureBonusAtKingdomLevel": false,
      "settlementInfluenceRadius": 0,
      "cultOfTheBloomEvents": false
    },
    "camping": {
      "campingActivityCountByPartySize": false,
      "enableSheltered": false,
      "enableWeather": true
    },
    "hex": {
      "travelCostRiverNoBridgeAdditional": 0,
      "pavedStreetsReduceTravelCost": false,
      "hexMapEnabled": true
    },
    "army": {
      "enableCombatTracks": true
    },
    "ui": {
      "hideBuiltinKingdomSheet": false,
      "enablePartyActorIcons": true,
      "enableWeatherSoundFx": true,
      "enableTokenMapping": true,
      "disableFirstRunMessage": false
    }
  }
}
```

#### Gregory (custom default) profile

```json
{
  "schemaVersion": 1,
  "id": "gregory",
  "name": "Gregory's Gear Settings",
  "description": "Gregory's custom gear settings profile.",
  "author": "Gregory",
  "isBuiltin": false,
  "createdAt": "2026-06-01T00:00:00Z",
  "updatedAt": "2026-06-01T00:00:00Z",
  "settings": {
    "kingdom": {
      "advancement": "xp",
      "ruinThreshold": 5,
      "eventDc": 5,
      "eventDcStep": 0,
      "leadershipActivityCap": 8,
      "leadershipActivityCapWithTownhall": 12,
      "canUpgradeNonCapital": true,
      "capitalCanGrowOneSizeLarger": true,
      "noRandomCombatInClaimedHexes": true,
      "capStructureBonusAtKingdomLevel": true,
      "settlementInfluenceRadius": 1,
      "cultOfTheBloomEvents": true
    },
    "camping": {
      "campingActivityCountByPartySize": true,
      "enableSheltered": false,
      "enableWeather": false
    },
    "hex": {
      "travelCostRiverNoBridgeAdditional": 1,
      "pavedStreetsReduceTravelCost": true,
      "hexMapEnabled": true
    },
    "army": {
      "enableCombatTracks": true
    },
    "ui": {
      "hideBuiltinKingdomSheet": true,
      "enablePartyActorIcons": true,
      "enableWeatherSoundFx": true,
      "enableTokenMapping": true,
      "disableFirstRunMessage": true
    }
  }
}
```

### 1.4 Kotlin Data Classes (commonMain)

```kotlin
package at.posselt.pfrpg2e.gearsettings

data class GearSettingsProfile(
    val schemaVersion: Int = 1,
    val id: String,
    val name: String,
    val description: String? = null,
    val author: String? = null,
    val isBuiltin: Boolean = false,
    val createdAt: String,
    val updatedAt: String,
    val settings: GearSettings = GearSettings(),
) {
    companion object {
        fun raw() = GearSettingsProfile(
            id = "raw", name = "RAW (Rules as Written)",
            isBuiltin = true,
            createdAt = Instant.now().toString(),
            updatedAt = Instant.now().toString(),
            settings = GearSettings(),
        )
        fun vanceKerenshara() = GearSettingsProfile(
            id = "vance-kerenshara", name = "Vance & Kerenshara",
            author = "Vance & Kerenshara", isBuiltin = true,
            createdAt = Instant.now().toString(),
            updatedAt = Instant.now().toString(),
            settings = GearSettings(),
        )
        fun gregory() = GearSettingsProfile(
            id = "gregory", name = "Gregory's Gear Settings",
            author = "Gregory", isBuiltin = false,
            createdAt = Instant.now().toString(),
            updatedAt = Instant.now().toString(),
            settings = GearSettings(
                kingdom = KingdomSettingsGroup(
                    ruinThreshold = 5, eventDc = 5,
                    leadershipActivityCap = 8,
                    leadershipActivityCapWithTownhall = 12,
                    canUpgradeNonCapital = true,
                    capitalCanGrowOneSizeLarger = true,
                    noRandomCombatInClaimedHexes = true,
                    capStructureBonusAtKingdomLevel = true,
                    settlementInfluenceRadius = 1,
                    cultOfTheBloomEvents = true,
                ),
                camping = CampingSettingsGroup(
                    campingActivityCountByPartySize = true,
                    enableWeather = false,
                ),
                hex = HexSettingsGroup(
                    travelCostRiverNoBridgeAdditional = 1,
                    pavedStreetsReduceTravelCost = true,
                ),
                ui = UiSettingsGroup(
                    hideBuiltinKingdomSheet = true,
                    disableFirstRunMessage = true,
                ),
            ),
        )
    }
}

data class GearSettings(
    val kingdom: KingdomSettingsGroup = KingdomSettingsGroup(),
    val camping: CampingSettingsGroup = CampingSettingsGroup(),
    val hex: HexSettingsGroup = HexSettingsGroup(),
    val army: ArmySettingsGroup = ArmySettingsGroup(),
    val ui: UiSettingsGroup = UiSettingsGroup(),
)

data class KingdomSettingsGroup(
    val advancement: String = "xp",
    val ruinThreshold: Int = 10,
    val eventDc: Int = 15,
    val eventDcStep: Int = 0,
    val leadershipActivityCap: Int = 6,
    val leadershipActivityCapWithTownhall: Int = 8,
    val canUpgradeNonCapital: Boolean = false,
    val capitalCanGrowOneSizeLarger: Boolean = false,
    val noRandomCombatInClaimedHexes: Boolean = false,
    val capStructureBonusAtKingdomLevel: Boolean = false,
    val settlementInfluenceRadius: Int = 0,
    val cultOfTheBloomEvents: Boolean = false,
)

data class CampingSettingsGroup(
    val campingActivityCountByPartySize: Boolean = false,
    val enableSheltered: Boolean = false,
    val enableWeather: Boolean = true,
)

data class HexSettingsGroup(
    val travelCostRiverNoBridgeAdditional: Int = 0,
    val pavedStreetsReduceTravelCost: Boolean = false,
    val hexMapEnabled: Boolean = true,
)

data class ArmySettingsGroup(
    val enableCombatTracks: Boolean = true,
)

data class UiSettingsGroup(
    val hideBuiltinKingdomSheet: Boolean = false,
    val enablePartyActorIcons: Boolean = true,
    val enableWeatherSoundFx: Boolean = true,
    val enableTokenMapping: Boolean = true,
    val disableFirstRunMessage: Boolean = false,
)

data class GearSettingsProfileRegistry(
    val schemaVersion: Int = 1,
    val activeProfileId: String? = null,
    val profiles: List<GearSettingsProfile> = listOf(
        GearSettingsProfile.raw(),
        GearSettingsProfile.vanceKerenshara(),
    ),
)
```

### 1.5 Storage in Foundry

| Key | Type | Storage | Scope | Purpose |
|-----|------|---------|-------|---------|
| `gearSettingsProfileRegistry` | `Object` | `ClientSettings.register` | `world` | Full profile registry |
| `gearSettingsActiveProfile` | `DataModel` | `ClientSettings.registerDataModel` | `world` | Active profile (denormalized for fast read) |

The active profile is **denormalized** into a separate `DataModel` for fast reads. When the active profile changes, both the registry and denormalized copy are updated atomically.

### 1.6 Schema Validation

Three validation levels:

1. **JSON Schema validation** on import — incoming JSON validated against `gear-settings-profile.json` using the project's JSON Schema validation infrastructure
2. **Semantic validation** on save — `leadershipActivityCapWithTownhall >= leadershipActivityCap`, `ruinThreshold >= 1`, custom profiles don't use reserved IDs (`raw`, `vance-kerenshara`)
3. **Runtime validation** on activation — missing groups filled from RAW defaults with a logged warning

### 1.7 Version Migration Strategy

1. **Additive changes** (new fields): New fields get sensible defaults. Missing fields filled from RAW at resolution time.
2. **Breaking changes** (removed/renamed): Migration function applied lazily on first profile load after module update, following the `Migration26`/`Migration27` numbered pattern.
3. **Builtin profiles are regenerated** on schema version bump; custom profiles are migrated.

```kotlin
fun migrateProfiles(registry: GearSettingsProfileRegistry): GearSettingsProfileRegistry {
    return when (registry.schemaVersion) {
        1 -> registry  // current
        else -> registry
    }
}
```

### 1.8 Schema File Manifest

| File | Purpose |
|------|---------|
| `src/commonMain/resources/schemas/gear-settings-profile.json` | JSON Schema for a single profile |
| `src/commonMain/resources/schemas/gear-settings-registry.json` | JSON Schema for the registry |
| `src/commonMain/kotlin/at/posselt/pfrpg2e/gearsettings/GearSettingsProfile.kt` | Kotlin data classes |

---

## Section 2: Import/Export

### 2.1 Serialization Format

Profiles serialize to JSON conforming to `gear-settings-profile.json` (draft 2020-12). Top-level profile metadata (`id`, `name`, `schemaVersion`, `author`, `isBuiltin`, `createdAt`, `updatedAt`) shares the same skeleton as f1 homebrew profiles. The payload differs:

| Aspect | Gear Settings Profile | f1 Homebrew Rules Profile |
|--------|----------------------|--------------------------|
| Payload key | `settings` (nested grouped object) | `rules` (flat data class) |
| Preset marker | `isBuiltin: boolean` | `HomebrewPreset` enum |
| Registry key | `gearSettingsProfileRegistry` | `homebrewRulesProfileRegistry` |

### 2.2 File Structure

**Registry storage:** Foundry world-scoped `ClientSettings` under key `gearSettingsProfileRegistry`.

**Individual export file:** `gear-profile-{id}-v{version}.json` — contains only the profile object (not the registry wrapper), making it portable and shareable.

### 2.3 Version Compatibility (3-tier import validation)

**Tier 1 — Structural:** JSON is well-formed and conforms to `gear-settings-profile.json` schema.

**Tier 2 — Key/group validation:** Every setting key in the nested `settings` group is checked against the known settings registry. Unknown keys are rejected with the key name in the error message.

**Tier 3 — Value range:** Integer values checked against min/max. Enum strings checked against registered choices.

**Migration on import:** If `schemaVersion` is older than current, a pure-function migration runs before acceptance (same numbered-migration pattern as the rest of the project).

### 2.4 Migration from Current Workbook Settings

**Step 1 — Registry Setup (Migration29):** Creates `gearSettingsProfileRegistry` with `version: 1`, `activeProfileId: null`, `profiles: []`.

**Step 2 — First-Run Auto-Import:** If `profiles` is empty on first run, creates a default profile from current settings using `detectPreset()` to determine if values match RAW, Gregory, or custom.

```kotlin
val currentSettings = snapshotCurrentSettings()
val defaultProfile = GearSettingsProfile(
    id = "default-v1",
    name = "Default (Current Settings)",
    schemaVersion = 1,
    settings = currentSettings,
    createdAt = now(), updatedAt = now(),
)
```

**Step 3 — Read Path Interception:** After migration, all consuming code reads from the active profile via `RuleResolutionHelper` instead of `game.settings.getXxx()`.

**Step 4 — Write Path:** GM setting changes write to the active profile; on save, diffs are applied to Foundry `ClientSettings` as a cache.

The Excel workbook is **not** modified — it remains a reference document.

### 2.5 Export Flow

1. GM selects profile in `GearSettingsProfileManager` dialog
2. Click "Export" → profile serialized to pretty-printed, 2-space-indented, key-sorted JSON
3. Browser download or clipboard copy via Foundry's `saveDataToFile`
4. **Default export mode:** only keys differing from RAW (keeps files small). "Full Export" toggle exports all keys.

### 2.6 Import Flow

1. GM clicks "Import" → file picker or text area
2. Tier 1/2/3 validation runs
3. On pass: profile added to registry with new UUID-based `id` to avoid collisions
4. Prompt: "Activate this profile now?" (default: yes)
5. On yes: `activeProfileId` set, resolution helper updates, all systems use new values immediately

**Conflict detection:** If imported `id` matches existing profile → "Replace, rename, or cancel?"

**Error messages:**

| Condition | Message |
|-----------|---------|
| Invalid JSON | "Invalid profile: not valid JSON" |
| Missing required field | "Invalid profile: missing '{field}'" |
| Unknown setting key | "Invalid profile: unknown setting key '{key}'" |
| Value out of range | "Invalid profile: '{key}' value {v} out of range [{min}, {max}]" |
| Future version | "Invalid profile: version {n} newer than module-supported {m}" |

### 2.7 Share Use Cases

- **GM-to-GM sharing:** Export → file transfer → import → all settings restored
- **RAW baseline:** `raw-defaults-v1.json` shipped in `data/` for one-click RAW reset
- **V&K preset:** `vk-defaults-v1.json` shipped in `data/` with V&K values

### 2.8 Test Strategy

| Test Class | Scope |
|------------|-------|
| `GearSettingsProfileSchemaTest.kt` | Schema validation pass/fail |
| `GearSettingsProfilePresetTest.kt` | RAW/VK/Gregory preset values |
| `GearSettingsProfileMigrationTest.kt` | V1→V2 migration |
| `GearSettingsImportValidationTest.kt` | Key whitelist + range rejection |
| `GearSettingsImportExportIntegrationTest.kt` | Round-trip import→activate→export |
| `GearSettingsFirstRunMigrationTest.kt` | Migration29 + first-run auto-import |
| `GearSettingsActivationIntegrationTest.kt` | Activate → systems read new values |

**Fixtures:** `src/commonTest/resources/fixtures/gear-profiles/` — `raw-v1.json`, `vk-v1.json`, `gregory-v1.json`, `invalid-missing-version.json`, `invalid-unknown-key.json`.

---

## Section 3: Settings UI

### 3.1 Profile Selection

**Module Settings dropdown:** GM configures the active gear settings profile via `game.settings` menu (standard Foundry module settings UI). No custom application needed for basic selection.

**Compact profile picker:** For in-session switching without leaving the kingdom sheet, a compact picker component is available on the kingdom sheet (enabled when `gearSettingsActiveProfile` is non-null).

### 3.2 Inline Editing

**Settings hints:** When RAW values differ from the active profile's values, the settings UI shows the RAW default as a hint (greyed-out text alongside the current value). This helps GMs understand what "RAW" means for any setting they're unsure about.

**Profile-aware setting labels:** Each setting in the Kingdom Settings dialog shows a small badge indicating which profile it comes from (RAW / V&K / Gregory / Custom). Colors: grey for RAW, blue for V&K, green for Gregory, orange for custom.

### 3.3 Profile Display in Kingdom Sheet

**Active profile badge:** When a non-RAW profile is active, the kingdom sheet header displays a colored badge with the profile name. Clicking the badge opens the profile manager.

**Conflict banner:** If the active profile's settings conflict with current Foundry `ClientSettings` values (e.g., a setting was changed manually outside the profile system), a yellow banner appears in the kingdom sheet with a "Review conflicts" button.

### 3.4 Conflict Detection and Resolution

**Comparison dialog:** The `ConflictResolutionDialog` shows a three-column table: setting name | Foundry current value | Profile value. For each conflict, the GM chooses: keep Foundry value (writes to profile) or use profile value (writes to Foundry settings). A "Use all profile values" bulk action is available.

**When conflicts are detected:**
1. On profile activation (compare active profile values against current Foundry settings)
2. On kingdom sheet render (if profile was changed externally)
3. On import with "activate" flag

### 3.5 Profile Manager Dialog

`GearSettingsProfileManager` (extends the abstract `ProfileManagerCrudApplication` from f1) provides:

- **Profile list:** All profiles displayed with name, author, built-in badge, last updated
- **Activate button:** Sets profile as active (with conflict detection flow)
- **Create button:** Opens a form to create a new custom profile (starts as copy of current)
- **Edit button:** Opens profile for editing (built-in profiles are read-only)
- **Export button:** Downloads selected profile as `.json`
- **Import button:** File picker or paste area for profile JSON
- **Delete button:** Removes custom profile (built-in profiles cannot be deleted)

### 3.6 Localization Keys

| Key | Default text |
|-----|-------------|
| `pf2e-kingmaker-tools.gearSettings.title` | Gear Settings |
| `pf2e-kingmaker-tools.gearSettings.activeProfile` | Active Profile |
| `pf2e-kingmaker-tools.gearSettings.noProfile` | RAW (No Profile) |
| `pf2e-kingmaker-tools.gearSettings.manageProfiles` | Manage Profiles |
| `pf2e-kingmaker-tools.gearSettings.conflict.title` | Settings Conflict Detected |
| `pf2e-kingmaker-tools.gearSettings.conflict.description` | Your Foundry settings differ from the active profile. |
| `pf2e-kingmaker-tools.gearSettings.conflict.useProfile` | Use Profile Value |
| `pf2e-kingmaker-tools.gearSettings.conflict.keepFoundry` | Keep Foundry Value |
| `pf2e-kingmaker-tools.gearSettings.conflict.useAll` | Use All Profile Values |
| `pf2e-kingmaker-tools.gearSettings.export` | Export Profile |
| `pf2e-kingmaker-tools.gearSettings.import` | Import Profile |
| `pf3e-kingmaker-tools.gearSettings.activate` | Activate |
| `pf2e-kingmaker-tools.gearSettings.create` | New Custom Profile |
| `pf2e-kingmaker-tools.gearSettings.edit` | Edit Profile |
| `pf2e-kingmaker-tools.gearSettings.delete` | Delete Profile |
| `pf2e-kingmaker-tools.gearSettings.badge.raw` | RAW |
| `pf2e-kingmaker-tools.gearSettings.badge.vk` | V&K |
| `pf2e-kingmaker-tools.gearSettings.badge.gregory` | Gregory |
| `pf2e-kingmaker-tools.gearSettings.badge.custom` | Custom |

*(Note: 20 keys total covering profile selection, conflict dialog, and profile manager UI.)*

---

## Section 4: Rule-Resolution Helper

### 4.1 Purpose

`RuleResolutionHelper` (`commonMain`) is the **single resolution layer** between the active profiles and four consuming systems:

| System | Consumes settings for |
|--------|----------------------|
| **Camping** | Activity count (party-size vs. fixed-4), sheltered, weather |
| **Kingdom** | Ruin threshold, event DC, leadership caps, structure bonus cap, capital growth, cult events, non-capital upgrades, settlement influence |
| **Hex** | Random combat suppression, travel cost, paved streets |
| **Army** | Combat music tracks |

All four systems previously read `KingdomSettings` booleans/int fields directly. The helper centralizes those reads behind a profile-aware interface so switching between RAW and Gregory's rules is a single profile activation.

### 4.2 Resolution Precedence

```
custom profile overrides  >  V&K preset values  >  RAW defaults
```

The helper is a **pure-function object**: every method is a stateless function of `(profile, ...contextParams)`. No caching, no mutation. The `profile` parameter is always passed in by the caller.

**Precedence per setting:**

| Setting | Custom profile | Gregory (V&K) | RAW default |
|---------|---------------|---------------|-------------|
| `ruinThreshold` | `profile.rules.ruinThreshold` | `5` | `10` |
| `eventDc` | `profile.rules.eventDc` | `5` | `15` |
| `leadershipActivityCap` | `profile.rules.leadershipActivityCap` | `8` | `6` |
| `leadershipActivityCapWithTownhall` | `profile.rules.leadershipActivityCapWithTownhall` | `12` | `8` |
| `campingActivityCountByPartySize` | `profile.rules.campingActivityCountByPartySize` | `true` | `false` |
| `noRandomCombatInClaimedHexes` | `profile.rules.noRandomCombatInClaimedHexes` | `true` | `false` |
| `capStructureBonusAtKingdomLevel` | `profile.rules.capStructureBonusAtKingdomLevel` | `true` | `false` |
| `canUpgradeNonCapital` | `profile.rules.canUpgradeNonCapital` | `true` | `false` |
| `capitalCanGrowOneSizeLarger` | `profile.rules.capitalCanGrowOneSizeLarger` | `true` | `false` |
| `cultOfTheBloomEvents` | `profile.rules.cultOfTheBloomEvents` | `true` | `false` |
| `travelCostRiverNoBridgeAdditional` | `profile.rules.travelCostRiverNoBridgeAdditional` | `1` | `0` |
| `pavedStreetsReduceTravelCost` | `profile.rules.pavedStreetsReduceTravelCost` | `true` | `false` |
| `settlementInfluenceRadius` | `profile.rules.settlementInfluenceRadius` | `1` | `0` |
| `enableWeather` | `profile.rules.enableWeather` | `false` | `true` |
| `enableCombatTracks` | `profile.rules.enableCombatTracks` | `true` | `true` |

### 4.3 Helper Interface Contract

```kotlin
// NOTE: This object is defined in the f1 Homebrew Rules Profile Plan
// (docs/plans/2026-06-01-homebrew-rules-profile-system.md, lines 188-243).
// The gear settings system calls the SAME object — it does not define its own.

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

**Calling convention:**

1. Resolve active profile once per operation: `RuleResolutionHelper.getActiveProfile(registry)`
2. Pass `HomebrewRulesProfile?` to each helper method
3. Never read `KingdomSettings.ruinThreshold` (or equivalent) directly — always go through the helper

**Handling missing/partial settings:**

- **Null profile** → RAW defaults (no exceptions)
- **Partial rules** → Kotlin data class constructor defaults fill missing fields from RAW
- **Invalid values** → Caught at import schema validation; helper assumes valid data

### 4.4 Consuming System Integration

**Camping system** (`CampingSheet.kt`, `CampingSheetDataModel.kt`):
```kotlin
val registry = game.settings.getObject("homebrewRulesProfileRegistry") as? HomebrewProfileRegistry
val activeProfile = RuleResolutionHelper.getActiveProfile(registry)
val activityCount = RuleResolutionHelper.getCampingActivityCount(activeProfile, party.actorUuids.size)
```

**Kingdom system** (scattered sites S1–S11, refactored in f1 Tasks 14–16):
```kotlin
val activeProfile = RuleResolutionHelper.getActiveProfile(registry)
val threshold = RuleResolutionHelper.getRuinThreshold(activeProfile)
val cap = RuleResolutionHelper.getLeadershipActivityCap(activeProfile, hasTownhallOrHigher)
```

**Hex system** (travel cost, random encounters):
```kotlin
val activeProfile = RuleResolutionHelper.getActiveProfile(registry)
val suppress = RuleResolutionHelper.isRandomCombatSuppressedInClaimedHexes(activeProfile)
val additionalCost = RuleResolutionHelper.getTravelCostRiverNoBridgeAdditional(activeProfile)
val pavedDiscount = RuleResolutionHelper.doPavedStreetsReduceTravelCost(activeProfile)
```

### 4.5 Data Flow Diagram

```
┌──────────────────────────────────────────────────────────┐
│  Foundry ClientSettings (world scope)                     │
│  homebrewRulesProfileRegistry  ← f1 registry              │
│  gearSettingsProfileRegistry   ← this system's registry   │
└──────────────────┬───────────────────────────────────────┘
                   │ game.settings.getObject()
                   ▼
┌──────────────────────────────────────────────────────────┐
│  Consuming code (one-time per operation)                  │
│  val registry = settings.getObject("homebrewRules...")   │
│  val activeProfile = RuleResolutionHelper                 │
│      .getActiveProfile(registry)                          │
└──────────────────┬───────────────────────────────────────┘
                   │ pass activeProfile
       ┌───────────┬┴──────────┬──────────────┐
       ▼           ▼           ▼              ▼
  ┌──────────┐ ┌──────────┐ ┌────────┐ ┌──────────┐
  │ Camping  │ │ Kingdom  │ │  Hex   │ │  Army    │
  │ Sheet    │ │ Systems  │ │ Travel │ │ Combat   │
  └────┬─────┘ └────┬─────┘ └───┬────┘ └────┬─────┘
       │             │            │            │
       └─────────────┴─────┬──────┴────────────┘
                           ▼
  ┌────────────────────────────────────────────────────┐
  │  RuleResolutionHelper (object, commonMain)         │
  │  — SAME object as defined in f1 homebrew plan      │
  │  — 13 pure-function methods                        │
  │  — precedence: custom > V&K > RAW                  │
  └──────────────────────┬─────────────────────────────┘
                         │ profile?.rules ?: HomebrewRules.none()
                         ▼
  ┌────────────────────────────────────────────────────┐
  │  HomebrewRules (data class, commonMain) — from f1  │
  │  — all fields have RAW defaults                    │
  │  — HomebrewRules.gregory() for V&K                 │
  │  — HomebrewRules.none() for RAW                    │
  └────────────────────────────────────────────────────┘
```

### 4.6 Test Strategy

**Unit tests** (`RuleResolutionHelperTest.kt`, `commonTest`) — ~24–30 tests, one per method per variant:

```kotlin
class RuleResolutionHelperTest {
    private val gregoryProfile = HomebrewRulesProfile(
        id = "gregory-v1", name = "Gregory's House Rules",
        isActive = true, createdAt = "2026-06-01T00:00:00Z",
        updatedAt = "2026-06-01T00:00:00Z", rules = HomebrewRules.gregory(),
    )

    @Test fun `ruin threshold - Gregory returns 5`() =
        assertEquals(5, RuleResolutionHelper.getRuinThreshold(gregoryProfile))

    @Test fun `ruin threshold - null profile returns 10`() =
        assertEquals(10, RuleResolutionHelper.getRuinThreshold(null))

    @Test fun `camping activity count - Gregory uses party size`() {
        assertEquals(3, RuleResolutionHelper.getCampingActivityCount(gregoryProfile, 3))
        assertEquals(5, RuleResolutionHelper.getCampingActivityCount(gregoryProfile, 5))
    }
    // ... remaining methods
}
```

**Integration tests** (`jsTest`):

| Test Class | Scope |
|------------|-------|
| `CampingActivityCountIntegrationTest.kt` | Camping sheet uses helper for activity count |
| `RuinThresholdIntegrationTest.kt` | Kingdom ruin system reads from helper |
| `StructureBonusCapIntegrationTest.kt` | Structure eval respects helper |
| `SettingsRegistrationIntegrationTest.kt` | Registry setting writable, defaults to NONE |
| `ImportExportIntegrationTest.kt` | Round-trip: export → import → verify |

**Build verification:**
```
commonTest   → HomebrewPreset, HomebrewRules, schema validation tests
jsTest       → I1 (camping), I2 (ruin), I3 (structure), I4 (settings), I5 (import/export)
compileKotlinJs → no errors
grep verification → no direct KingdomSettings reads for homebrew fields
```

---

## Acceptance Checklist

- [ ] JSON Schema files are valid draft 2020-12
- [ ] Kotlin data classes compile in `commonMain`
- [ ] All three built-in profiles (RAW, V&K, Gregory) validate against the schema
- [ ] Overlapping fields with f1 `HomebrewRules` are identical in type and semantics
- [ ] No duplication of f1 structures — only cross-references
- [ ] Version migration strategy covers additive and breaking changes
- [ ] Semantic validation rules are testable
- [ ] Import/export round-trip preserves all settings
- [ ] First-run auto-import creates viable default profile from current settings
- [ ] All consuming systems (camping, kingdom, hex, army) read through `RuleResolutionHelper`
- [ ] No remaining direct `KingdomSettings` reads for homebrew fields
- [ ] Profile activation conflict detection works
- [ ] All test classes listed in test strategy exist and pass
