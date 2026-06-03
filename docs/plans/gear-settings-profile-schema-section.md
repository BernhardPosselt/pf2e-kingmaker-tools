# Gear Settings Profile System — Schema Section

> **Parent plan:** [Plan: workbook gear settings-profile system](t_41de528b)
> **Sibling sections:** Settings UI (t_3b6dceba) | Import/Export (t_d5edbbde) | Rule-Resolution Helper (t_e279401e)
> **Assembler:** t_3b4c06d4 will merge all sections into the final plan doc.

---

## 1. schema overview

The gear settings profile system uses **versioned JSON profiles** to encapsulate all configurable "gear" settings — the toggleable module settings that control kingdom, camping, hex, army, and weather behavior. Profiles are stored as Foundry `ClientSettings` using `registerDataModel` (for the active profile) and `registerObject` (for the profile registry).

Three profile variants exist:

| Variant | ID | Description |
|---------|----|-------------|
| RAW | `raw` | Unmodified Pathfinder 2e Kingmaker rules |
| V&K | `vance-kerenshara` | Vance & Kerenshara community house rules |
| Gregory/custom | `gregory` (default custom) | Gregory's house rules, user-editable |

Each profile is a **versioned JSON document** following a strict schema. Version fields allow forward-compatible migration when new settings are added.

---

## 2. profile data model

### 2.1 top-level profile container

```
GearSettingsProfile
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `schemaVersion` | `integer` | yes | Schema format version (current: `1`). Increment when fields are added/removed. |
| `id` | `string` | yes | Unique profile identifier. One of: `raw`, `vance-kerenshara`, `gregory`, or a custom UUID. |
| `name` | `string` | yes | Display name shown in the settings UI. |
| `description` | `string` | no | Longer description for the profile tooltip. |
| `author` | `string` | no | Creator name (for shared profiles). |
| `isBuiltin` | `boolean` | yes | `true` for RAW and V&K (immutable). `false` for custom/Gregory. |
| `createdAt` | `string` (ISO 8601) | yes | Creation timestamp. |
| `updatedAt` | `string` (ISO 8601) | yes | Last modification timestamp. |
| `settings` | `GearSettings` | yes | The actual configuration values. |

### 2.2 GearSettings (settings bag)

The `settings` object groups all configurable toggles by subsystem. Each group mirrors the corresponding Foundry `ClientSettings` key currently registered in `Pfrpg2eKingdomCampingWeatherSettings`, `KingdomSettings`, `CampingSettings`, etc.

```
GearSettings
```

#### 2.2.1 kingdom settings (group: "kingdom")

Maps to keys registered in `Pfrpg2eKingdomCampingWeatherSettings` and `KingdomSettings`.

| Field | Type | Default (RAW) | Description |
|-------|------|---------------|-------------|
| `advancement` | `Advancement` enum | `xp` | Advancement mode: `xp` or `milestone`. |
| `ruinThreshold` | `integer` | `10` | Ruin points before the kingdom collapses. |
| `eventDc` | `integer` | `15` | Base DC for kingdom events. |
| `eventDcStep` | `integer` | `0` | DC increment per event tier. |
| `leadershipActivityCap` | `integer` | `6` | Max leadership activities per turn (without Townhall+). |
| `leadershipActivityCapWithTownhall` | `integer` | `8` | Max leadership activities with Townhall/Castle/Palace. |
| `canUpgradeNonCapital` | `boolean` | `false` | Allow the "Improve Settlement" civic activity for non-capital settlements. |
| `capitalCanGrowOneSizeLarger` | `boolean` | `false` | Capital can grow to one size above normal limit. |
| `noRandomCombatInClaimedHexes` | `boolean` | `false` | Suppress random combat encounters in claimed hexes. |
| `capStructureBonusAtKingdomLevel` | `boolean` | `false` | Cap structure item bonuses at kingdom level. |
| `settlementInfluenceRadius` | `integer` | `0` | Settlements absorb nearby ruins/structures within this many hexes (0 = disabled). |
| `cultOfTheBloomEvents` | `boolean` | `false` | Enable Cult of the Bloom daily events during Season of Bloom. |

**Cross-reference with f1 (homebrew rules profile system):** These fields overlap directly with `HomebrewRules` in the homebrew rules profile plan. The gear settings profile schema **does not define** `HomebrewRules` — it references it. See section 4 for the shared structure link.

#### 2.2.2 camping settings (group: "camping")

Maps to camping-related keys.

| Field | Type | Default (RAW) | Description |
|-------|------|---------------|-------------|
| `campingActivityCountByPartySize` | `boolean` | `false` | Number of camping activities equals PC count instead of fixed 4. |
| `enableSheltered` | `boolean` | `false` | Allow the sheltered condition from camping. |
| `enableWeather` | `boolean` | `true` | Enable the weather subsystem. |

#### 2.2.3 hex settings (group: "hex")

Maps to hexploration/travel settings.

| Field | Type | Default (RAW) | Description |
|-------|------|---------------|-------------|
| `travelCostRiverNoBridgeAdditional` | `integer` | `0` | Additional travel cost when crossing a river without a bridge. |
| `pavedStreetsReduceTravelCost` | `boolean` | `false` | Paved streets reduce settlement travel cost to 1. |
| `hexMapEnabled` | `boolean` | `true` | Enable hex map/sync features. |

#### 2.2.4 army settings (group: "army")

Maps to army/combat toggle settings.

| Field | Type | Default (RAW) | Description |
|-------|------|---------------|-------------|
| `enableCombatTracks` | `boolean` | `true` | Play combat music tracks during army battles. |

#### 2.2.5 UI/misc settings (group: "ui")

Maps to client-facing toggles.

| Field | Type | Default (RAW) | Description |
|-------|------|---------------|-------------|
| `hideBuiltinKingdomSheet` | `boolean` | `false` | Hide the native Foundry kingdom sheet in favor of the module's. |
| `enablePartyActorIcons` | `boolean` | `true` | Show party member icons on the scene. |
| `enableWeatherSoundFx` | `boolean` | `true` | Play ambient weather sound effects. |
| `enableTokenMapping` | `boolean` | `true` | Enable token-to-actor mapping features. |
| `disableFirstRunMessage` | `boolean` | `false` | Suppress the first-run welcome message. |

### 2.3 profile registry

The registry stores all profiles and tracks which is active:

```
GearSettingsProfileRegistry
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `schemaVersion` | `integer` | yes | Registry format version (current: `1`). |
| `activeProfileId` | `string\|null` | yes | ID of the currently active profile. `null` == RAW. |
| `profiles` | `GearSettingsProfile[]` | yes | All available profiles. Always includes RAW and V&K. |

---

## 3. JSON Schema definition

### 3.1 gear-settings-profile.json

File: `src/commonMain/resources/schemas/gear-settings-profile.json`

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://posselt.at/pf2e-kingmaker-tools/schemas/gear-settings-profile.json",
  "title": "Gear Settings Profile",
  "description": "A versioned settings profile for the pf2e-kingmaker-tools module",
  "$ref": "#/$defs/GearSettingsProfile",
  "$defs": {
    "GearSettingsProfile": {
      "type": "object",
      "additionalProperties": false,
      "required": [
        "schemaVersion",
        "id",
        "name",
        "isBuiltin",
        "createdAt",
        "updatedAt",
        "settings"
      ],
      "properties": {
        "schemaVersion": {
          "type": "integer",
          "minimum": 1,
          "description": "Schema format version. Current: 1"
        },
        "id": {
          "type": "string",
          "pattern": "^[a-z0-9_-]+$",
          "description": "Unique profile identifier. Builtin: raw, vance-kerenshara, gregory. Custom: any slug."
        },
        "name": {
          "type": "string",
          "minLength": 1,
          "maxLength": 128
        },
        "description": {
          "type": "string",
          "maxLength": 512
        },
        "author": {
          "type": "string",
          "maxLength": 128
        },
        "isBuiltin": {
          "type": "boolean",
          "description": "Builtin profiles (raw, vance-kerenshara) cannot be edited or deleted."
        },
        "createdAt": {
          "type": "string",
          "format": "date-time"
        },
        "updatedAt": {
          "type": "string",
          "format": "date-time"
        },
        "settings": {
          "$ref": "#/$defs/GearSettings"
        }
      }
    },
    "GearSettings": {
      "type": "object",
      "additionalProperties": false,
      "properties": {
        "kingdom": {
          "$ref": "#/$defs/KingdomSettings"
        },
        "camping": {
          "$ref": "#/$defs/CampingSettings"
        },
        "hex": {
          "$ref": "#/$defs/HexSettings"
        },
        "army": {
          "$ref": "#/$defs/ArmySettings"
        },
        "ui": {
          "$ref": "#/$defs/UiSettings"
        }
      }
    },
    "KingdomSettings": {
      "type": "object",
      "additionalProperties": false,
      "properties": {
        "advancement": {
          "type": "string",
          "enum": ["xp", "milestone"]
        },
        "ruinThreshold": {
          "type": "integer",
          "minimum": 1,
          "maximum": 20
        },
        "eventDc": {
          "type": "integer",
          "minimum": 1,
          "maximum": 30
        },
        "eventDcStep": {
          "type": "integer",
          "minimum": 0,
          "maximum": 10
        },
        "leadershipActivityCap": {
          "type": "integer",
          "minimum": 1,
          "maximum": 20
        },
        "leadershipActivityCapWithTownhall": {
          "type": "integer",
          "minimum": 1,
          "maximum": 20
        },
        "canUpgradeNonCapital": {
          "type": "boolean"
        },
        "capitalCanGrowOneSizeLarger": {
          "type": "boolean"
        },
        "noRandomCombatInClaimedHexes": {
          "type": "boolean"
        },
        "capStructureBonusAtKingdomLevel": {
          "type": "boolean"
        },
        "settlementInfluenceRadius": {
          "type": "integer",
          "minimum": 0,
          "maximum": 5
        },
        "cultOfTheBloomEvents": {
          "type": "boolean"
        }
      }
    },
    "CampingSettings": {
      "type": "object",
      "additionalProperties": false,
      "properties": {
        "campingActivityCountByPartySize": {
          "type": "boolean"
        },
        "enableSheltered": {
          "type": "boolean"
        },
        "enableWeather": {
          "type": "boolean"
        }
      }
    },
    "HexSettings": {
      "type": "object",
      "additionalProperties": false,
      "properties": {
        "travelCostRiverNoBridgeAdditional": {
          "type": "integer",
          "minimum": 0,
          "maximum": 5
        },
        "pavedStreetsReduceTravelCost": {
          "type": "boolean"
        },
        "hexMapEnabled": {
          "type": "boolean"
        }
      }
    },
    "ArmySettings": {
      "type": "object",
      "additionalProperties": false,
      "properties": {
        "enableCombatTracks": {
          "type": "boolean"
        }
      }
    },
    "UiSettings": {
      "type": "object",
      "additionalProperties": false,
      "properties": {
        "hideBuiltinKingdomSheet": {
          "type": "boolean"
        },
        "enablePartyActorIcons": {
          "type": "boolean"
        },
        "enableWeatherSoundFx": {
          "type": "boolean"
        },
        "enableTokenMapping": {
          "type": "boolean"
        },
        "disableFirstRunMessage": {
          "type": "boolean"
        }
      }
    }
  }
}
```

### 3.2 gear-settings-registry.json

File: `src/commonMain/resources/schemas/gear-settings-registry.json`

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://posselt.at/pf2e-kingmaker-tools/schemas/gear-settings-registry.json",
  "title": "Gear Settings Profile Registry",
  "description": "Registry of all gear settings profiles and the active profile selection",
  "$ref": "#/$defs/GearSettingsProfileRegistry",
  "$defs": {
    "GearSettingsProfileRegistry": {
      "type": "object",
      "additionalProperties": false,
      "required": [
        "schemaVersion",
        "activeProfileId",
        "profiles"
      ],
      "properties": {
        "schemaVersion": {
          "type": "integer",
          "minimum": 1
        },
        "activeProfileId": {
          "type": ["string", "null"],
          "description": "ID of the active profile, or null for RAW defaults."
        },
        "profiles": {
          "type": "array",
          "items": {
            "$ref": "gear-settings-profile.json#/$defs/GearSettingsProfile"
          },
          "minItems": 2,
          "description": "Always contains at least raw and vance-kerenshara profiles."
        }
      }
    }
  }
}
```

---

## 4. cross-reference with f1 (homebrew rules profile system)

The homebrew rules profile plan (`docs/plans/2026-06-01-homebrew-rules-profile-system.md`, task f1) defines:

- `HomebrewRules` — a data class with the same kingdom rule fields that overlap with `GearSettings.kingdom`
- `HomebrewRulesProfile` — a profile container with `id`, `version`, `isActive`, `rules: HomebrewRules`
- `HomebrewProfileRegistry` — a registry with `activeProfileId`, `profiles`
- `RuleResolutionHelper` — a pure-function resolver

**The gear settings profile schema does NOT duplicate these structures.** Instead:

1. **Shared kingdom rule fields:** The `GearSettings.kingdom` group and `HomebrewRules` share the same field names and semantics. When both profiles are active, the rule-resolution helper (see sibling section t_e279401e) reads from whichever profile has precedence. The gear settings profile is the **storage format**; the homebrew rules profile is the **resolution layer**. The resolver merges them with the precedence: `custom > V&K > RAW`.

2. **Separate registries, shared resolver:** The gear settings registry (`GearSettingsProfileRegistry`) and the homebrew rules registry (`HomebrewProfileRegistry`) are stored as separate Foundry `ClientSettings` keys. The `RuleResolutionHelper` (f1) reads both and produces a unified `EffectiveSettings` view. See the rule-resolution helper section (t_e279401e) for the merge algorithm.

3. **Import/export:** Profile import/export uses the same serialization format for both systems. When a gear settings profile is exported, the overlapping kingdom fields can be imported into a homebrew rules profile and vice versa. See the import/export section (t_d5edbbde) for the conversion logic.

4. **Versioning:** Both schemas use `schemaVersion` for forward-compatible migration. Incrementing the gear settings schema version does not require incrementing the homebrew rules schema version — they are independently versioned.

### 4.1 fields that overlap with HomebrewRules

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

Fields marked "gear-only" do not overlap with `HomebrewRules` and are managed exclusively by the gear settings profile system.

---

## 5. profile variant definitions

### 5.1 RAW profile

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

### 5.2 V&K profile

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

Note: V&K primarily differs in the **Kotlin data files** (structure bonuses, XP tables, activity caps) that are toggled via Foundry data packs, not via the settings profile itself. The V&K profile serves as a marker that enables those data packs. See the import/export section (t_d5edbbde) for data pack activation semantics.

### 5.3 Gregory (custom default) profile

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

---

## 6. version migration strategy

When the schema version is incremented:

1. **Additive changes** (new fields): New fields get sensible defaults in existing profiles. The registry `schemaVersion` is bumped. Old profiles remain valid — missing fields are filled from RAW defaults at resolution time.

2. **Breaking changes** (removed/renamed fields): A migration function reads profiles at the old version, transforms them, and writes them at the new version. The migration is applied lazily on first profile load after a module update.

3. **Builtin profiles are regenerated:** When the schema version bumps, the RAW and V&K profiles are regenerated from the new Kotlin defaults. Custom profiles are migrated, not regenerated.

Migration lives in the same pattern as `Migration27` from f1:

```kotlin
// In GearSettingsProfileRegistry loading logic:
fun migrateProfiles(registry: GearSettingsProfileRegistry): GearSettingsProfileRegistry {
    return when (registry.schemaVersion) {
        1 -> registry  // current, no migration needed
        // future: 0 -> migrateV0ToV1(registry)
        else -> registry
    }
}
```

---

## 7. serialization format

Profiles are serialized as **pretty-printed JSON** (2-space indent) for human readability. The import/export section (t_d5edbbde) specifies the file format:

- Single profile export: `gear-profile-{id}-v{version}.json`
- Full registry export: `gear-settings-registry-v{version}.json`
- MIME type: `application/json`
- Encoding: UTF-8

---

## 8. storage in Foundry

| Key | Type | Storage | Scope | Purpose |
|-----|------|---------|-------|---------|
| `gearSettingsProfileRegistry` | `Object` | `ClientSettings.register` | `world` | Full profile registry |
| `gearSettingsActiveProfile` | `DataModel` | `ClientSettings.registerDataModel` | `world` | Active profile (denormalized for fast read) |

The active profile is **denormalized** into a separate DataModel for fast reads without deserializing the full registry on every settings check. When the active profile changes, both the registry and the denormalized copy are updated atomically.

---

## 9. schema validation

Validation happens at three levels:

1. **JSON Schema validation** on import: Incoming JSON is validated against `gear-settings-profile.json` using the project's existing JSON Schema validation infrastructure. Invalid profiles are rejected with a structured error listing the failing constraints.

2. **Semantic validation** on save: After JSON Schema validation, a semantic check verifies:
   - `leadershipActivityCapWithTownhall >= leadershipActivityCap` (the Townhall cap must be at least the base cap)
   - `ruinThreshold >= 1` (must be positive)
   - `settlementInfluenceRadius` has no overlapping settlement influence areas (checked at activation time, not import time)
   - Custom profiles do not use reserved IDs (`raw`, `vance-kerenshara`)

3. **Runtime validation** on profile activation: Before activating a profile, the system verifies that all required settings groups are present. Partial/missing groups are filled from RAW defaults. A warning is logged but activation proceeds.

---

## 10. Kotlin data classes (commonMain)

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
)

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

### companion factory methods

```kotlin
fun GearSettingsProfile.Companion.raw() = GearSettingsProfile(
    id = "raw",
    name = "RAW (Rules as Written)",
    isBuiltin = true,
    createdAt = Instant.now().toString(),
    updatedAt = Instant.now().toString(),
    settings = GearSettings(),  // all defaults
)

fun GearSettingsProfile.Companion.vanceKerenshara() = GearSettingsProfile(
    id = "vance-kerenshara",
    name = "Vance & Kerenshara",
    author = "Vance & Kerenshara",
    isBuiltin = true,
    createdAt = Instant.now().toString(),
    updatedAt = Instant.now().toString(),
    settings = GearSettings(),  // V&K differences live in data packs, not settings
)

fun GearSettingsProfile.Companion.gregory() = GearSettingsProfile(
    id = "gregory",
    name = "Gregory's Gear Settings",
    author = "Gregory",
    isBuiltin = false,
    createdAt = Instant.now().toString(),
    updatedAt = Instant.now().toString(),
    settings = GearSettings(
        kingdom = KingdomSettingsGroup(
            ruinThreshold = 5,
            eventDc = 5,
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
```

---

## 11. schema file manifest

New files to create:

| File | Purpose |
|------|---------|
| `src/commonMain/resources/schemas/gear-settings-profile.json` | JSON Schema (draft 2020-12) validating a single gear settings profile |
| `src/commonMain/resources/schemas/gear-settings-registry.json` | JSON Schema validating the profile registry |
| `src/commonMain/kotlin/at/posselt/pfrpg2e/gearsettings/GearSettingsProfile.kt` | Kotlin data classes for profile, settings groups, and registry |

Modified files: none (this section defines schema only; the settings UI section t_3b6dceba covers registration and UI)

---

## 12. acceptance checklist

- [ ] JSON Schema files are valid draft 2020-12 and pass schema validation
- [ ] Kotlin data classes compile in `commonMain`
- [ ] All three builtin profiles (RAW, V&K, Gregory) validate against the schema
- [ ] Overlapping fields with f1 `HomebrewRules` are identical in type and semantics
- [ ] No duplication of f1 structures — only cross-references
- [ ] Version migration strategy covers additive and breaking changes
- [ ] Semantic validation rules are documented and testable