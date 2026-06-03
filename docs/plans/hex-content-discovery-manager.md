# Hex Content & Discovery Manager Implementation Plan

> **For Hermes:** Use the subagent-driven-development skill to implement this plan task-by-task. PLAN ONLY — this document changes no production code.

**Goal:** Implement a Hex Content & Discovery Manager (roadmap item #3) that tracks what each hex *contains* (landmarks, refuges, worksites, resources, ruins, merchants, trainers, enemy armies), what players *know* about it (hidden → discovered → cleared), and how that content affects play (suppressing random encounters on claimed/cleared hexes, contributing travel-cost inputs, and seeding quests/events on discovery). It layers a **content + visibility** model on top of the existing native hex **state** (claimed/explored/cleared/roads) and the existing scene-drawing sync.

**Architecture:** A per-hex content registry lives on the **kingdom actor** as the authoritative source (Design Decision 4 = B), keyed by the native Kingmaker hex key (the same string keys used in `kingmaker.state.hexes`). Foundry scene drawings/markers are a **visualization layer** the actor pushes to (extending the existing `HexGridSync.kt` pattern); the actor always wins on mismatch. Pure, Foundry-free helpers in `commonMain` own visibility transitions, encounter-suppression, and travel-modifier aggregation so they are unit-testable. A GM-facing CRUD dialog edits hex content; a one-click "Reveal" flips visibility to players (mirroring Decision 2's quest pattern).

**Tech Stack:** Kotlin Multiplatform `commonMain`/`commonTest` (pure logic) + `jsMain`/`jsTest` (Foundry glue), Foundry ApplicationV2 dialogs, Handlebars templates, Gradle `compileKotlinJs` / `jsBrowserTest` (Chrome-headless in WSL — see `docs/...` build notes).

---

## Source of truth

- Roadmap item #3: [`docs/feature-roadmap.md`](../feature-roadmap.md) lines 82–105.
- Design decisions (card f0): [`docs/plans/2026-06-01-roadmap-design-decisions.md`](2026-06-01-roadmap-design-decisions.md) — **Decision 4** (hex content storage: kingdom actor is source of truth, scene is visualization) and **Decision 6** (sync explored + cleared, skip roads) are the governing decisions; **Decision 2** (GM-only-by-default + one-click reveal) is reused for the visibility default.
- Existing native hex state binding: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/map/HexGridSync.kt` (reads `kingmaker.state.hexes` / `kingmaker.region.hexes`; writes claimed/explored/cleared overlays + road segments as Drawing docs tagged with the `pf2e-kingmaker-tools` app flag `{type, hexKey, kingdomActor uuid}`).
- Pure hex visual helpers: `src/commonMain/kotlin/at/posselt/pfrpg2e/kingdom/map/HexDrawingHelpers.kt` (drawing constants + `shouldHave*Drawing` predicates).
- Kingdom data model: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/KingdomData.kt`; defaults in `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/Defaults.kt`; flag accessors in `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/Kingdom.kt` (`getKingdom`/`setKingdom` over the `kingdom-sheet` app flag; scope = `Config.moduleId`).
- Migration pattern: `src/jsMain/kotlin/at/posselt/pfrpg2e/migrations/migrations/Migration26.kt` (latest = 26); registry `src/jsMain/kotlin/at/posselt/pfrpg2e/migrations/Migrations.kt` (`latestMigrationVersion = migrations.maxOfOrNull { it.version }`).
- CRUD dialog reference: the roster panel (`src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/dialogs/RosterPanel.kt` + `roster-add.hbs` / `roster-edit.hbs`) and the army browser (`ArmyBrowser.kt`).
- Enum convention (`Translatable`, `ValueEnum`, `i18nKey`): `src/commonMain/kotlin/at/posselt/pfrpg2e/data/armies/ArmyType.kt`.
- Related plans for cross-feature seams: [`quest-event-generator.md`](quest-event-generator.md) (discovery → quest), `travel-route-planner.md` (card t_f84a5675; consumes travel-cost inputs), [`random-encounter-rumor-curator.md`](random-encounter-rumor-curator.md) (encounter filtering).

## Non-goals

- Do **not** re-implement native hex state. `claimed`/`explored`/`cleared`/roads come from the native Kingmaker module (`com.foundryvtt.kingmaker.HexState`); we read it. Per Decision 6 the explored+cleared *visual* sync is already done in `HexGridSync.kt` — extend, don't rewrite it.
- Do **not** build the full route/ETA calculator. This plan only *exposes* per-hex travel-cost inputs as a pure function; route planning is roadmap #4 (`travel-route-planner.md`).
- Do **not** build the quest/event generator. This plan only *fires a hook / writes a linkable quest stub* on discovery; quest generation logic is roadmap #2.
- Do **not** add reverse sync (scene drawing → actor). Decision 4 (B) and Decision 6 are explicit: actor is authoritative, no sync loops.
- Do **not** add per-player visibility. Visibility is binary GM-only vs. player-visible (Decision 2), not per-user.
- Do **not** modify camping/turn-ticking systems.

---

## Affected Files

### New files

| File | Purpose |
|------|---------|
| `src/commonMain/kotlin/at/posselt/pfrpg2e/data/hex/HexContentType.kt` | Enum `HexContentType` (LANDMARK, REFUGE, WORKSITE, RESOURCE, RUIN, MERCHANT, TRAINER, ENEMY_ARMY, CUSTOM) implementing `Translatable`+`ValueEnum` (i18nKey `hexContentType.<value>`) |
| `src/commonMain/kotlin/at/posselt/pfrpg2e/data/hex/HexContentVisibility.kt` | Enum `HexContentVisibility` (HIDDEN, DISCOVERED, CLEARED) + `Translatable`/`ValueEnum` |
| `src/commonMain/kotlin/at/posselt/pfrpg2e/kingdom/map/HexDiscovery.kt` | **Pure** helpers: `nextVisibility(current, event)` transition fn; `suppressesRandomEncounter(claimed, cleared, content)`; `aggregateTravelModifiers(features, content)`; `contentMarkerFor(content)` → which icon/label/priority to draw (single composite marker per hex, per Decision 6 "one composite, not stacked") |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/data/RawHexContent.kt` | `@JsPlainObject external interface RawHexContent` — the persisted per-hex content record (see Data Models) |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/dialogs/HexContentManager.kt` | GM CRUD dialog (ApplicationV2): list hexes-with-content, add/edit/delete, visibility toggle, one-click "Reveal" |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/contexts/HexContentContext.kt` | JS render context interfaces for the manager + add/edit forms |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/map/HexContentSync.kt` | Reads `kingdom.hexContents` (+ native `hexes` for geometry/state) and creates/updates/deletes per-hex content **marker** Drawing docs on the active scene; mirrors `HexGridSync.kt` app-flag conventions (`type="hexContent"`, `hexKey`, kingdomActor uuid) |
| `src/jsMain/resources/applications/kingdom/hex-content-manager.hbs` | Manager template (single root `<div>` — see UI gotcha) |
| `src/jsMain/resources/applications/kingdom/hex-content-add.hbs` | Add form partial |
| `src/jsMain/resources/applications/kingdom/hex-content-edit.hbs` | Edit form partial |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/migrations/migrations/Migration27.kt` | Backfill `kingdom.hexContents = emptyArray()` when null (idempotent null-guard, modeled on `Migration24`) |
| `src/commonTest/kotlin/at/posselt/pfrpg2e/kingdom/map/HexDiscoveryTest.kt` | Tests for the pure helpers (transitions, suppression, travel modifiers, marker priority) |
| `src/commonTest/kotlin/at/posselt/pfrpg2e/data/hex/HexContentEnumsTest.kt` | Enum `value`/`fromString`/`i18nKey` round-trips |
| `src/jsTest/kotlin/at/posselt/pfrpg2e/kingdom/HexContentManagerTest.kt` | Context building + add/edit/delete + reveal state transitions |
| `src/jsTest/kotlin/at/posselt/pfrpg2e/migrations/Migration27Test.kt` | Migration idempotency on null / already-migrated data |

### Modified files

| File | Change |
|------|--------|
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/KingdomData.kt` | Add `val hexContents: Array<RawHexContent>?` (nullable, for migration safety) |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/Defaults.kt` | Default `hexContents = emptyArray()` on new kingdoms |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/migrations/Migrations.kt` | `import ... Migration27` and add `Migration27()` to the list (drives `latestMigrationVersion` → 27) |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/KingdomSheet.kt` | Add `data-action="open-hex-content-manager"` handler → launches `HexContentManager`; on discovery/clear, call `HexContentSync` + fire the discovery hook |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/map/HexGridSync.kt` | After the existing claimed/explored/cleared sync, invoke `syncHexContentMarkers(...)` so content markers refresh on the same `onReady`/`onUpdateActor`/`onUpdateScene`/`onCanvasReady` triggers |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/Main.kt` | Ensure `HexContentSync` initial sync runs in the `onReady` block alongside `syncHexDrawingsToNativeState(game)` |
| `src/jsMain/resources/applications/kingdom/sections/turn/page.hbs` (or the map/hex section) | Add an "Open Hex Content Manager" button (GM-gated) |
| `src/jsMain/resources/lang/en.json` | New `kingdom.hexContent.*`, `hexContentType.*`, `hexContentVisibility.*` keys |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/dialogs/AddQuest.kt` (seam only) | Accept an optional pre-fill from a discovered hex (`hexKey`, name) so "create quest from discovery" links the two; no generation logic here |

> **Encounter-filtering seam:** the random-encounter roll path (compendium/rolltable invoked from the kingdom/camping flow) gains a guard that calls `suppressesRandomEncounter(...)`. Locate the exact call site during implementation (`grep -rn "random-encounter\|RandomEncounter\|rollTable" src/jsMain/kotlin`) and add the guard there rather than duplicating roll logic. Cross-reference `random-encounter-rumor-curator.md`.

---

## Data Models

### `RawHexContent` (`jsMain`, persisted on `KingdomData.hexContents`)

| Field | Type | Notes |
|-------|------|-------|
| `id` | `String` | stable id (e.g. `foundryId`-style) for CRUD addressing |
| `hexKey` | `String` | matches native `kingmaker.state.hexes` key; the join to geometry/state |
| `type` | `String` | a `HexContentType.value` |
| `name` | `String` | short label (GM + player) |
| `visibility` | `String` | a `HexContentVisibility.value`; **default `hidden`** (Decision 2) |
| `gmNotes` | `String` | GM-only rich text |
| `playerText` | `String` | shown once `discovered`/`cleared` |
| `suppressesEncounters` | `Boolean?` | per-content override; default derives from claimed/cleared via the pure helper |
| `travelModifier` | `Int?` | optional additive travel-cost contribution (roads/bridges normally negative) |
| `linkedQuestId` | `String?` | links a discovery to a `RawQuest` (quest generator seam) |
| `linkedUuid` | `String?` | optional Foundry doc/journal/actor link (e.g. enemy army actor, merchant journal) |
| `icon` | `String?` | optional FontAwesome class override for the scene marker |

- **Authoritative location:** `KingdomData.hexContents: Array<RawHexContent>?` on the kingdom actor's `kingdom-sheet` flag (Decision 4 = B). Never read content back from scene drawings.
- **Native state is referenced, not duplicated:** `claimed`/`explored`/`cleared`/roads stay in `kingmaker.state.hexes`; `RawHexContent` only stores module-specific content + visibility, joined by `hexKey`.

### Enums (`commonMain`)

- `HexContentType { LANDMARK, REFUGE, WORKSITE, RESOURCE, RUIN, MERCHANT, TRAINER, ENEMY_ARMY, CUSTOM }`
- `HexContentVisibility { HIDDEN, DISCOVERED, CLEARED }`
- Both follow the `ArmyType.kt` pattern: `value = toCamelCase()`, `fromString`, `i18nKey = "<group>.<value>"`. Keeps template `localizeKM`/Kotlin `t()` lookups consistent.

### Pure logic (`commonMain/.../HexDiscovery.kt`)

- `nextVisibility(current: HexContentVisibility, event: DiscoveryEvent): HexContentVisibility` — HIDDEN→DISCOVERED on discover; DISCOVERED/HIDDEN→CLEARED on clear; idempotent and monotonic (never regresses unless an explicit `reset`).
- `suppressesRandomEncounter(claimed: Boolean, cleared: Boolean, content: RawHexContent?): Boolean` — true when claimed or cleared, unless a content override says otherwise (roadmap: "claimed hexes suppress random combat encounters").
- `aggregateTravelModifiers(featureTypes: List<String?>, contents: List<RawHexContent>): Int` — sums road/bridge/settlement effects + content `travelModifier`; consumed later by the Travel planner.
- `contentMarkerFor(state, contents): MarkerSpec?` — picks **one** composite marker (icon + tint + priority) per hex (Decision 6: composite, not stacked layers).

---

## Migrations

- **`Migration27`** (`Migration(27)`): in `migrateKingdom`, if `kingdom.hexContents == null` set `emptyArray()`; otherwise leave as-is (idempotent). Mirrors `Migration24` exactly (companions backfill). `showUpgradingNotices = false`.
- Register in `Migrations.kt` (import + list entry); this advances `latestMigrationVersion` to **27** automatically via `maxOfOrNull`.
- **No destructive data change.** Existing worlds simply gain an empty content array; nothing about native hex state changes.
- **Ordering:** independent of 24/25/26 (different field); runs in version order with no cross-dependency.

---

## UI / Template Changes

- **Manager dialog** (`HexContentManager.kt` + `hex-content-manager.hbs`): GM-only. A table of hexes that have content (hexKey, type, name, visibility badge, claimed/cleared indicators from native state) with row actions: **Edit**, **Delete**, **Reveal/Hide** (toggles visibility → triggers `HexContentSync` + the player-facing text). An "Add content for hex…" control (hex key picker seeded from `kingmaker.region.hexes`, or "use currently selected hex").
- **Add/Edit forms** (`hex-content-add.hbs` / `hex-content-edit.hbs`): type dropdown (`HexContentType`), name, GM notes, player text, visibility, optional encounter-suppression override, optional travel modifier, optional linked quest/uuid.
- **Entry point:** a GM-gated "Hex Content Manager" button on the kingdom sheet's turn/map section (`data-action="open-hex-content-manager"` handled in `KingdomSheet.kt`).
- **Scene visualization:** `HexContentSync` draws one composite content **marker** per hex (icon/label by `contentMarkerFor`), tagged with the `pf2e-kingmaker-tools` app flag (`type="hexContent"`, `hexKey`, kingdomActor uuid) so it is cleanable/idempotent exactly like the claimed/explored/cleared overlays. Hidden content draws a GM-only marker (or none for players); discovered/cleared draws player-visible markers.
- **Player-facing discovery text:** on reveal, post a chat message and/or write/update a linked journal entry with `playerText` (reuse the chat-message + `TextEditor.enrichHTML` pattern already used in `ArmyCompendiumEntries.kt` / recruit flow).
- **⚠️ ApplicationV2 gotcha (do not regress):** every Handlebars template *part* must render exactly one root element, or ApplicationV2 throws *"Template part 'div' must render a single HTML element"* and the dialog silently never opens. Wrap each new template in a single root `<div class="km-browser">` (this exact bug was fixed in `army-browser.hbs`, commit `0b87492e`; `structure-browser.hbs` is the canonical example). Add this to the implementation checklist for every new `.hbs`.
- **i18n:** all strings via `localizeKM` (templates) / `t()` (Kotlin) under `kingdom.hexContent.*`, `hexContentType.*`, `hexContentVisibility.*` in `lang/en.json`. Verify keys resolve (a missing key renders as the raw key string in the UI).

---

## Tests

### Unit — `commonTest` (pure, no Foundry; highest value)

- `HexDiscoveryTest`:
  - `nextVisibility` transitions: hidden→discovered→cleared; idempotent re-apply; never regresses.
  - `suppressesRandomEncounter`: claimed=true, cleared=true, neither + content override true/false, no-content default.
  - `aggregateTravelModifiers`: roads/bridges/settlements + content modifiers; empty → 0; multiple features.
  - `contentMarkerFor`: priority selection when a hex has multiple contents; null when none/hidden-for-players.
- `HexContentEnumsTest`: `value`, `fromString` round-trip, `i18nKey` for every `HexContentType` and `HexContentVisibility`.

### Unit — `jsTest`

- `HexContentManagerTest`: context building from `kingdom.hexContents`; add/edit/delete produce correct new `KingdomData` (immutably); "Reveal" flips visibility and is reflected in context; GM-only gating.
- `Migration27Test`: null → `emptyArray()`; already-populated array unchanged (idempotent); does not touch other fields.

### Integration / acceptance scope

- `HexContentSync` create/update/delete of marker drawings against a faked scene (follow the existing `HexStateTest` / `HexGridSync` test approach for drawing diffing); verify actor-wins semantics (a stale/extra marker with our app flag is removed).
- Encounter-filtering guard: a claimed or cleared hex suppresses the random-encounter roll; an un-claimed hex does not.

### Coverage / run

- Target the 80% bar on the new pure modules. Run JS suite via the Chrome-headless workaround (`useChromeHeadless()` + `CHROME_BIN` + `-x kotlinStoreYarnLock`); `commonTest` runs in the JS suite. Keep `JAVA_HOME` = local JDK 25.

---

## Manual Foundry Verification Checklist (for Gregory)

Run against a **saved** world (not fresh) so Migration 27 exercises real data.

- [ ] **Migration 27** — load a pre-existing world; no errors; `kingdom.hexContents` is present (empty array if none); reload is idempotent.
- [ ] **Open manager** — Kingdom sheet → "Hex Content Manager" opens (no ApplicationV2 single-root error in console).
- [ ] **Add content** — add a Landmark to a hex (by key or selected hex); it appears in the manager list with visibility = **Hidden** by default.
- [ ] **GM-only while hidden** — as a player (or with a player test user), the hidden content's player text is **not** visible and no player-facing scene marker shows.
- [ ] **Reveal** — click Reveal; visibility → Discovered; player text becomes visible (chat/journal) and a player-facing scene marker appears.
- [ ] **Discovery transition** — exploring/clearing the hex updates visibility per the rules; cleared content shows the cleared marker (composite, single marker — not stacked layers).
- [ ] **Encounter suppression** — on a claimed or cleared hex, a random-encounter roll is suppressed; on an un-claimed/un-cleared hex it still rolls. (Verify with the encounter action used in your table.)
- [ ] **Travel modifier input** — a hex with a road/bridge/content modifier reports the expected aggregate (visible wherever travel cost is surfaced; full ETA is the Travel planner feature).
- [ ] **Quest link seam** — "Create quest from discovery" opens AddQuest pre-filled with the hex name/key and stores `linkedQuestId` (generation itself is the quest feature).
- [ ] **Scene sync robustness** — manually delete a content marker drawing on the scene, then trigger a re-sync (reload / canvas ready); the actor re-creates it (actor wins, Decision 4).
- [ ] **Edit / delete** — editing updates the marker; deleting removes both the record and its scene marker.
- [ ] **i18n** — no raw localization keys (e.g. `hexContentType.landmark`) appear in the UI.

---

## Implementation order (suggested)

1. `commonMain` enums + `HexDiscovery.kt` pure helpers + `commonTest` (RED→GREEN first; no Foundry needed).
2. `RawHexContent` + `KingdomData.hexContents` + `Defaults.kt` + `Migration27` + `Migration27Test`.
3. `HexContentManager` dialog + templates + contexts + `KingdomSheet.kt` wiring + i18n (mind the single-root rule).
4. `HexContentSync` + `HexGridSync.kt`/`Main.kt` wiring (visualization layer).
5. Encounter-filtering guard + AddQuest pre-fill seam.
6. `jsTest` for manager/migration; full Chrome-headless run; manual Foundry checklist.

Each step compiles green (`JAVA_HOME=<jdk25> ./gradlew compileKotlinJs`) before the next.
