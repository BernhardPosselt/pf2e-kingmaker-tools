# Gear Settings Profiles — Import/Export Section

> **Part of:** Gear Settings Profile System Plan (t_41de528b)
> **Cross-references:** Homebrew Rules Profile Plan (f1) `docs/plans/2026-06-01-homebrew-rules-profile-system.md`

---

## 1. Serialization Format

All gear settings profiles serialize to a single JSON object conforming to a versioned JSON Schema (draft 2020-12). The schema lives at:

```
src/commonMain/resources/schemas/gear-settings-profile.json
```

### 1.1 Top-Level Structure

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "title": "Gear Settings Profile",
  "type": "object",
  "required": ["id", "name", "version", "preset", "settings"],
  "properties": {
    "id": { "type": "string", "minLength": 1 },
    "name": { "type": "string", "minLength": 1 },
    "version": { "type": "integer", "minimum": 1 },
    "preset": {
      "type": "string",
      "enum": ["raw", "vk", "gregory", "custom"]
    },
    "description": { "type": ["string", "null"] },
    "createdAt": { "type": "string" },
    "updatedAt": { "type": "string" },
    "settings": { "$ref": "#/$defs/gearSettings" }
  }
}
```

### 1.2 Settings Object

The `settings` object is a **flat key-value map** of setting key to serialized value. Each key corresponds to a registered Foundry `ClientSettings` key in the module. Values are stored as their JSON-native types (boolean, integer, string).

This approach mirrors f1's `rules` sub-object pattern but differs in that gear settings are a **flat map of well-known keys** rather than a structured Kotlin data class. This is intentional: the settings system registers keys individually, so profile import must be key-aware rather than deserializing into a single typed object.

```json
{
  "id": "gregory-gear-v1",
  "name": "Gregory's Gear",
  "version": 1,
  "preset": "gregory",
  "settings": {
    "vanceAndKerensharaXP": true,
    "ruinThreshold": 5,
    "eventDc": 5,
    "eventDcStep": 0,
    "leadershipActivityCap": 8,
    "leadershipActivityCapWithTownhall": 12,
    "campingActivityCountByPartySize": true,
    "noRandomCombatInClaimedHexes": true,
    "capStructureBonusAtKingdomLevel": true,
    "canUpgradeNonCapital": true,
    "capitalCanGrowOneSizeLarger": true,
    "cultOfTheBloomEvents": true,
    "travelCostRiverNoBridgeAdditional": 1,
    "pavedStreetsReduceTravelCost": true,
    "settlementInfluenceRadius": 0,
    "enableWeather": true,
    "enableSheltered": false,
    "advancement": "xp",
    "weatherRollMode": "gmroll"
  }
}
```

### 1.3 Shared Profile Skeleton (from f1)

The `id`, `name`, `version`, `preset`, `description`, `createdAt`, `updatedAt`, and top-level schema structure are **identical** to the homebrew rules profile schema defined in f1 (section 7 / Task 18). Import/export code **reuses the same base skeleton** — the only difference is the payload (`settings` map vs. `rules` object). See cross-reference table below.

| Field | Gear Settings Profile | Homebrew Rules Profile (f1) |
|-------|----------------------|---------------------------|
| Top-level metadata | Same | Same |
| Payload key | `settings` (flat map) | `rules` (structured object) |
| Preset enum | `raw`, `vk`, `gregory`, `custom` | `none`, `gregory` |
| Schema file | `gear-settings-profile.json` | `homebrew-profile.json` |
| Validation | Key whitelist + type check | JSON Schema `additionalProperties: false` |

---

## 2. File Structure

### 2.1 Profile Registry File

Profiles are stored in Foundry world-scoped `ClientSettings` under the key `gearSettingsProfileRegistry`, registered as `DataModel` type `GearSettingsProfileRegistry`:

```
gearSettingsProfileRegistry (Object, world-scoped)
├── version: Int                    // registry schema version
├── activeProfileId: String?        // currently active profile ID
└── profiles: Array<GearSettingsProfile>
```

This mirrors f1's `HomebrewProfileRegistry` structure. Both registries live under the same `ClientSettings` infrastructure but use separate keys.

### 2.2 Individual Profile File (Export)

When exported, a single profile is written as a standalone `.json` file named:

```
gear-profile-{id}-v{version}.json
```

Example: `gear-profile-gregory-gear-v1-v1.json`

The file contains only the profile object (not the registry wrapper), so it is portable and shareable.

---

## 3. Version Compatibility Handling

### 3.1 Schema Versioning

The `version` field on the profile is the **profile schema version**, not the module version. It starts at `1` and increments when:

- A new setting key is added to the known settings list
- A setting key is removed or renamed
- The default preset values change

### 3.2 Import Validation (3-tier)

When a profile is imported, validation runs in three tiers:

**Tier 1 — Structural Validation:**
JSON is well-formed and conforms to `gear-settings-profile.json` schema. Rejects: missing required fields, wrong types, unknown preset value.

**Tier 2 — Key Whitelist Validation:**
Every key in the `settings` map is checked against the **known settings registry** at import time. Unknown keys are rejected with the key name in the error message. This prevents importing stale keys from outdated profiles.

**Tier 3 — Value Range Validation:**
Integer values are checked against their registered min/max. Enum strings are checked against registered choices. This mirrors the Foundry `ClientSettings` validation behavior.

### 3.3 Version Migration on Import

If a profile's `version` is older than the current known version, a **migration function** runs before acceptance:

```
migrateGearProfile(profile: Json, fromVersion: Int): Json
```

Migration is additive-only for V1 (the initial version). When V2 arrives, a `when(fromVersion)` block applies sequential transforms. Each transform is a pure function `Json -> Json`. This mirrors the project's existing numbered migration pattern (`Migration26.kt`, `Migration27.kt`).

### 3.4 Compatibility Matrix

| Import Version | Module Version | Behavior |
|---------------|---------------|----------|
| V1 | Current | Import directly |
| V1 | Future (V2+ settings added) | Import + warn: "N setting keys have no counterpart in current version" |
| Future | Current (older) | Reject: "Profile version {n} is newer than module version" |

---

## 4. Migration Path from Current Workbook Settings

### 4.1 Current State

Settings are registered individually via `ClientSettings.registerScalar`, `.registerBoolean`, `.registerEnum`, etc. in `Pfrpg2eKingdomCarmingWeatherSettings.register()` and `KingdomSettingsApplication`. Each kingdom has its own copy of settings; there is no concept of a named, shareable profile.

### 4.2 Migration Strategy (Non-Breaking)

Migration is **additive and non-breaking**. Existing settings continue to work as-is. The profile system sits on top:

**Step 1 — Registry Setup (Migration29):**
Migration29 creates the `gearSettingsProfileRegistry` setting if it does not exist, initializes it with `version: 1`, `activeProfileId: null`, `profiles: []`.

**Step 2 — Default Profile Auto-Import (First Run):**

On first run after migration, if `profiles` is empty, the system creates a default profile from current settings:

```kotlin
// Pseudocode for first-run auto-import
val currentSettings = snapshotCurrentSettings()
val defaultProfile = GearSettingsProfile(
    id = "default-v1",
    name = "Default (Current Settings)",
    version = 1,
    preset = detectPreset(currentSettings),  // "raw" if all defaults
    settings = currentSettings,
    createdAt = now(),
    updatedAt = now()
)
registry.profiles = listOf(defaultProfile)
registry.activeProfileId = defaultProfile.id
```

The `detectPreset()` function compares current setting values against known RAW and Gregory defaults. If all values match RAW, preset is `"raw"`. If all match Gregory, preset is `"gregory"`. Otherwise, `"custom"`.

**Step 3 — Read Path Interception:**

After the profile system is live, all consuming code reads from the active profile's `settings` map instead of calling `game.settings.getXxx()` directly. This is done via a `GearSettingsResolutionHelper` (see t_e279401e), which mirrors f1's `RuleResolutionHelper` pattern.

**Step 4 — Write Path:**

When a GM changes a setting in the Kingdom Settings dialog, the change is written to the **active profile** (not directly to Foundry settings). On save, the active profile's settings are diffed against Foundry's current values, and only changed keys are written to `game.settings`. This keeps Foundry settings in sync as a cache.

### 4.3 Offline/Workbook Settings

The Excel workbook (`Copy of Royal Kingdom Sheet for Pathfinder 2E - CURRENT.xlsx`) does **not** need to be modified. The profile system is a Foundry-module-level feature. The workbook serves as a reference doc for house rule values; those values are now also available as importable JSON profiles.

---

## 5. Export Flow

### 5.1 GM-Triggered Export

From the `GearSettingsProfileManager` dialog (mirrors f1's `HomebrewProfileManager`):

1. GM selects a profile.
2. Click "Export" → serializes the profile object to JSON.
3. Triggers browser download or copies to clipboard (via Foundry's `ClipboardHelper` or `saveDataToFile`).

### 5.2 Export Format

The exported JSON is **pretty-printed, 2-space indented**, sorted by key. The `isActive` flag is **not** exported — it is a registry-level concern, not a profile-level one.

### 5.3 What Gets Exported

Only the `settings` keys that differ from RAW defaults are included in the export by default. This keeps exported profiles small and focused. A "Full Export" toggle exports all keys.

---

## 6. Import Flow

### 6.1 GM-Triggered Import

From the `GearSettingsProfileManager` dialog:

1. GM clicks "Import" → text area or file picker opens.
2. Paste or select JSON file.
3. Tier 1/2/3 validation runs (section 3.2).
4. If validation passes: profile is added to the registry (with a new UUID-based `id` to avoid collisions).
5. GM is prompted: "Activate this profile now?" (default: yes).
6. If "yes": `activeProfileId` is set, `GearSettingsResolutionHelper` updates, all systems use new values immediately.

### 6.2 Conflict Detection

If an imported profile has the same `id` as an existing profile, the GM is warned: "A profile with ID '{id}' already exists. Replace, rename, or cancel?"

### 6.3 Import Error Messages

| Condition | Message |
|-----------|---------|
| Invalid JSON | "Invalid profile: not valid JSON" |
| Missing required field | "Invalid profile: missing '{field}'" |
| Unknown preset | "Invalid profile: unknown preset '{value}'" |
| Unknown setting key | "Invalid profile: unknown setting key '{key}' (remove or update profile)" |
| Value out of range | "Invalid profile: '{key}' value {v} out of min/max range [{min}, {max}]" |
| Future version | "Invalid profile: version {n} is newer than module-supported version {m}" |

---

## 7. Export/Share Use Cases

### 7.1 Sharing Rules with Another GM

1. GM exports their profile as `my-group-gear-v1.json`.
2. Shares the file (email, Discord, etc.).
3. Recipient imports via the Gear Settings Profile dialog.
4. All setting values are restored exactly.

### 7.2 RAW Baseline Distribution

A `raw-defaults-v1.json` file is included in the module's `data/` directory. GMs can import it to reset to RAW settings without manually toggling each one.

### 7.3 V&K Preset Distribution

A `vk-defaults-v1.json` preset file is included in `data/` with all Vance & Kerenshara setting values pre-configured (values sourced from the V&K document). Import gives V&K settings in one click.

---

## 8. Cross-References to f1 (Homebrew Rules Profile Plan)

| Topic | Gear Settings Plan (this doc) | f1 Homebrew Plan |
|-------|-------------------------------|------------------|
| Profile manager dialog | `GearSettingsProfileManager.kt` | `HomebrewProfileManager.kt` (Task 13) |
| JSON schema location | `gear-settings-profile.json` | `homebrew-profile.json` (Task 18) |
| Registry storage key | `gearSettingsProfileRegistry` | `homebrewRulesProfile` / `homebrewRulesProfileRegistry` |
| Export serialization | `ProfileExportSerializer.kt` (new) | Reuse same serializer with `settings` payload |
| Import validation | 3-tier (structural, key whitelist, range) | JSON Schema only (structured object) |
| Default profile seeding | First-run auto-import from current settings | Migration27 seeds Gregory profile |
| Resolution helper | `GearSettingsResolutionHelper` (t_e279401e) | `RuleResolutionHelper` |
| Integration tests | `GearSettingsImportExportIntegrationTest.kt` | `ImportExportIntegrationTest.kt` (I5) |
| TM check | M4/M5/M6 in f1 checklist apply | M4/M5/M6 |

**Code reuse:** The base `ProfileManagerCrudApplication` class (abstracted from the f1 `HomebrewProfileManager`) is shared. Both profile types extend it with their own serializer, validator, and dialog template.

---

## 9. Test Strategy

### 9.1 Unit Tests

| Test Class | Scope |
|-----------|-------|
| `GearSettingsProfileSchemaTest.kt` | JSON schema validation: valid profiles pass, invalid profiles fail with correct error |
| `GearSettingsProfilePresetTest.kt` | RAW/VK/Gregory preset values serialize correctly and match expected values |
| `GearSettingsProfileMigrationTest.kt` | V1→V2 migration adds/renames keys correctly |
| `GearSettingsImportValidationTest.kt` | Tier 2 key whitelist rejects unknown keys; Tier 3 range validation rejects out-of-range values |

### 9.2 Integration Tests

| Test Class | Scope |
|-----------|-------|
| `GearSettingsImportExportIntegrationTest.kt` | Round-trip: import → activate → export → verify JSON matches |
| `GearSettingsFirstRunMigrationTest.kt` | Migration29 + first-run auto-import creates default profile from current settings |
| `GearSettingsActivationIntegrationTest.kt` | Activate profile → all consuming systems read new values from resolution helper |

### 9.3 Test Fixtures

Preset JSON files live under `src/commonTest/resources/fixtures/gear-profiles/`:
- `raw-v1.json` — all RAW defaults
- `vk-v1.json` — all V&K values
- `gregory-v1.json` — all Gregory values
- `invalid-missing-version.json` — missing `version` field
- `invalid-unknown-key.json` — contains an unregistered setting key
