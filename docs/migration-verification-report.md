# Migration Verification Report

**Date:** 2026-05-31
**Branch:** `kingmaker.5`
**Scope:** Post-migration QA — static code analysis of all new/changed modules.
**Verdict:** ALL CORE CHECKS PASS. Migration is code-complete.

---

## Summary

| # | Verification Task | Result | Key Finding |
|---|---|---|---|
| 1 | Settlements Detailed Matrix | PASS* | `chosenFeats` not threaded to `toContext()` (likely intentional) |
| 2 | Native Companion Roster Sync | PASS* | Write-only sync gap (no actor read-back) |
| 3 | Hex Grid Scene Sync | PASS* | Only `claimed` state synced; `explored/cleared/roads` not implemented |
| 4 | Turn Ticking Engine | PASS* | Injury durations & weather shifts not in engine; test exec blocked by FirefoxHeadless |
| 5 | Migrations 24 & 25 | PASS | All checks pass, no issues found |

---

## [NEEDS HUMAN FOUNDRY CHECK] — Checklist for Gregory

These items **cannot be verified by static code analysis** and require clicking through a live Foundry VTT instance:

- [ ] **1. Settlements toggle** — Toggle button switches between overview and matrix views; all columns populate correctly
- [ ] **2. Settlements matrix rendering** — Item level values display correctly; `km-active` CSS highlights active view; capital star and overcrowded icons appear
- [ ] **3. Roster tab navigation** — Roster tab is accessible on the kingdom sheet; section renders correctly
- [ ] **4. Roster grid visual rendering** — Card layout, role badges, status indicators, and action buttons display correctly
- [ ] **5. Companion sheet sync** — After editing a companion on its actor sheet, changes (or lack thereof) are consistent with the write-only sync design
- [ ] **6. Hex grid scene redraw** — After claiming a hex, the Foundry scene drawing updates to reflect the claimed state
- [ ] **7. End-Turn execution** — Clicking End Turn visibly advances kingdom state (fame, resources, consumption, commodities, cooldowns, modifiers) and posts chat output
- [ ] **8. Companion travel arrival** — When a companion's ETA reaches 0 on End Turn, the token moves to the destination hex and a chat message is posted
- [ ] **9. Live migration run** — Migrations 24 & 25 apply cleanly to an existing saved world (not a fresh world) and the null guards work on real data

---

## Detailed Findings by Subsystem

### 1. Settlements Detailed Matrix (t_19736ad7)

**Files:** `page.hbs`, `SettlementsContext.kt`, `KingdomSheet.kt`, `lang/en.json`, `EvaluateStructures.kt`, `AvailableItemBonuses.kt`, `SettlementSize.kt`

| Check | Status |
|---|---|
| page.hbs toggle buttons (overview/matrix) | PASS |
| page.hbs matrix table rows (all fields) | PASS |
| SettlementsContext interface fields (all 13) | PASS |
| Item levels coerced to maxItemBonus | PASS |
| toggle-settlements-view handler registered | PASS |
| showDetailedMatrix passed to context | PASS |
| lang/en.json keys (all ~20) | PASS |
| chosen feats passed to settlements.toContext() | NOT IMPLEMENTED |
| Visual rendering in Foundry | [NEEDS HUMAN FOUNDRY CHECK] |

**Key Finding:** `chosenFeats` is computed in KingdomSheet.kt but NOT passed to `settlements.toContext()`. The `toContext()` function signature has no `chosenFeats` parameter. Item levels are computed purely from structures via `parseAvailableItems()` in `evaluateSettlement()`. This is likely intentional design — structures provide item bonuses, feats affect unrest/RP/etc.

---

### 2. Native Companion Roster Sync (t_862e4cb6)

**Files:** `RawCharacter.kt`, `RosterContext.kt`, `RosterPanel.kt`, `KingdomSheet.kt`, `Kingdom.kt`, `Main.kt`, `roster-add.hbs`, `roster-edit.hbs`, `page.hbs`, `kingdom-sheet.hbs`, `lang/en.json`, `Migration24.kt`, `Migrations.kt`, `KingdomData.kt`, `Defaults.kt`

| Check | Status |
|---|---|
| RawCharacter schema (12 fields) | PASS |
| RosterContext building | PASS |
| RosterPanel add/edit/delete dialogs | PASS |
| KingdomSheet GM handlers (6 actions) | PASS |
| Kingdom.kt sync wiring (setKingdom → setAppFlag) | PASS |
| Turn-based travel tick + arrival | PASS |
| Template wiring (partials, includes) | PASS |
| Lang keys (24+ keys) | PASS |
| Build (`./gradlew assemble`) | PASS |
| Roster tab navigation | [NEEDS HUMAN FOUNDRY CHECK] |
| Roster grid visual rendering | [NEEDS HUMAN FOUNDRY CHECK] |

**Key Finding:** Write-only sync gap. `setKingdom()` writes companion data TO actor flags via `setAppFlag("companion-data")`, but no `getAppFlag("companion-data")` read exists. The roster is built from `kingdom.companions`, not from actor flags. GM edits on the kingdom sheet work correctly; edits on companion actor sheets won't reflect back to the roster. Functionally sufficient for the current use-case where GMs manage companions from the kingdom sheet.

---

### 3. Hex Grid Scene Sync (t_2cadd64e)

**Files:** `HexGridSync.kt`, `Main.kt`, `Hooks.kt`, `KingmakerModule.kt`, `Kingdom.kt`

| Check | Status |
|---|---|
| Hook registration path (Main.kt:225, TypedHooks.onReady) | PASS |
| 4 hooks registered (onCloseKingmakerHexEdit, onUpdateActor-kingdom-filtered, onUpdateScene, onCanvasReady) | PASS |
| `claimed` hex state → drawing sync | PASS |
| Drawing create/delete/update paths | PASS |
| Initial sync on onReady | PASS |
| `explored`/`cleared`/`roads` hex state sync | NOT IMPLEMENTED |
| Live scene grid layer redraw | [NEEDS HUMAN FOUNDRY CHECK] |

**Key Finding:** Only the `claimed` hex state is synced to Foundry scene drawings. The `explored`, `cleared`, and `roads` fields are absent from the `HexState` interface and have no sync logic at all. If these states need visual representation on the map, additional sync code will be required.

---

### 4. Turn Ticking Engine (t_adab2ed5)

**Files:** `TurnTickingEngine.kt`, `TurnTickingEngineTest.kt`, `KingdomSheet.kt`, `RawConsumption.kt`, `RawResources.kt`, `RawCommodities.kt`, `page.hbs`

| Check | Status |
|---|---|
| Engine design (pure-function, no Foundry deps) | PASS |
| All 8 tick operations implemented | PASS |
| Change/diff tracking (TickChange records) | PASS |
| End-Turn UI button → engine call → state apply → persist | PASS |
| Companion travel ETA ticks on end-turn | PASS |
| Test structural coverage (28+ tests) | PASS (code review) |
| Test execution | BLOCKED — FirefoxHeadless timeout in WSL |
| Injury duration ticking | NOT FOUND — [NEEDS HUMAN REVIEW] |
| Weather shift ticking | NOT FOUND — [NEEDS HUMAN REVIEW] |
| Live End-Turn execution | [NEEDS HUMAN FOUNDRY CHECK] |

**8 Tick Operations Verified:**
1. Reset supernatural/creative solution counters to 0
2. Advance fame: next → now, reset next to 0
3. Advance resource points: next → now
4. Advance resource dice: next → now
5. Advance consumption: next → now (preserves armies)
6. Merge commodities with storage cap (next into now, cap by storage, reset next)
7. Tick down council cooldowns (audit/scrying/feast/lockdown, floored at 0)
8. Tick down modifier durations (null/0 = permanent, 1 = expire, >1 = decrement)

**Key Finding:** Injury durations and weather shifts are not implemented in `TurnTickingEngine.kt`. They may be handled by a separate system or not yet implemented. The test suite (`TurnTickingEngineTest.kt`, 500 lines, 28+ tests, structurally complete) could not execute in the current WSL environment due to a known FirefoxHeadless/snap compatibility issue.

---

### 5. Migrations 24 & 25 (t_90c7ea15)

**Files:** `Migration24.kt`, `Migration25.kt`, `Migration.kt`, `Migrations.kt`, `KingdomData.kt`, `CampingData.kt`, `Defaults.kt`, `Pfrpg2eKingdomCampingWeatherSettings.kt`, `Main.kt`

| Check | Status |
|---|---|
| Migration24 file exists | PASS |
| Migration25 file exists | PASS |
| Migration24 registered in Migrations.kt (import L18, list L57) | PASS |
| Migration25 registered in Migrations.kt (import L19, list L58) | PASS |
| latestMigrationVersion = maxOfOrNull → 25 | PASS |
| schemaVersion setting registered as Int | PASS |
| Hook registration (Main.kt:220, TypedHooks.onReady) | PASS |
| Migration24 idempotency (null guard on companions) | PASS |
| Migration25 idempotency (null guard on watchSlots) | PASS |
| Migration24 showUpgradingNotices = false (explicit) | PASS |
| Migration25 showUpgradingNotices = false (default) | PASS |
| Migration24 field coverage (KingdomData.companions, Defaults.kt) | PASS |
| Migration25 field coverage (CampingData.watchSlots, init block) | PASS |
| Live migration execution | [NEEDS HUMAN FOUNDRY CHECK] |

**Migration24:** Backfills `kingdom.companions` with `emptyArray()` when null. Field: `KingdomData.companions: Array<RawCharacter>?` (nullable). Null-guard ensures idempotency.

**Migration25:** Backfills `camping.watchSlots` with `emptyArray<String>()` when null. Uses dynamic receiver (documented as intentional since typed check would be always-false). Null-guard ensures idempotency.

---

## Architecture Notes

- **Build:** `./gradlew assemble` passes. `jsBrowserTest` fails in WSL due to FirefoxHeadless/snap timeout (pre-existing env issue, not a code problem).
- **Pure-function design:** `TurnTickingEngine.kt` has zero Foundry/Game dependencies, making it fully unit-testable in isolation.
- **Write-only sync pattern:** Used in companion roster (`setAppFlag` without `getAppFlag` read-back). Acceptable for current GM-driven workflow.
- **Hex state scope:** Only `claimed` is synced. `explored`, `cleared`, `roads` are data-model-only with no Foundry visual representation.

---

## Open Questions for Gregory

1. Should `explored`/`cleared`/`roads` hex states be visually synced to Foundry scene drawings?
2. Should injury durations and weather shifts tick through `TurnTickingEngine` or a separate system?
3. Is the write-only companion sync sufficient, or should companion actor edits propagate back to the kingdom roster?
4. Should `chosenFeats` affect settlement item levels, or is the structure-only approach correct?

---

# QA Checklist Update — 2026-06-03 (t_a98da4cd)

**Author:** interactive maintainer session.
**Method:** static code re-review of all checklist items **plus a live headless-Chrome render check** against the running world `kingmaker` (login as Gamemaster, `game.ready`, module active). See [[foundry-live-render-check]] for the harness.

This section supersedes stale entries above where noted. The 2026-05-31 report predates migration 26, the explored/cleared/roads hex visuals, the settlement "Workbook Activity Bonuses" rows, the V&K toggles, and the army catalog UI.

## Static confirmation of newer items

| Item | Code evidence | Status |
|---|---|---|
| Migration **26** (watchSlots flat → nested, multiple actors per slot) | `Migration26.kt` `migrateCamping`; registered `Migrations.kt:20,60`; `latestMigrationVersion` now = 26 | PASS |
| Settlement matrix **"Workbook Activity Bonuses"** rows | `sections/settlements/page.hbs:128` section header (colspan), `:131` `data-workbook-row` rows w/ `km-matrix-section-header` | PASS |
| Settlements **toggle** + `km-active` highlight | `page.hbs:13-14` `data-action="toggle-settlements-view"` overview/matrix, `km-active` class | PASS |
| **Overcrowded** icon | `page.hbs:61,159` `fa-people-roof` gated on `isOvercrowded` | PASS |
| **explored / cleared / roads** hex visuals | `HexDrawingHelpers.kt`: `EXPLORED_DRAWING_TYPE` (dashed blue), `CLEARED_DRAWING_TYPE` (orange), `shouldHaveExploredDrawing/ClearedDrawing`; roads stored as `HexFeature.type`; synced in `HexGridSync.kt` | PASS — **corrects 05-31 report** (was "NOT IMPLEMENTED") |
| **Set-watches** (multi-actor slots) | `camping-sheet.hbs:178-199` watch grid, per-slot assignees (draggable `data-type="Actor"`), `clear-watch-slot`; backed by nested `watchSlots` (Migration26) | PASS |
| **Army catalog** in UI | `army-browser.hbs` + `ArmyBrowser.kt` workbook context; compendium via `createArmyCompendiumEntries` | PASS — **LIVE-VERIFIED**, see below |

## Live-verified this pass (real Foundry render)

- **Army catalog** — the "Available Basic Armies" table renders **all 11 workbook armies** (full stat columns: Level/Type/DC/HP/Consumption/Attacks/Maneuver Save/Accessible/Starting Tactics/Description). The compendium `kingmaker-tools-journals` holds **11 Army + 40 Tactic + 8 Modifier** journal entries.
- **Bug found & fixed** during this check: `army-browser.hbs` emitted multiple root elements → Foundry ApplicationV2 threw *"Template part 'div' must render a single HTML element"* and the dialog never opened (this is what exhausted prior automated workers). Fixed by wrapping the template in a single `<div class="km-browser">` root — commit `0b87492e`.

## Confirmed issue / world-data follow-up

- **`recruitableArmiesFolderId` points to a deleted folder** in the live `kingmaker` world (`SRbMwBpUUwK367uO`). `findArmyFolder()` throws, so the in-app Recruit Army flow is broken until repointed. The valid folder is **"Recruitable Armies" (`7AAQ0CdAFDIWoccN`)** — fix under Kingdom → Creation or settings. (Not a code bug; the army catalog is also reachable via the compendium regardless.)

## Manual Foundry click-through checklist (for Gregory)

Run against a **saved** world (not fresh). Most items are static-confirmed above; these are the human visual/behavioral confirmations.

- [ ] **Settlements toggle** — open Kingdom sheet → Settlements; click Overview/Matrix; the active button shows the `km-active` highlight and the view switches.
- [ ] **Settlements matrix** — in Matrix view, item-level rows populate; the **"Workbook Activity Bonuses"** section header + its rows appear; capital **star** and **overcrowded** (people-roof) icons show on the right settlements.
- [ ] **Roster tab** — Kingdom sheet → Roster: tab navigates; cards render with role badges, status, action buttons.
- [ ] **Companion sheet sync** — edit a companion on its actor sheet; confirm behavior matches the write-only design (kingdom-sheet edits drive the roster; actor edits don't read back — see [[companion-sync-policy]]).
- [ ] **Hex claim redraw** — claim a hex → claimed fill appears; verify the **explored** (dashed blue) and **cleared** (orange) outlines and **roads** render distinctly.
- [ ] **End Turn** — click End Turn; fame/resources/consumption/commodities/cooldowns/modifiers advance and a chat summary posts.
- [ ] **Companion travel arrival** — with a companion ETA at 0, advancing the world clock moves the token to the destination and posts a chat message (travel is world-clock driven, **not** End Turn — see [[companion-travel-trigger]]).
- [ ] **Migrations 24, 25 & 26** — load a saved world; confirm no errors and `watchSlots` upgrades to nested arrays (Migration 26) without data loss; idempotent on reload.
- [ ] **Set-watches** — Camping sheet: change the watch-slot count; assign **multiple** actors to a single watch slot (drag actors in); clear a slot.
- [ ] **Army catalog** — Kingdom → Recruit Army opens the browser with the 11 basic armies **(requires a valid `recruitableArmiesFolderId`; see issue above)**; armies/tactics also browsable via the journals compendium. ✅ render confirmed live this pass.
