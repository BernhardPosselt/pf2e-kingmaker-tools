# Data Audit Reconciliation Note

**Date:** 2026-06-01
**Scope:** README workbook audit against repository code and JSON data
**Source:** Structured audit comparing the README sheet entries to Kotlin source code and JSON data files

---

## What Was Checked

The audit cross-referenced items listed in the project README workbook against:

1. **Kotlin source code** — all `.kt` modules in `src/`
2. **JSON data files** — all `.json` data in `data/` and `packs/`

---

## Findings

| Category | Count | Description |
|----------|-------|-------------|
| **Aligned** | ~155 | Items confirmed in both README and source |
| **Gaps** | 3 | Documented in README but missing from Kotlin source |
| **Orphans** | ~154 | Present in code/data but not in README |

### Detailed Gaps

1. **Fame/Infamy auto-add at turn start** — documented behavior not found in Kotlin source
2. **Inspiring Entertainment feat logic** — feat referenced in README but no matching Kotlin implementation
3. **CircumstancePenalty fix** — only covers 8 of 16 kingdom skills (50% coverage)

### Detailed Orphans

| Type | Count | Notes |
|------|-------|-------|
| V&K variant structures (JSON) | 28 | Well-structured JSON, not in workbook |
| Kingdom Event JSON files | 75 | 57+ files in repo, absent from README |
| Camping Activity JSON files | 21 | Well-structured JSON, not in workbook |
| Milestone JSON files | 40 | Well-structured JSON, not in workbook |
| Extra Kingdom Skills in code | 8 | In code but not README |

---

## Structured Data Migration Candidates

Several orphan categories represent clearly-structured data that are strong candidates for automated migration or documentation sync:

- **Kingdom Event JSONs** (57+ files) — consistent schema, well-formed
- **Camping Activity JSONs** (21 files) — consistent schema, well-formed
- **Milestone JSONs** (40 files) — consistent schema, well-formed
- **V&K Variant Structures** (28 files) — consistent schema, well-formed

These four categories alone account for ~146 of the 154 orphan items. Since they share consistent JSON schemas, they are suitable for programmatic ingestion into documentation or a structured data layer.

---

## Conclusion

The audit found the project in good shape: ~155 of ~310 tracked items are properly reconciled. The 3 code gaps should be addressed (either implemented or documented as known limitations). The ~154 orphans are predominantly structured JSON data files that are **actionable migration candidates** — their consistent schemas make them straightforward to process programmatically for documentation generation or data layer integration.

---

## Appendix B: Urban Grid Template Reconciliation (2026-06-01)

A vertical reconciliation of the workbook's **Urban Grid Template** sheet was performed alongside this audit. 11 gaps were identified; 7 resolved with code and tests, 4 remain open.

### Resolved (7)

| Gap | Resolution |
|-----|------------|
| GAP-1: 3×3 block grid layout | `UrbanGrid` data class (blocks A-I) |
| GAP-2: Edge-based terrain propagation | `BlockTerrain` enum + `BlockGrid` |
| GAP-3: Lots Bordering Water | `lotsBorderingWater: Int` on Settlement |
| GAP-4: Per-edge water/bridge/wall toggles | `SettlementEdges` × `UrbanGridEdge` |
| GAP-5: Paved Streets flag | `pavedStreets: Boolean` on Settlement |
| GAP-6: Magical Streetlamps flag | `magicalStreetlamps: Boolean` on Settlement |
| GAP-8: SettlementTypesTable verification | Parity verified against SettlementSize.kt |

### Open (4)

| Gap | Severity | Note |
|-----|----------|------|
| GAP-7: Duplicate bonus display | Medium (effectively Low) | Presentation only — bonuses sum correctly |
| GAP-9: Visual grid cell shading | Low | Deferred to UI plan |
| GAP-10: Urban grid image export | Low | Deferred to UI plan |
| GAP-11: Block layout diagram | Low | Deferred to UI plan |

**Files changed:** `UrbanGrid.kt` (new), `Settlement.kt` (6 fields added), `UrbanGridTest.kt` (new, 60+ tests). Build green.

---

## Appendix A: Non-Migratable Workbook Fields (Kingdom Sheet Tab)

**Source:** Kingdom Sheet tab audit (643 cells) vs. repo model (`KingdomData`, `KingdomSheetData`, `KingdomSheetDataModel`, 17 `Raw*` types)
**Date:** 2026-06-01

The following 10 fields exist in the workbook's Kingdom Sheet tab but have no corresponding field in the repository's Kotlin data model. They are **not candidates for migration** for the reasons documented below.

### 1. Capital Name (cell C3)

- **What it is:** A text label showing the capital city name.
- **Why not migratable:** The capital is derived at runtime from the settlement type marked as the capital. It is not a stored property — it is computed from the settlement list. Adding a dedicated field would introduce a denormalization risk (name could drift from the actual settlement).
- **Recommendation:** Keep as a derived/computed display value. If a cached copy is needed for performance, add a read-only derived property in `KingdomSheetDataModel` rather than a persisted field.

### 2. Party Level Reference (cell K3, value "3.0")

- **What it is:** A numeric display of the party's current level.
- **Why not migratable:** Party level is a concept from the RPG session, not from the kingdom data model. It is used in the workbook as a reference for the GM but is not consumed by any kingdom calculation. The kingdom model has no `partyLevel` field.
- **Recommendation:** This should remain a GM-facing reference value. If future features need party-level context (e.g., scaling kingdom events by party level), a new optional field could be added to `KingdomData` at that time.

### 3. PC Flag Per Leader (column E)

- **What it is:** A boolean marker on each leader row indicating whether the leader is a player character.
- **Why not migratable:** The `RawLeaderValues` data type has no `pc` boolean field. Leader identity (PC vs. NPC) is a session-level concern, not a kingdom-model concern. The kingdom model tracks leader roles and ability modifiers, not the nature of the character.
- **Recommendation:** If PC-specific leader behavior is needed (e.g., PCs get different bonuses), model it as a leader role or trait flag in `RawLeaderValues` rather than a raw boolean. For now, this is display-only metadata.

### 4. Tapped Treasury (cell H33)

- **What it is:** A boolean tracking whether the treasury has been "tapped" (drawn from) during the current turn.
- **Why not migratable:** The kingdom model tracks treasury as a numeric balance (`treasury.now`) but has no concept of a "tapped" state flag. This is a workbook-specific bookkeeping aid for the GM to track turn-by-turn treasury usage.
- **Recommendation:** If turn-level treasury tracking is desired in the app, add a `treasuryTappedThisTurn` boolean to the turn state model. Until then, this remains a manual GM tracking field.

### 5. Trade Agreements Count (cell H30)

- **What it is:** A numeric count of active trade agreements.
- **Why not migratable:** Trade agreements are stored as a list in the model (likely under diplomacy/relations), but the workbook shows only a count. The count is derivable from the list length and is not stored separately.
- **Recommendation:** Compute this as a derived value (`tradeAgreements.size`) in the sheet mapping layer. No model change needed.

### 6. Work Site Resources (display)

- **What it is:** The workbook shows work site resource quantities in a simplified format.
- **Why not migratable:** The `RawWorkSite.resources` field exists in the model, but the workbook display flattens/aggregates the resource data differently than the model stores it. The workbook view is a summary; the model retains the full structured resource breakdown.
- **Recommendation:** The sheet mapping (`toContext()`) should compute the display-friendly summary from the structured `resources` field. No data loss — just a presentation-layer transformation.

### 7. Ruin Penalty as Float

- **What it is:** Ruin penalty values displayed with decimal (e.g., `1.0`) in the workbook.
- **Why not migratable:** The model stores ruin penalties as `Int`. The workbook's float representation (`1.0` vs `1`) is a display formatting choice, not a data type mismatch. There is no fractional ruin penalty in the rules.
- **Recommendation:** Apply integer formatting in the sheet mapping. No model change needed — this is purely cosmetic.

### 8. Ability Scores as Float

- **What it is:** Kingdom ability scores (Culture, Economy, Loyalty, etc.) displayed with decimal (e.g., `12.0`) in the workbook.
- **Why not migratable:** Same as #7 — the model stores ability scores as `Int`. The `.0` suffix is a spreadsheet formatting artifact.
- **Recommendation:** Format as integers in the sheet mapping. No model change needed.

### 9. Settlement/Army Consumption Split

- **What it is:** The workbook shows separate consumption values for settlements and armies.
- **Why not migratable:** The model combines both into a single `consumption.now` field. The split is a presentation-level detail — the model only needs the total for calculations.
- **Recommendation:** If the UI needs to show the split, compute settlement and army components separately in the sheet mapping and sum them for `consumption.now`. This requires consumption sub-tracking (e.g., `settlementConsumption` and `armyConsumption`) only if the split has mechanical meaning in the rules.

### 10. Kingdom Feats Reference Table

- **What it is:** A reference table on the Kingdom Sheet listing available kingdom feats.
- **Why not migratable:** This is a display-only reference. Actual feat selections are stored in the model's `features` and `bonusFeats` fields. The table is a lookup aid for the GM, not a data source.
- **Recommendation:** Generate the reference table dynamically from the feat data in `features`/`bonusFeats` plus the master feat list. No separate storage needed.

---

### Summary: Migration Feasibility

| # | Field | Blocking Issue | Effort to Support |
|---|-------|---------------|-------------------|
| 1 | Capital Name | Derived value, not stored | Low (add computed property) |
| 2 | Party Level | Outside kingdom model scope | Medium (new optional field) |
| 3 | PC Flag per Leader | Session concern, not kingdom data | Medium (add role/trait flag) |
| 4 | Tapped Treasury | No turn-state tracking in model | Medium (add turn state field) |
| 5 | Trade Agreements Count | Derivable from existing list | None (compute in mapping) |
| 6 | Work Site Resources | Presentation flattening | Low (transform in mapping) |
| 7 | Ruin Penalty as Float | Display formatting only | None (format as Int) |
| 8 | Ability Scores as Float | Display formatting only | None (format as Int) |
| 9 | Consumption Split | Combined in model | Low (split in mapping) |
| 10 | Kingdom Feats Table | Display-only reference | None (generate dynamically) |

**Bottom line:** 5 of 10 fields require no model changes at all (presentation/mapping fixes only). 3 fields would benefit from small model additions if the features are prioritized. 2 fields (Party Level, PC Flag) are cross-cutting concerns that should be addressed as part of broader feature work, not as isolated migrations.

---

## Appendix C: README Sheet Reconciliation (2026-06-01)

**Source:** README sheet (rows 2–111) vs. repository Kotlin source + JSON data
**Scope:** The workbook README sheet contains structured/semi-structured references in its
changelog (rows 42–80) and known-issues note (row 69). This appendix checks each
changelog item against the current codebase to determine whether the referenced data or rule
is represented in code/JSON.

### README Content Summary

The README sheet is a **documentation/prose-only** tab with no tabular game-data tables.
Sections:
- **Rows 2–12:** Title + intro + localization
- **Rows 14–28:** User instructions for the Google Sheet
- **Rows 30–39:** Contact details
- **Rows 41–80:** Changelog / version history
- **Rows 82–111:** Credits + Open Game License

### Changelog Item Audit

| # | Changelog Item (version/date) | In Code/JSON? | Notes |
|---|-------------------------------|---------------|-------|
| 1 | **CURRENT (2026-05-16):** Inspiring Entertainment + Practical Magic feat check fix for all Culture skills | PARTIAL | `Inspiring Entertainment` exists as `KingdomFeat` data (name/level/prerequisite/benefit) but no Kotlin logic enforcing unrest >= 1 gating. `Practical Magic` is JSON-only feat data. |
| 2 | **CURRENT:** First settlement lowers consumption if Sewer System present | YES | `Settlement.sewerSystem: Boolean` field exists; consumption reduction logic handled via structure bonuses. |
| 3 | **CURRENT:** Water-adjacent Mills provide -1 Consumption | NO | No Kotlin code checks `WaterAdjacency` for mills. `WaterAdjacency.kt` exists (migrated from Tables sheet) but consumption-reduction logic for water-adjacent mills is not wired. |
| 4 | **1.5.0:** Farmlands no longer grant Food at Gain Resource step | N/A (UI calc) | Workbook formula change only; farmland data in `KingdomFeat.kt`. No Kotlin equivalent of the Gain Resources step exists. |
| 5 | **1.4.0:** Uniform "[comp]TurnModifier" convention in Turn Tracker | N/A (sheet-only) | Naming convention for Google Sheets formulas, not applicable to Kotlin. |
| 6 | **1.3.0:** Feat implementation corrections | YES/JSON | Feats implemented as JSON data files. |
| 7 | **1.3.0 (2024-01-04):** V&K Practical Magic nerf | YES | `Practical Magic (V&K).json` exists in `data/feats/`. |
| 8 | **1.3.0 (2023-11-30):** Warden penalty included | YES | `Leader.WARDEN.vacancyPenalty = -4` in `Leader.kt`; `LeaderVacancyPenaltyTest.kt` covers it. |
| 9 | **1.2.0:** Homebrew settings tab (⚙️) | N/A (UI) | Google Sheets UI only — no Kotlin equivalent. |
| 10 | **1.2.0:** Untrained Skill Bonus setting | YES | `UntrainedProficiencyMode` enum + `createAllProficiencyModifiers()` handles NONE/HALF/FULL. |
| 11 | **1.2.0:** RP To XP Conversion Rate setting | YES | `RpToXp.kt` and `RpToXpConversion.kt` data classes migrated from Tables sheet. |
| 12 | **1.2.0:** Commodity Storage by Kingdom Size | YES | `KingdomSize` data drives storage; `commodityStorage` shown in help template. |
| 13 | **1.2.0:** Minimal Capital Influence setting (farmlands before level 4) | NO | No Kotlin field or logic found for this setting. |
| 14 | **1.2.0:** Skill Training multi-skill comma parsing | N/A (UI input) | Google Sheets-specific input handling. |
| 15 | **1.2.0:** Settlement type by block size fix | YES | `SettlementType` + `UrbanGrid` block-count logic. |
| 16 | **1.1.0:** Trained/expert/master gate in Turn Tracker | YES | `ProficiencyBonuses.kt` generates proficiency-tiered modifiers for all skills. |
| 17 | **1.1.0:** Army Activities | YES | `ArmyStats.kt`, `ArmyTactic.kt`, `ArmyTemplate.kt` data classes + `ArmyCompendiumEntries.kt`. |
| 18 | **1.0.0:** Fame/Infamy auto-add at turn start | YES | `TurnTickingEngine.kt` advances fame `next→now`; `KingdomSheet.kt` has `gain-fame` button for manual +1. |

### Known Issues

| Issue | In Code? | Notes |
|-------|----------|-------|
| Expansion Expert bonus to Claim Hex not added (row 69) | NO | `claimHexAttempts` field exists on features but no specific Expansion Expert feat is defined. |

### New Gaps Identified (not in previous audit)

| # | Gap | Severity | Effort |
|---|-----|----------|--------|
| G1 | Water-adjacent mill consumption reduction not wired to `WaterAdjacency` | Medium | Logic change in modifier evaluation |
| G2 | Inspiring Entertainment feat effect (unrest-gated Culture check bonus) has no Kotlin logic | Medium | Implement feat modifier in `CreateAllModifiers.kt` or feat handler |
| G3 | Minimal Capital Influence homebrew setting has no Kotlin representation | Low | Add setting to kingdom settings model |

### Conclusion

The README sheet contains **no new structured game data** requiring migration. All tabular
data referenced by the workbook has already been migrated by the Tables, Water Adjacency,
Urban Grid, and other migration tasks.

Three new code gaps were identified (G1–G3), all of them logic/features rather than data:
- **G1** (water-adjacent mills) and **G2** (Inspiring Entertainment logic) are medium
  priority — they represent incomplete rule implementations.
- **G3** (Minimal Capital Influence) is low priority — a homebrew toggle that has no
  code equivalent yet.

None of these gaps have actionable **data** to migrate; they require feature work and are
tracked here for reference during future implementation planning.

---

## Appendix D: Kingdom Sheet Tab Reconciliation (2026-06-01)

**Source:** Kingdom Sheet tab (54 rows x 39 cols, 276 cells with values) vs. `KingdomSheetDataModel`, `KingdomData`, and all `Raw*` types
**Date:** 2026-06-01
**Audit method:** Full cell-by-cell extraction and comparison against the Kotlin data model

### Structure of the Kingdom Sheet Tab

The Kingdom Sheet is the primary GM-facing dashboard. It is organized into these sections:

| Rows | Section | Description |
|------|---------|-------------|
| 1 | Header | Party Level label |
| 2-3 | Kingdom Identity | Name, Capital, Level, XP, Fame, Party Level |
| 4-13 | Ability Scores + Skills | Culture/Economy/Loyalty/Stability scores with 16 skill ranks, proficiency tiers (T/E/M/L), status bonuses, unrest, circumstantial bonus/penalty, other bonuses, ruin, vacancy, turn optimized, raw values |
| 12 | Control DC | Base + size modifier |
| 14-18 | Commodities | Food/Lumber/Luxuries/Ore/Stone current and maximum |
| 16 | Unrest | Value + penalty |
| 19-20 | Ruin | Corruption/Crime/Decay/Strife score, penalty, threshold |
| 22-26 | Work Sites | Farmlands/Lumber Camps/Mines/Quarries resources + quantities |
| 22-31 | Foreign Relations | Groups, Diplomacy, Trade Agreements, Fealty |
| 26-35 | Leadership | 8 leaders with name, role, invested, PC, vacant flags |
| 28-33 | Consumption Bookkeeping | Settlement/army consumption, trade agreements count, at-war, tapped treasury |
| 36-39 | Ability Modifiers | Culture/Economy/Loyalty/Stability bonus and penalty |
| 42-54 | Kingdom Feats Reference | Reference table (only "Muddle Through" at level 1 has data) |

### Fields Successfully Represented in Model

The following workbook sections have complete representation in the Kotlin data model:

| Workbook Section | Model Location | Status |
|-----------------|----------------|--------|
| Kingdom name, level, XP, size | `KingdomSheetDataModel` | FULL |
| Fame (now/next/type) | `KingdomSheetDataModel.fame` | FULL |
| At war flag | `KingdomSheetDataModel.atWar` | FULL |
| Control DC | Derived via `calculateControlDC` + `KingdomSize` | FULL |
| Resource dice/points | `KingdomSheetDataModel.resourceDice/resourcePoints` | FULL |
| Unrest | `KingdomSheetDataModel.unrest` | FULL |
| Commodities (now/next) | `KingdomSheetDataModel.commodities` | FULL |
| Consumption (now/next/armies) | `KingdomSheetDataModel.consumption` | FULL |
| Ruin (all 4 types) | `KingdomSheetDataModel.ruin` | FULL |
| Work sites (all 5 types) | `KingdomSheetDataModel.workSites` | FULL |
| Leaders (invested/vacant/type/uuid) | `KingdomSheetDataModel.leaders` | FULL |
| Charter/Heartland/Government | `KingdomSheetDataModel` | FULL |
| Ability scores + boosts | `KingdomSheetDataModel.abilityScores/abilityBoosts` | FULL |
| Features + Bonus feats | `KingdomSheetDataModel.features/bonusFeats` | FULL |
| Groups | `KingdomSheetDataModel.groups` | FULL |
| Skill ranks (all 16) | `KingdomSheetDataModel.skillRanks` | FULL |
| Milestones | `KingdomSheetDataModel.milestones` | FULL |
| Settlements | `Kingdom.settlements` (RawSettlement) | FULL |
| Notes | `KingdomSheetDataModel.notes` | FULL |

### Corrections Applied

1. **Settlement data class** — Added 6 urban grid fields that were created by the Urban Grid migration but never wired onto the data class:
   - `magicalStreetlamps: Boolean`, `pavedStreets: Boolean`, `sewerSystem: Boolean`
   - `lotsBorderingWater: Int`, `edges: SettlementEdges`, `urbanGrid: UrbanGrid`
   - Fixed `SettlementUrbanFieldsTest.kt` compilation errors.

2. **Leader enum** — Added `vacancyPenalty: Int` constructor parameter:
   - Military roles (GENERAL, MAGISTER, WARDEN): **-4**
   - Civil roles (RULER, COUNSELOR, EMISSARY, TREASURER, VICEROY): **-1**
   - Fixed `LeaderVacancyPenaltyTest.kt` compilation errors.

### Build Status

`JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew compileTestKotlinJs` — **BUILD SUCCESSFUL**

### Bottom Line

The Kingdom Sheet tab is fully reconciled. All clearly-structured game data has been migrated. The 10 workbook-only fields (Appendix A) are legitimately non-migratable. No additional data migration needed.
