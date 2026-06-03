# Companion Relationship & Personal Quest Manager Implementation Plan

> **For Hermes:** Use subagent-driven-development skill to implement this plan task-by-task.

## Overview

Implement a companion relationship and personal quest manager (roadmap item #7) that tracks companion influence, camp availability, learning activities, and personal quest hooks. Builds on the existing `RawCharacter` model, `CampingActivityData.requiredCompanion` gating (already implemented + 10 passing tests), roster panel UI, and quests page. Adds new companion profile fields, a personal quest data model with GM-only visibility default (Decision 2), and a companion profile dialog on the kingdom sheet. This feature is a prerequisite for the session prep dashboard's companion moments aggregation (Decision 5).

**Architecture:** New `CompanionProfile` dialog (Foundry `FormApp` subclass) provides a per-companion detail view with influence/discovery tracking and personal quest hooks. New `AddPersonalQuest` dialog creates `CompanionPersonalQuest` records linked to a companion. New `CompanionProfileContext` and `PersonalQuestContext` JS-plain objects feed the Handlebars templates. Camping activity gating already uses `isRequiredCompanionPresent()` — the new companion `campAvailable` flag adds a second discrimination layer for companions who are in camp but busy. All personal quests default to GM-only with one-click reveal (Decision 2). The `setKingdom()` companion sync removes dead `setAppFlag("companion-data")` writes per Decision 8.

**Tech Stack:** Kotlin Multiplatform jsMain/jsTest/commonMain, Foundry `FormApp` / `CrudApplication` dialogs, Handlebars templates, `kotlin.test`, existing `RosterAddDialog` / `RosterEditDialog` patterns.

## Goals & Non-Goals

### Goals

- Add companion relationship tracking (influence 0-100, discovery status 5-stage progression) to `RawCharacter`.
- Add `campAvailable` flag to `RawCharacter` for fine-grained camp activity gating.
- Create `CompanionPersonalQuest` data model for companion-specific quests stored in `KingdomData`.
- Provide a per-companion profile dialog with influence bar, discovery status, camp availability, quest hooks, and personal quest list.
- Extend the roster tab UI with influence bars, camp badges, discovery icons, and personal quest count badges.
- Add a "Personal Quests" subsection to the existing quests page.
- Default all personal quests to GM-only with one-click player reveal (Decision 2).
- Remove dead `setAppFlag("companion-data")` writes from `setKingdom()` (Decision 8).

### Non-Goals

- Do NOT add bidirectional companion actor sync (companion sync policy doc recommends write-only; Decision 8 confirms remove dead setAppFlag writes).
- Do NOT implement campaign clock integration (roadmap item #1, separate feature). Personal quest deadlines are simple turn counts.
- Do NOT implement full prose-based session prep generation (roadmap item #10). The companion moments aggregation in the roster tab is structured data only.
- Do NOT add homebrew profile import/export (roadmap item #9, separate feature).
- Do NOT modify the camping activity resolution system — gating via `isRequiredCompanionPresent()` is complete. Only the `campAvailable` flag on `RawCharacter` is added.
- Do NOT modify `TurnTickingEngine.tick()`. Companion relationship ticks are computed on read, not in the turn engine.
- Do NOT implement a separate companion actor sheet tab. All companion management stays on the kingdom sheet roster tab.
- Do NOT implement automatic discovery progression — GM manually advances discovery status.

## Source of truth

- Roadmap item #7: `docs/feature-roadmap.md` lines 169-189
- Design decisions (card f0): `docs/plans/2026-06-01-roadmap-design-decisions.md` — Decision 2 (GM-only default), Decision 5 (structured aggregation first), Decision 8 (remove dead setAppFlag writes)
- Companion sync policy: `docs/companion-sync-policy.md`
- Existing companion model: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/data/RawCharacter.kt`
- Existing quest model: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/data/RawQuest.kt`
- Existing camping gating: `src/jsMain/kotlin/at/posselt/pfrpg2e/camping/CampingActivityData.kt`
- Existing camping data: `src/jsMain/kotlin/at/posselt/pfrpg2e/camping/CampingData.kt`
- Existing kingdom data: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/KingdomData.kt`
- Existing kingdom sync: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/Kingdom.kt`
- Existing roster dialogs: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/dialogs/RosterPanel.kt`
- Existing roster context: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/contexts/RosterContext.kt`
- Existing roster template: `src/jsMain/resources/applications/kingdom/sections/roster/page.hbs`
- Existing roster add template: `src/jsMain/resources/applications/kingdom/roster-add.hbs`
- Existing roster edit template: `src/jsMain/resources/applications/kingdom/roster-edit.hbs`
- Existing quests template: `src/jsMain/resources/applications/kingdom/sections/quests/page.hbs`
- Existing kingdom sheet: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/KingdomSheet.kt`
- Existing migration pattern: `src/jsMain/kotlin/at/posselt/pfrpg2e/migrations/migrations/Migration26.kt`
- Existing migration registry: `src/jsMain/kotlin/at/posselt/pfrpg2e/migrations/Migrations.kt`
- Existing companion gating tests: `src/jsTest/kotlin/at/posselt/pfrpg2e/camping/CompanionGatingTest.kt`
- Existing companion model tests: `src/commonTest/kotlin/at/posselt/pfrpg2e/kingdom/data/RawCharacterTest.kt`
- Existing test patterns: `src/jsTest/kotlin/at/posselt/pfrpg2e/kingdom/`

## Affected Files

### New files

| File | Purpose |
|------|---------|
| `src/commonMain/kotlin/at/posselt/pfrpg2e/companion/CompanionQuest.kt` | Campaign-scoped personal quest data model (id, companionId, title, description, status, questHook, rewards, visibleToPlayers, turnsRemaining) |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/companion/AddPersonalQuestDialog.kt` | `FormApp` dialog for creating/editing a companion personal quest |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/companion/CompanionProfileDialog.kt` | `FormApp` dialog: per-companion detail view with influence/discovery/quest hooks |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/contexts/CompanionProfileContext.kt` | JS context interface for the companion profile dialog template |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/contexts/PersonalQuestContext.kt` | JS context interface for the personal quest template partials |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/contexts/CompanionQuestContext.kt` | JS context for companion quest summary on roster cards |
| `src/jsMain/resources/applications/kingdom/companion-profile.hbs` | Handlebars template: companion profile dialog with stats, influence, quest hooks, personal quests |
| `src/jsMain/resources/applications/kingdom/add-personal-quest.hbs` | Handlebars template: add/edit personal quest dialog |
| `src/jsMain/resources/applications/kingdom/companion-quest-card.hbs` | Partial template: quest card shown on companion profile |
| `src/jsMain/resources/applications/kingdom/companion-quest-row.hbs` | Partial template: quest row shown on quests page for personal quests |
| `src/jsMain/resources/applications/kingdom/companion-relationship.css` | New CSS for companion profile dialog, relationship bars, quest badges |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/migrations/migrations/Migration27.kt` | Migration27 — adds `influence`, `campAvailable`, `discoveryStatus`, `personalQuestIds` to `RawCharacter`; adds `companionPersonalQuests` array to `KingdomData` |
| `src/jsTest/kotlin/at/posselt/pfrpg2e/companion/CompanionQuestTest.kt` | Unit tests for CompanionQuest data model (CQ1) |
| `src/jsTest/kotlin/at/posselt/pfrpg2e/companion/CompanionProfileDialogTest.kt` | Unit tests for profile dialog context computation (CQ2) |
| `src/jsTest/kotlin/at/posselt/pfrpg2e/companion/AddPersonalQuestDialogTest.kt` | Unit tests for personal quest dialog defaults and validation (CQ3) |
| `src/jsTest/kotlin/at/posselt/pfrpg2e/companion/CampAvailabilityGatingTest.kt` | Unit tests for camp availability + existing companion gating combined (CQ4) |
| `src/jsTest/kotlin/at/posselt/pfrpg2e/companion/PersonalQuestIntegrationTest.kt` | Integration test: create personal quest → verify GM-only default → reveal to players (CQ5) |
| `src/commonTest/kotlin/at/posselt/pfrpg2e/kingdom/data/RawCharacterCompanionFieldsTest.kt` | Unit tests for new RawCharacter fields (influence, campAvailable, discoveryStatus) (CQ6) |

### Modified files

| File | Change |
|------|--------|
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/data/RawCharacter.kt` | Add `influence: Int`, `campAvailable: Boolean`, `discoveryStatus: String`, `personalQuestIds: Array<String>` fields |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/KingdomData.kt` | Add `companionPersonalQuests: Array<CompanionPersonalQuest>?` field |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/Kingdom.kt` | Remove dead `setAppFlag("companion-data", companion)` loop in `setKingdom()` (Decision 8). Remove the corresponding `companionActor` lookup block (lines 20-28). |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/dialogs/RosterPanel.kt` (`RosterAddDialog`) | Add `influence` number input (default 0, min 0, max 100) and `discoveryStatus` dropdown (default "unknown", options: unknown/introduced/established/trusted/bonded) to the companion add form. Map form field names: `companionInfluence` → `influence`, `companionDiscovery` → `discoveryStatus`. |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/dialogs/RosterPanel.kt` (`RosterEditDialog`) | Add `influence` display/read-only, `campAvailable` toggle, `discoveryStatus` dropdown, "Open Profile" button, personal quest count badge |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/contexts/RosterContext.kt` | Add `influence`, `campAvailable`, `discoveryStatus`, `personalQuestCount`, `hasPersonalQuests` fields to `RosterActorContext` |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/KingdomSheet.kt` | Add `data-action="open-companion-profile"` handler (opens `CompanionProfileDialog`). Add `data-action="add-personal-quest"` handler (opens `AddPersonalQuestDialog`). Add companion quest aggregates to roster section context (`activePersonalQuestCount`, `totalInfluence`). Extend `checkUpdateActorReRenders()` is NOT needed for companions (no reactive sync per Decision 8). |
| `src/jsMain/resources/applications/kingdom/sections/roster/page.hbs` | Add influence bar, camp availability badge, discovery status icon, personal quest count badge to roster cards. Add "Open Profile" button. Show greyed-out state for companions with `campAvailable == false`. |
| `src/jsMain/resources/applications/kingdom/roster-add.hbs` | Add `influence` input field and `discoveryStatus` dropdown |
| `src/jsMain/resources/applications/kingdom/roster-edit.hbs` | Add `campAvailable` toggle, `discoveryStatus` display, "Open Profile" button |
| `src/jsMain/resources/applications/kingdom/sections/quests/page.hbs` | Add "Personal Quests" subsection below active quests. Use `companion-quest-row.hbs` partial. Add filter toggle for companion vs. kingdom quests. |
| `src/jsMain/resources/applications/kingdom/kingdom-sheet.css` | Add companion relationship styles (influence bar, camp badge, quest count badge) — or import from companion-relationship.css |
| `src/jsMain/resources/lang/en.json` | Add ~20 localization keys (see section below) |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/migrations/Migrations.kt` | Register `Migration27` in the migration list |

### Localization keys to add

All in `src/jsMain/resources/lang/en.json` under a `kingdom.companion` namespace:

```json
"kingdom.companion.profile": "Companion Profile",
"kingdom.companion.influence": "Influence",
"kingdom.companion.campAvailable": "Available at Camp",
"kingdom.companion.discoveryStatus": "Discovery Status",
"kingdom.companion.discovery.unknown": "Unknown",
"kingdom.companion.discovery.introduced": "Introduced",
"kingdom.companion.discovery.established": "Established",
"kingdom.companion.discovery.trusted": "Trusted",
"kingdom.companion.discovery.bonded": "Bonded",
"kingdom.companion.personalQuests": "Personal Quests",
"kingdom.companion.addPersonalQuest": "Add Personal Quest",
"kingdom.companion.noPersonalQuests": "No personal quests yet.",
"kingdom.companion.openProfile": "Profile",
"kingdom.companion.inCamp": "In Camp",
"kingdom.companion.notInCamp": "Away",
"kingdom.companion.activeQuests": "{count} Active",
"kingdom.companion.questHook": "Quest Hook",
"kingdom.companion.quest.reveal": "Reveal to Players",
"kingdom.companion.quest.hide": "Hide from Players",
"kingdom.companion.quest.turnsRemaining": "Turns Remaining: {count}",
"kingdom.companion.personalQuest": "Personal Quest"
```

---

## Data Models

### RawCharacter (modified)

```kotlin
// src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/data/RawCharacter.kt

@JsPlainObject
external interface RawCharacter {
    var name: String
    var actorUuid: String?
    var destinationX: Int?
    var destinationY: Int?
    var speed: Int
    var eta: Int?
    var plotHook: String?
    var traveling: Boolean
    var active: Boolean
    var role: String
    var img: String?

    // NEW FIELDS
    /** Companion relationship influence (0-100). Higher = stronger bond. Default 0. */
    var influence: Int
    /** Whether this companion is available for camp activities (not traveling, not busy). Default true. */
    var campAvailable: Boolean
    /** Discovery/relationship stage. One of: "unknown", "introduced", "established", "trusted", "bonded". Default "unknown". */
    var discoveryStatus: String
    /** IDs of personal quests linked to this companion. */
    var personalQuestIds: Array<String>
}
```

**Changes to default factory:**
Add `influence=0`, `campAvailable=true`, `discoveryStatus="unknown"`, `personalQuestIds=[]` to the default factory.

### Camp Availability & Discovery Status

**Camp availability semantics:**
- `campAvailable = false` → companion is in camp but currently occupied (on a mission, injured, etc.)
- `traveling = true` → implies `campAvailable = false` (enforced in `KingdomSheet` toggle handlers, not in the data model)
- Existing `isRequiredCompanionPresent()` uses `CampingData.actorUuids` (who is physically in camp). The new `campAvailable` flag adds a second layer: a companion can be in camp AND unavailable for activities. Camping activity rendering should check BOTH:
  1. Existing: is companion in `actorUuids`? (`isRequiredCompanionPresent`)
  2. New: is `campAvailable == true`?

**Discovery status progression:**
```
unknown → introduced → established → trusted → bonded
```
- One influence/discovery attempt per camp session (enforced in the companion profile dialog, not in the data model)
- GM manually advances the status; no automatic progression

### CompanionPersonalQuest (commonMain)

```kotlin
// src/commonMain/kotlin/at/posselt/pfrpg2e/companion/CompanionQuest.kt

package at.posselt.pfrpg2e.companion

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface CompanionPersonalQuest {
    var id: String
    var companionId: String          // RawCharacter.actorUuid or companion index reference
    var title: String
    var description: String
    var questHook: String?           // GM-facing narrative hook / trigger condition
    var status: String               // "active" | "completed" | "failed" | "abandoned"
    var type: String                 // "personal" (distinguishes from kingdom quests)
    var rewards: CompanionQuestRewards
    var visibleToPlayers: Boolean    // Decision 2: default false (GM-only)
    var turnsRemaining: Int?         // null = no deadline
    var sourceEvent: String?         // optional: kingdom event that triggered this quest
}

@JsPlainObject
external interface CompanionQuestRewards {
    var influence: Int?              // influence gain on completion
    var xp: Int?
    var rp: Int?
    var customReward: String?
}
```

**State machine:**
```
active → completed (GM resolves, rewards applied)
active → failed (turns expired, objective failed)
active → abandoned (GM dismisses)
```

**Relationship to existing `RawQuest`:**
- `RawQuest` = kingdom-level quests (hex claims, structures, etc.) — displayed on the quests page
- `CompanionPersonalQuest` = companion-specific quests — displayed on the companion profile AND in a new "Personal Quests" subsection on the quests page
- Both share the same GM-only default visibility pattern (Decision 2)
- `CompanionPersonalQuest` is stored in `KingdomData.companionPersonalQuests`, separate from `KingdomData.quests`

### KingdomData (modified)

```kotlin
// src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/KingdomData.kt

@JsPlainObject
external interface KingdomData {
    // ... existing fields ...

    var companions: Array<RawCharacter>?

    // NEW FIELD
    /** Personal quests tied to individual companions. */
    var companionPersonalQuests: Array<CompanionPersonalQuest>?
}
```

### CompanionProfileContext (jsMain)

```kotlin
// src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/contexts/CompanionProfileContext.kt

package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.forms.FormElementContext
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface CompanionProfileContext {
    val companionName: String
    val companionUuid: String?
    val influence: Int
    val influencePercent: Int          // influence / 100 for progress bar
    val campAvailable: Boolean
    val discoveryStatus: String
    val discoveryStatusLabel: String   // localized
    val plotHook: String?
    val personalQuests: Array<PersonalQuestSummaryContext>
    val activeQuestCount: Int
    val isGM: Boolean
    val canAttemptDiscovery: Boolean   // true if not already attempted this session
}

@JsPlainObject
external interface PersonalQuestSummaryContext {
    val id: String
    val title: String
    val status: String
    val statusLabel: String            // localized
    val turnsRemaining: Int?
    val visibleToPlayers: Boolean
    val hasDeadline: Boolean
}
```

### RosterActorContext (modified)

```kotlin
// src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/contexts/RosterContext.kt

@JsPlainObject
external interface RosterActorContext {
    // ... existing fields ...
    val name: String
    val role: String
    val roleLabel: String
    val speed: Int
    val destinationX: Int?
    val destinationY: Int?
    val destinationLabel: String
    val eta: Int?
    val traveling: Boolean
    val active: Boolean
    val plotHook: String?
    val actorUuid: String?
    val img: String?

    // NEW FIELDS
    val influence: Int
    val campAvailable: Boolean
    val discoveryStatus: String
    val discoveryStatusLabel: String   // localized
    val personalQuestCount: Int
    val hasPersonalQuests: Boolean
}
```

---

## Migrations

### Migration27

**File:** `src/jsMain/kotlin/at/posselt/pfrpg2e/migrations/migrations/Migration27.kt`

**Purpose:** Adds companion relationship fields to `RawCharacter` and `companionPersonalQuests` array to `KingdomData`.

**Schema changes (kingdom actor):**

| Field | Type | Default | Notes |
|-------|------|---------|-------|
| `RawCharacter.influence` | `Int` | `0` | Added to each companion in `kingdom.companions` |
| `RawCharacter.campAvailable` | `Boolean` | `true` | Added to each companion |
| `RawCharacter.discoveryStatus` | `String` | `"unknown"` | Added to each companion |
| `RawCharacter.personalQuestIds` | `Array<String>` | `[]` | Added to each companion |
| `KingdomData.companionPersonalQuests` | `Array<CompanionPersonalQuest>` | `[]` | New top-level field |

**Migration logic:**

```
For each companion in kingdom.companions:
  - Set influence=0, campAvailable=true, discoveryStatus="unknown", personalQuestIds=[] if null
Set kingdom.companionPersonalQuests=[] if null
```

**Rollback considerations:**
- This migration only adds new fields with safe defaults. No existing data is removed or transformed.
- Rollback would require removing the new fields, but since they have no downstream dependencies in the same migration, a simple version downgrade is safe.
- The companion `personalQuestIds` array is additive — existing companions simply have an empty array.

**Registration:** Add `Migration27()` to the `migrations` list in `Migrations.kt`.

---

## UI / Template Changes

### Roster page (`sections/roster/page.hbs`)

**Changes to existing roster cards:**

1. **Influence bar** — below the role badge, show a thin progress bar (0-100%) representing influence. Color: grey (0-25), blue (26-50), green (51-75), gold (76-100).
2. **Camp availability badge** — small icon next to the name: green tent icon if `campAvailable && !traveling`, grey/away icon if `!campAvailable || traveling`.
3. **Discovery status icon** — small icon (question mark / handshake / heart) reflecting `discoveryStatus`. Tooltip shows localized label.
4. **Personal quest count badge** — small pill showing "N Active" if `hasPersonalQuests`.
5. **"Profile" button** — next to the edit button, opens `CompanionProfileDialog`.
6. **Unavailable styling** — if `campAvailable == false`, the card gets a slightly dimmed overlay (existing `km-roster-card-inactive` class extended).

**New Handlebars additions (within the existing `km-roster-card`):**
```handlebars
{{#if this.influence}}
<div class="km-roster-influence-bar">
  <div class="km-roster-influence-fill" style="width: {{this.influence}}%"></div>
</div>
{{/if}}
<div class="km-roster-badges">
  <span class="km-roster-camp-badge {{#if this.campAvailable}}km-camp-available{{else}}km-camp-unavailable{{/if}}">
    <i class="fa-solid {{#if this.campAvailable}}fa-campground{{else}}fa-person-walking-arrow-right{{/if}}"></i>
  </span>
  <span class="km-roster-discovery-icon" title="{{this.discoveryStatusLabel}}">
    {{#if (eq this.discoveryStatus "unknown")}}<i class="fa-solid fa-question"></i>{{/if}}
    {{#if (eq this.discoveryStatus "introduced")}}<i class="fa-solid fa-handshake-simple"></i>{{/if}}
    {{#if (eq this.discoveryStatus "established")}}<i class="fa-solid fa-handshake"></i>{{/if}}
    {{#if (eq this.discoveryStatus "trusted")}}<i class="fa-solid fa-heart"></i>{{/if}}
    {{#if (eq this.discoveryStatus "bonded")}}<i class="fa-solid fa-heart-pulse"></i>{{/if}}
  </span>
  {{#if this.hasPersonalQuests}}
  <span class="km-roster-quest-badge">
    <i class="fa-solid fa-scroll"></i> {{this.personalQuestCount}} {{localizeKM "kingdom.companion.personalQuests"}}
  </span>
  {{/if}}
</div>
```

### Roster add dialog (`roster-add.hbs`)

Add after the plot hook textarea:
```handlebars
<div class="km-form-group">
    <label for="companionInfluence">{{localizeKM "kingdom.companion.influence"}}</label>
    <input type="number" name="companionInfluence" value="0" min="0" max="100" />
</div>
<div class="km-form-group">
    <label for="companionDiscovery">{{localizeKM "kingdom.companion.discoveryStatus"}}</label>
    <select name="companionDiscovery">
        <option value="unknown">{{localizeKM "kingdom.companion.discovery.unknown"}}</option>
        <option value="introduced">{{localizeKM "kingdom.companion.discovery.introduced"}}</option>
        <option value="established">{{localizeKM "kingdom.companion.discovery.established"}}</option>
        <option value="trusted">{{localizeKM "kingdom.companion.discovery.trusted"}}</option>
        <option value="bonded">{{localizeKM "kingdom.companion.discovery.bonded"}}</option>
    </select>
</div>
```

### Roster edit dialog (`roster-edit.hbs`)

Add after the status section:
```handlebars
<div class="km-form-group">
    <label>{{localizeKM "kingdom.companion.campAvailable"}}</label>
    <span class="km-roster-status {{#if campAvailable}}km-status-on{{else}}km-status-off{{/if}}">
        {{#if campAvailable}}{{localizeKM "kingdom.roster.yes"}}{{else}}{{localizeKM "kingdom.roster.no"}}{{/if}}
    </span>
</div>
<div class="km-form-group">
    <label for="companionDiscovery">{{localizeKM "kingdom.companion.discoveryStatus"}}</label>
    <select name="companionDiscovery">
        <option value="unknown" {{#if (eq discoveryStatus "unknown")}}selected{{/if}}>{{localizeKM "kingdom.companion.discovery.unknown"}}</option>
        <option value="introduced" {{#if (eq discoveryStatus "introduced")}}selected{{/if}}>{{localizeKM "kingdom.companion.discovery.introduced"}}</option>
        <option value="established" {{#if (eq discoveryStatus "established")}}selected{{/if}}>{{localizeKM "kingdom.companion.discovery.established"}}</option>
        <option value="trusted" {{#if (eq discoveryStatus "trusted")}}selected{{/if}}>{{localizeKM "kingdom.companion.discovery.trusted"}}</option>
        <option value="bonded" {{#if (eq discoveryStatus "bonded")}}selected{{/if}}>{{localizeKM "kingdom.companion.discovery.bonded"}}</option>
    </select>
</div>
<hr>
<div class="km-roster-dialog-actions">
    <button type="button" data-action="open-profile" class="km-primary-btn">
        <i class="fa-solid fa-user"></i> {{localizeKM "kingdom.companion.openProfile"}}
    </button>
</div>
```

### Companion profile dialog (`companion-profile.hbs`) — NEW

Full dialog template with:
- Header: companion name, portrait, role badge, discovery status
- Influence section: progress bar (0-100), current value, "Attempt Discovery" button (GM only, once per session)
- Camp availability toggle (GM only)
- Quest hooks section: free-text area for GM notes on narrative triggers
- Personal quests section: list of `companion-quest-card.hbs` partials, "Add Personal Quest" button
- Action buttons: Save, Close

### Personal quest card partial (`companion-quest-card.hbs`) — NEW

Small card showing quest title, status badge, turns remaining, visibility toggle (eye icon), and a "View" button.

### Personal quest row partial (`companion-quest-row.hbs`) — NEW

Compact row for the quests page "Personal Quests" subsection. Shows quest title, companion name, status, turns remaining, visibility toggle.

### Quests page (`sections/quests/page.hbs`)

Add a new subsection after the active quests grid and before the completed quests section:

```handlebars
{{#if isGM}}
<section class="km-quests-section km-personal-quests-section">
    <h2 class="km-quests-header">
        <span><i class="fa-solid fa-heart"></i> {{localizeKM "kingdom.companion.personalQuests"}}</span>
        <span class="km-header-right-align">
            <button type="button" class="km-add-personal-quest-btn" data-action="add-personal-quest">
                <i class="fa-solid fa-plus"></i> {{localizeKM "kingdom.companion.addPersonalQuest"}}
            </button>
        </span>
    </h2>
    {{#if personalQuests.length}}
    <div class="km-quests-list">
        {{#each personalQuests}}
            {{> "applications/kingdom/companion-quest-row.hbs"}}
        {{/each}}
    </div>
    {{else}}
    <div class="km-empty-quests">
        <p>{{localizeKM "kingdom.companion.noPersonalQuests"}}</p>
    </div>
    {{/if}}
</section>
{{/if}}
```

### Camping activity gating integration

The existing camping activity list rendering (in the camping sheet) already greys out activities where `isRequiredCompanionPresent()` returns false. The new `campAvailable` flag adds a second check:

**In the camping sheet context builder**, for each activity, add a `companionAvailable` boolean that is true only when:
1. The required companion is in `CampingData.actorUuids` (existing check), AND
2. The companion's `campAvailable` flag is `true` (new check)

This means a companion can be physically in camp but marked as unavailable (e.g., injured, on a separate mission), and their gated activities will be greyed out.

**Implementation:** In the camping sheet context builder, for each `CampingActivityData` with a non-null `requiredCompanion`, add a `companionAvailable` check: find the companion by name in `KingdomData.companions`, and verify `campAvailable != false` (null defaults to true for backward compat). An activity is gated if the required companion is either not in `actorUuids` OR has `campAvailable == false`.

---

## Tests

### CQ1: CompanionQuestTest (`src/jsTest/kotlin/at/posselt/pfrpg2e/companion/CompanionQuestTest.kt`)

| Test | Description |
|------|-------------|
| `CompanionPersonalQuest default values` | Verify default status is "active", visibleToPlayers is false, turnsRemaining is null |
| `CompanionPersonalQuest state transitions` | active→completed, active→failed, active→abandoned |
| `CompanionQuestRewards default values` | All rewards default to 0/null |
| `CompanionPersonalQuest with all fields` | Full field assignment and read-back |

### CQ2: CompanionProfileDialogTest (`src/jsTest/kotlin/at/posselt/pfrpg2e/companion/CompanionProfileDialogTest.kt`)

| Test | Description |
|------|-------------|
| `profile context with no quests` | Context has empty personalQuests array, activeQuestCount = 0 |
| `profile context with active quests` | Context correctly counts active vs. completed quests |
| `influence percent calculation` | influence=75 → influencePercent=75 |
| `discovery status labels` | Each discovery status maps to correct localized key |
| `canAttemptDiscovery defaults to true` | Fresh session allows discovery attempt |
| `GM-only fields present` | isGM=true shows all fields; isGM=false hides GM fields |

### CQ3: AddPersonalQuestDialogTest (`src/jsTest/kotlin/at/posselt/pfrpg2e/companion/AddPersonalQuestDialogTest.kt`)

| Test | Description |
|------|-------------|
| `dialog defaults to GM-only` | visibleToPlayers defaults to false (Decision 2) |
| `dialog pre-populates companionId` | When opened from a companion profile, companionId is pre-filled |
| `dialog validates required fields` | Title is required; description defaults to empty string |
| `dialog creates valid quest object` | Output has valid id, status="active", type="personal" |

### CQ4: CampAvailabilityGatingTest (`src/jsTest/kotlin/at/posselt/pfrpg2e/companion/CampAvailabilityGatingTest.kt`)

| Test | Description |
|------|-------------|
| `companion in camp and available` | actorUuids contains companion, campAvailable=true → activity enabled |
| `companion in camp but unavailable` | actorUuids contains companion, campAvailable=false → activity gated |
| `companion not in camp` | actorUuids does not contain companion → activity gated (regardless of campAvailable) |
| `companion traveling implies unavailable` | traveling=true → campAvailable should be set to false by toggle handler |
| `old data without campAvailable field` | campAvailable=null → treated as true (backward compat) |
| `campAvailable does not affect non-gated activities` | Activities without requiredCompanion are unaffected |

### CQ5: PersonalQuestIntegrationTest (`src/jsTest/kotlin/at/posselt/pfrpg2e/companion/PersonalQuestIntegrationTest.kt`)

| Test | Description |
|------|-------------|
| `create personal quest → appears in companion profile` | Quest created for companion shows in profile's quest list |
| `create personal quest → GM-only by default` | visibleToPlayers is false after creation |
| `reveal personal quest to players` | GM clicks reveal → visibleToPlayers becomes true |
| `complete personal quest → influence reward applied` | Completing a quest with influence reward increases companion's influence |
| `personal quest appears in quests page subsection` | Created quest shows in the "Personal Quests" section |
| `personal quest filtered by companion` | Filtering by companion UUID shows only that companion's quests |

### CQ6: RawCharacterCompanionFieldsTest (`src/commonTest/kotlin/at/posselt/pfrpg2e/kingdom/data/RawCharacterCompanionFieldsTest.kt`)

| Test | Description |
|------|-------------|
| `RawCharacter factory includes new defaults` | New RawCharacter has influence=0, campAvailable=true, discoveryStatus="unknown", personalQuestIds=[] |
| `RawCharacter with new fields set` | All new fields can be set and read back |
| `RawCharacter mutation of new fields` | Fields can be mutated after creation |
| `RawCharacter backward compat — old fields unchanged` | Existing fields (name, speed, traveling, etc.) retain their defaults |

---

## Manual Foundry Verification Checklist

Use this checklist after implementation to verify the feature works in Foundry VTT.

### Setup
- [ ] **1.** Open a kingdom actor sheet that has at least one companion in the roster.
- [ ] **2.** Verify the roster tab shows the new influence bar, camp availability badge, and discovery status icon on each companion card.
- [ ] **3.** Verify companions with no personal quests show no quest badge.

### Companion Profile Dialog
- [ ] **4.** Click "Profile" on a companion card → Companion Profile dialog opens.
- [ ] **5.** Verify the profile shows: name, portrait, role, influence bar, discovery status, camp availability toggle.
- [ ] **6.** Verify the "Quest Hooks" text area is editable (GM only).
- [ ] **7.** Verify the Personal Quests section shows "No personal quests yet" when empty.
- [ ] **8.** Close the dialog. Re-open → state is preserved.

### Personal Quest Creation
- [ ] **9.** In the companion profile, click "Add Personal Quest" → Add Personal Quest dialog opens.
- [ ] **10.** Fill in title, description, quest hook. Set turns remaining to 3. Click Save.
- [ ] **11.** Verify the quest appears in the companion profile's Personal Quests list.
- [ ] **12.** Verify the quest status is "Active" and the turns remaining shows "3".
- [ ] **13.** Verify the quest is NOT visible to players (GM-only by default, Decision 2).
- [ ] **14.** On the kingdom sheet quests tab, verify a new "Personal Quests" subsection appears with the created quest.

### Quest Visibility Toggle
- [ ] **15.** In the companion profile, click the eye icon on a personal quest → it becomes visible to players.
- [ ] **16.** Verify the eye icon changes to an open eye.
- [ ] **17.** Click again → quest becomes hidden. Eye icon changes to closed eye.

### Camp Availability
- [ ] **18.** In the roster, toggle a companion's camp availability to "unavailable".
- [ ] **19.** Open the camping sheet. Verify activities gated to that companion are greyed out even if the companion is in `actorUuids`.
- [ ] **20.** Toggle camp availability back to "available". Verify activities are no longer greyed out.

### Discovery Status Progression
- [ ] **21.** In the companion profile, change discovery status from "Unknown" to "Introduced".
- [ ] **22.** Verify the discovery status icon updates on the roster card.
- [ ] **23.** Advance through all stages: Introduced → Established → Trusted → Bonded. Verify icon changes at each stage.

### Influence Tracking
- [ ] **24.** Create a personal quest with an influence reward of +10.
- [ ] **25.** Complete the quest. Verify the companion's influence increases by 10.
- [ ] **26.** Verify the influence bar on the roster card updates to reflect the new value.

### Migration
- [ ] **27.** Load a world with existing kingdom data (pre-migration). Verify Migration27 runs without errors.
- [ ] **28.** Verify existing companions have default values: influence=0, campAvailable=true, discoveryStatus="unknown", personalQuestIds=[].
- [ ] **29.** Verify the `companionPersonalQuests` array exists and is empty.
- [ ] **30.** Verify the dead `setAppFlag("companion-data")` writes no longer occur (check console for absence of companion-data flag writes).

### Edge Cases
- [ ] **31.** Delete a companion who has personal quests → verify quests are also cleaned up (or orphaned quests are handled gracefully).
- [ ] **32.** Set influence above 100 → verify it caps at 100 (UI enforcement).
- [ ] **33.** Set influence below 0 → verify it floors at 0.
- [ ] **34.** Create a personal quest with no deadline (turnsRemaining = null) → verify "No deadline" is displayed.
- [ ] **35.** As a non-GM player, verify personal quests are hidden unless revealed by the GM.

---

## Risks & Open Questions

### Risks

1. **Camping gating complexity**: Adding `campAvailable` as a second gating layer means the camping activity list needs to read from both `CampingData.actorUuids` AND `KingdomData.companions`. This requires the camping sheet to have access to kingdom data, which it may not currently have. If the camping sheet cannot access kingdom data, the `campAvailable` check will need to be done differently (e.g., by duplicating the flag on the actor or using a different mechanism).

2. **Companion identification by name**: The existing `isRequiredCompanionPresent()` matches by name (case-insensitive). The new `campAvailable` check also needs to find the companion by name in `KingdomData.companions`. If two companions have the same name, this could produce incorrect gating. This is unlikely in practice but should be documented.

3. **Migration27 backward compat**: Existing companions won't have the new fields. The migration adds defaults, but any code that reads these fields before migration runs will get `null`/`undefined`. All read sites must handle null gracefully (treat null `campAvailable` as true, null `influence` as 0, etc.).

4. **Quest data ownership**: `CompanionPersonalQuest` records are stored in `KingdomData.companionPersonalQuests`, separate from `RawQuest`. This means there are two quest lists to maintain. If a future feature needs a unified quest view, a refactor will be needed.

5. **Discovery "once per session" enforcement**: The plan specifies that discovery attempts are "once per camp session" but doesn't implement a session-tracking mechanism. The UI button is simply disabled after one click per dialog open. A more robust implementation would track this in the data model (e.g., `lastDiscoveryAttemptTurn: Int`), but this is deferred as YAGNI.

### Open Questions for Gregory

1. **Camping sheet kingdom data access**: Does the camping sheet currently have access to `KingdomData`? If not, should we pass the kingdom actor UUID to the camping sheet, or use a different mechanism for the `campAvailable` check?

2. **Influence cap**: Should influence be capped at 100, or can it go higher? Should there be a per-companion maximum based on discovery status (e.g., max 20 at "introduced", max 40 at "established", etc.)?

3. **Personal quest rewards**: Should personal quest rewards auto-apply on completion (e.g., influence gain applied to companion), or should the GM manually apply them? Auto-apply is more convenient but reduces GM control.

4. **Companion deletion behavior**: When a companion is deleted from the roster, what happens to their personal quests? Options: (a) cascade delete, (b) orphan them but keep in data, (c) block deletion if quests exist. Recommended: (c) with a confirmation dialog.

5. **Player-facing companion profile**: Should players be able to view a read-only version of their companion's profile (showing influence, discovery status, visible quests)? Or is the profile GM-only? Recommended: GM-only for now, player view as a future additive feature.

---

## Estimated Sequencing

Implement in this order to minimize blocked dependencies and maximize testability at each step:

1. **Data model changes** (CQ6 tests)
   - Add new fields to `RawCharacter.kt`
   - Add `CompanionPersonalQuest` and `CompanionQuestRewards` to `companion/CompanionQuest.kt`
   - Add `companionPersonalQuests` to `KingdomData.kt`
   - Write `RawCharacterCompanionFieldsTest` — verify new defaults

2. **Migration** (Migration27)
   - Write `Migration27.kt`
   - Register in `Migrations.kt`
   - Test migration on a copy of existing world data

3. **Remove dead companion sync code** (Decision 8)
   - Remove `setAppFlag("companion-data")` loop from `Kingdom.kt`
   - Verify no other code reads `companion-data` flag (confirmed: nothing does)

4. **Context objects**
   - Add new fields to `RosterActorContext` in `RosterContext.kt`
   - Create `CompanionProfileContext.kt`
   - Create `PersonalQuestContext.kt`

5. **Personal quest dialog** (CQ3 tests)
   - Create `AddPersonalQuestDialog.kt`
   - Create `add-personal-quest.hbs`
   - Wire to kingdom sheet action handler

6. **Companion profile dialog** (CQ2 tests)
   - Create `CompanionProfileDialog.kt`
   - Create `companion-profile.hbs`
   - Wire to roster "Profile" button

7. **Roster template changes**
   - Modify `roster/page.hbs` — add influence bar, badges, quest count, profile button
   - Modify `roster-add.hbs` — add influence and discovery status fields
   - Modify `roster-edit.hbs` — add camp availability, discovery status, profile button

8. **Quests page changes**
   - Add "Personal Quests" subsection to `quests/page.hbs`
   - Create `companion-quest-row.hbs` partial
   - Create `companion-quest-card.hbs` partial

9. **Camping gating integration** (CQ4 tests)
   - Modify camping sheet context builder to check `campAvailable`
   - Handle backward compat (null = true)
   - Verify existing `CompanionGatingTest` still passes

10. **Integration tests** (CQ5)
    - Write `PersonalQuestIntegrationTest`
    - End-to-end: create quest → verify GM-only → reveal → verify visible

11. **CSS styling**
    - Create `companion-relationship.css`
    - Influence bar, camp badge, discovery icons, quest badges
    - Import in `kingdom-sheet.css` or module CSS entry point

12. **Localization**
    - Add all ~20 keys to `en.json`
    - Verify no missing localization warnings in console

13. **Manual verification**
    - Run through the full Foundry Verification Checklist above
    - Fix any issues found
