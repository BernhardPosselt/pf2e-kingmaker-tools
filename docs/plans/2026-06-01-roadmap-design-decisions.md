# Roadmap Design Decisions

**Date:** 2026-06-01
**Status:** Recommended defaults pending Gregory approval
**Sources:** [`docs/feature-roadmap.md`](../feature-roadmap.md), [`docs/migration-verification-report.md`](../migration-verification-report.md)

This document consolidates all open design decisions from the feature roadmap (5 items) and the migration verification report (4 items) into a single reference. Each decision is numbered, presents the available options, and ends with a recommended default.

---

## Part A — Feature Design Decisions

### Decision 1: Homebrew Rules Profile Scope

**Context:** The roadmap proposes a homebrew rules profile system (Feature 9 in [`docs/feature-roadmap.md`](../feature-roadmap.md#9-homebrew-rules-profile-system)) to let the GM switch between RAW and custom rules without code edits. Before implementation starts, the scope of the first profile must be decided.

| # | Option | Trade-off |
|---|--------|-----------|
| A | Build a full multi-profile system from the start (import/export, versioning, multiple named presets) | Higher upfront cost; future-proof but YAGNI if only one profile exists |
| B | Single hardcoded "Gregory" profile now, with a clear seam (interface/wiring) to add multi-profile later | Lowest cost now; generalization is additive and doesn't require refactoring the core rule-resolution path |

**Recommended default: B — Single Gregory profile now, seam for generalization later.**
The house rules are stable and there is no second use case yet. The rule-resolution helper (`RuleProfile.resolve(...)`) should be parameterized from day one so that the profile is passed in rather than hardcoded, but the settings UI can be a single toggle (RAW vs. Gregory) without import/export machinery. When a second group needs their own profile, the wiring is already in place — only the storage/UI layer expands.

---

### Decision 2: Quest Visibility Default

**Context:** The quest/event generator (Feature 2 in [`docs/feature-roadmap.md`](../feature-roadmap.md#2-questevent-generator-tied-to-kingdom-events)) creates quests from kingdom events. The question is whether auto-generated quests are visible to players by default.

| # | Option | Trade-off |
|---|--------|-----------|
| A | Generated quests are player-visible by default | Risk of spoilers — kingdom events may reveal plot information prematurely (e.g., "Troll Sightings" quest tells players about Hargulka before the GM is ready) |
| B | Generated quests are GM-only by default; GM clicks "Reveal" to make visible | Prevents spoilers; requires one extra GM action but aligns with how the quest model already distinguishes GM notes from player text |

**Recommended default: B — GM-only by default, one-click reveal to players.**
The `RawQuest` model already has a visibility distinction. Auto-generated quests should be treated as GM-facing notes until the GM explicitly shares them. A single "Reveal" action (flipping a `visibleToPlayers` flag) is sufficient — no need for per-player visibility at this stage.

---

### Decision 3: Campaign Clock Strictness

**Context:** The campaign timeline feature (Feature 1 in [`docs/feature-roadmap.md`](../feature-roadmap.md#1-campaign-timeline-and-pressure-clock-dashboard)) adds deadline clocks. The question is whether clocks enforce consequences automatically or merely display warnings.

| # | Option | Trade-off |
|---|--------|-----------|
| A | Strict automation — clocks auto-advance state when they expire (unrest increases, threats trigger, NPCs act) | Highest campaign impact; GM must trust the system or review carefully before committing to a clock's expiry action |
| B | Advisory warnings only — clocks display visual urgency but require GM manual action | Safest; but warnings become noise over time (the GM learns to ignore them), defeating the purpose of a pressure system |
| C | Hybrid — strict automation with a per-clock "pause on expiry" override (GM reviews before consequence fires) | Best of both worlds for important clocks; adds a small amount of state (paused vs. running per clock) |

**Recommended default: C — Strict automation with opt-in per-clock "soft-pause".**
By default, clocks tick and fire consequences. For narrative-critical deadlines (e.g., Varnhold Vanishing), the GM can toggle "pause on expiry" so the clock stops and waits for GM confirmation before applying consequences. This avoids the "warning fatigue" of a fully advisory system while keeping control where it matters most. The `TurnTickingEngine` already supports optional/per-item overrides in its tick operations.

---

### Decision 4: Hex Content Storage and Sync

**Context:** The hex content manager (Feature 3 in [`docs/feature-roadmap.md`](../feature-roadmap.md#3-hex-content-and-discovery-manager)) tracks what each hex contains. The question is where the authoritative data lives and how it syncs to the Foundry scene.

| # | Option | Trade-off |
|---|--------|-----------|
| A | Scene drawings as source of truth; kingdom actor reads from them | Conflicts with the existing actor-driven architecture; requires reading from Foundry drawings (fragile, UI-dependent) |
| B | Kingdom actor as source of truth; scene drawings are a visualization layer (actor wins on mismatch) | Aligns with the existing pattern (`HexGridSync.kt` already pushes actor → scene); reverse sync (scene → actor) is a clean pattern inversion |
| C | Both directions of sync (actor ↔ scene) | Maximum complexity; risk of sync loops; no clear benefit since GMs don't edit hex state via drawings |

**Recommended default: B — Kingdom actor is source of truth, scene drawings are a visualization layer.**
The `HexGridSync.kt` machinery already pushes `claimed` state from the kingdom actor to Foundry scene drawings. The same pattern extends to `explored` and `cleared` states. If a mismatch is detected (e.g., manual scene edit), the actor wins and overwrites. Reverse sync is not needed — hex state is set programmatically through claiming/exploring actions, never by editing scene drawings directly.

---

### Decision 5: Session Prep Output Format

**Context:** The session prep dashboard (Feature 10 in [`docs/feature-roadmap.md`](../feature-roadmap.md#10-session-prep-and-recap-dashboard)) aggregates campaign data for the GM before a session. The question is whether it produces structured data or generated prose.

| # | Option | Trade-off |
|---|--------|-----------|
| A | Full prose generation from the start (LLM or template-based natural language) | Higher initial cost; prose quality varies; harder to test; external dependency if LLM-based |
| B | Structured aggregation only (lists of open quests, active clocks, unresolved events, nearby hex hooks, companion moments) with a later "Generate Narrative" button | Immediate value today; testable; zero external deps; prose can be added later as an additive layer |
| C | Hybrid — structured data with a simple template-based prose renderer (no LLM) | Moderate cost; still testable; but templates are brittle and require ongoing maintenance |

**Recommended default: B — Structured aggregation first, prose generation as a designed seam.**
Deliver a read-only GM dashboard that surfaces open quests, active clocks, unresolved kingdom events, nearby hex hooks, and companion moments as structured lists. This is immediately useful and fully testable without external dependencies. Behind a "Generate Narrative" button, a prose layer can be added later — either template-based or LLM-assisted — that reads the same structured context. The data contract between the aggregation engine and the prose layer is the key design seam.

---

## Part B — Migration Open Questions

### Decision 6: Explored/Cleared/Roads Visual Sync

**Context:** Verified in the migration report (Question 1 in [`docs/migration-verification-report.md`](../migration-verification-report.md#open-questions-for-gregory)): Only `claimed` hex state is synced to Foundry scene drawings (`HexGridSync.kt`). The `explored`, `cleared`, and `roads` fields have data models but no sync logic.

| # | Option | Trade-off |
|---|--------|-----------|
| A | Sync all four states (`explored`, `cleared`, `claimed`, `roads`) to scene drawings | Maximum visual fidelity; but 4 overlapping drawing layers per hex risks visual clutter and rendering conflicts |
| B | Sync `explored` + `cleared`, skip `roads` | Covers the two new states that were already implemented in code; roads adds little visual value (players can see road tiles on the map image itself) |
| C | No additional sync — keep `claimed`-only | Simplest; but leaves already-implemented `explored`/`cleared` states invisible to players |

**Recommended default: B — Sync explored + cleared, skip roads.**
`Explored` and `cleared` are already implemented in the data model and partially implemented in sync code. Completing their sync adds meaningful gameplay visibility (players see what they've explored and cleared). Roads are already visible as map tiles and adding a 4-layer drawing stack per hex for minimal visual gain is not worth the complexity. The `HexGridSync.kt` handler should draw one composite label/icon per hex reflecting the highest-priority state, not stacked layers.

---

### Decision 7: Injury and Weather Tick Location

**Context:** Verified in the migration report (Question 2 in [`docs/migration-verification-report.md`](../migration-verification-report.md#open-questions-for-gregory)): The original QA report listed injury durations and weather shifts as "NOT FOUND" in `TurnTickingEngine`. Subsequent code review confirmed both are now implemented and tested.

| # | Option | Trade-off |
|---|--------|-----------|
| A | Injury and weather tick through `TurnTickingEngine` alongside other duration-based state | Unified tick path; consistent with the pure-function design; already implemented and tested |
| B | Injury and weather tick through a separate `CampingTickEngine` or event-driven system | Unnecessary separation; duplicates tick logic; no architectural benefit |
| C | No programmatic ticking — GM manually resolves on each End Turn | Defeats the purpose of automation; manual tracking is error-prone for weather and injury durations |

**Recommended default: A — Keep both in TurnTickingEngine.**
Both injury duration decrements and weather shift logic are implemented in `TurnTickingEngine.kt` and covered by the test suite (`TurnTickingEngineTest.kt`). The original QA report is stale on this item. The migration-verification-report.md should be updated from "NOT FOUND" to "PASS" for this item. No separate ticking system is needed or warranted.

---

### Decision 8: Write-Only Companion Sync

**Context:** Verified in the migration report (Question 3 in [`docs/migration-verification-report.md`](../migration-verification-report.md#open-questions-for-gregory)): The companion roster uses write-only sync (`setAppFlag` writes companion data to actor flags, but no `getAppFlag` read exists). The roster is built from `kingdom.companions`, not from actor flags.

| # | Option | Trade-off |
|---|--------|-----------|
| A | Add bidirectional sync (companion actor edits propagate back to kingdom roster) | Most flexible; but adds complexity (merge conflict resolution, sync ordering); GMs currently manage companions from the kingdom sheet, not actor sheets |
| B | Keep write-only sync, but remove the dead `setAppFlag` writes (no reader exists) | Cleanest architecture; remove dead code; the appFlag data is never read anywhere and is misleading |
| C | Keep write-only sync as-is (with the dead flag writes) | Works today; but the dead `setAppFlag("companion-data")` writes are confusing for future maintainers |

**Recommended default: B — Keep write-only sync architecture, remove dead setAppFlag writes.**
The write-only pattern is correct for the current GM-driven workflow (GMs manage companions from the kingdom sheet). However, the `setAppFlag("companion-data")` writes in `Kingdom.kt` are dead code — no corresponding `getAppFlag` reader exists. Removing them eliminates confusion without changing any behavior. The `companion-data` appFlag field should be cleaned up in a future migration or code pass.

---

### Decision 9: Chosen Feats and Settlement Item Levels

**Context:** Verified in the migration report (Question 4 in [`docs/migration-verification-report.md`](../migration-verification-report.md#open-questions-for-gregory)): `chosenFeats` is computed in `KingdomSheet.kt` but not passed to `settlements.toContext()`. Settlement item levels are derived purely from structures via `parseAvailableItems()`.

| # | Option | Trade-off |
|---|--------|-----------|
| A | Thread `chosenFeats` through `toContext()` and add feat-based item level bonuses | Feels comprehensive; but no published PF2e Kingmaker feat modifies item levels — it would be implementing a rule that doesn't exist |
| B | Keep structure-only item levels; no feat involvement | Rules-correct; YAGNI; structures are the only published source of item bonuses in settlements |
| C | Add a homebrew-specific hook for feat-based item levels (disabled by default) | Supports hypothetical future homebrew; but adds code complexity for a feature that doesn't exist in any published or house rule set |

**Recommended default: B — Structure-only, no feat involvement.**
No published PF2e Kingmaker feat modifies settlement item levels. The structure-only approach via `parseAvailableItems()` / `EvaluateStructures.kt` is rules-correct. If a future homebrew rule grants item level bonuses from feats, a seam can be added to `evaluateSettlement()` at that point — but implementing it now is pure YAGNI. The existing `chosenFeats` computation in `KingdomSheet.kt` continues to serve its actual purpose (unrest, RP, and other kingdom stat calculations) without being threaded to item levels.

---

## Decision Summary

| # | Decision Area | Recommendation |
|---|---------------|----------------|
| 1 | Homebrew profile scope | Single Gregory preset now, seam for multi-profile |
| 2 | Quest visibility | GM-only by default, one-click reveal |
| 3 | Campaign clock strictness | Strict automation with per-clock soft-pause override |
| 4 | Hex content storage | Kingdom actor is source of truth, scene drawings are visualization layer |
| 5 | Session prep output | Structured aggregation first, prose generation behind "Generate Narrative" seam |
| 6 | Hex visual sync | Sync explored + cleared, skip roads |
| 7 | Injury/weather tick | Keep in TurnTickingEngine (already implemented) |
| 8 | Companion sync | Keep write-only, remove dead setAppFlag writes |
| 9 | Feats → item levels | Structure-only, no feat involvement (rules-correct, YAGNI) |
