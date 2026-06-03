# Quest/Event Generator Implementation Plan

> **For Hermes:** Use subagent-driven-development skill to implement this plan task-by-task.

**Goal:** Implement a quest/event generator (roadmap item #2) that turns kingdom events into actionable quests, complications, and rewards. Building on the existing `RawQuest` model, `data/events/`, and `event-browser.hbs`, the generator adds quest generation from kingdom events, a quest generation dialog, optional one-click campaign quest creation from templates, event-to-quest consequence application, and GM-only/generated quest distinction with player visibility controls.

**Architecture:** New `GenerateQuestDialog` (Foundry `FormApp` subclass) reads the current kingdom event list and `QuestTemplate` campaign data, then produces a `CampaignQuest` record via the existing `AddQuest` dialog path. A `QuestGenerator` pure-function engine computes quest parameters (type, urgency, rewards) from the selected event and current kingdom state. A `QuestGeneratorSettings` dialog lets the GM configure generator defaults per campaign. The `TurnTickingEngine` tick pipeline hooks into a new generator hook to auto-advance quest resolution and event generation on kingdom turns. Generated quests are GM-only by default (Decision 2).

**Tech Stack:** Kotlin Multiplatform jsMain/jsTest, Foundry `FormApp` / `CrudApplication` dialogs, Handlebars templates, JSON Schema (draft 2020-12), `kotlin.test`, existing `AddQuest` / `AddEvent` dialog patterns.

---

## Source of truth

- Roadmap item #2: `docs/feature-roadmap.md` lines 60-81
- Design decisions (card f0): `docs/plans/2026-06-01-roadmap-design-decisions.md` — Decision 2 (GM-only default), Decision 3 (hybrid clocks), Decisions 1 + 5 (structured aggregation, single Gregory profile)
- Existing quest model: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/dialogs/AddQuest.kt`
- Existing quest rewards: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/data/RawQuestRewards.kt`
- Existing event model: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/dialogs/AddEvent.kt`
- Existing events data: `data/events/`
- Existing event browser: `src/jsMain/resources/applications/kingdom/event-browser.hbs`
- Existing events template: `src/jsMain/resources/applications/kingdom/events.hbs`
- Existing kingdom sheet: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/KingdomSheet.kt`
- Existing kingdom data: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/data/KingdomData.kt`
- Existing turn engine: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/TurnTickingEngine.kt`
- Existing quests page: `src/jsMain/resources/applications/kingdom/sections/quests/page.hbs`
- Existing quest chat: `src/jsMain/resources/chatmessages/quest-completed.hbs`
- Existing Handlebars helpers: `src/jsMain/kotlin/at/posselt/pfrpg2e/utils/Localization.kt`
- Existing migration pattern: `src/jsMain/kotlin/at/posselt/pfrpg2e/migrations/migrations/Migration26.kt`
- Existing migration registry: `src/jsMain/kotlin/at/posselt/pfrpg2e/migrations/Migrations.kt`
- Existing settings pattern: `src/jsMain/kotlin/at/posselt/pfrpg2e/settings/Pfrpg2eKingdomCampingWeatherSettings.kt`
- Existing localization: `src/jsMain/resources/lang/en.json`
- Existing test patterns: `src/jsTest/kotlin/at/posselt/pfrpg2e/kingdom/`

## Non-goals

- Do NOT implement campaign clock data models or UI (roadmap item #1, separate feature). Quest due dates are simple turn counts, not clock-integrated.
- Do NOT add pacing/balance alert thresholds (roadmap item #13, separate feature).
- Do NOT implement balance pacing for quest generation frequency — the GM manually triggers generation; turn-tick hooks are passive.
- Do NOT add homebrew profile import/export (roadmap item #9, separate feature). The generator reads `KingdomSettings` toggles directly, consistent with Decision 1.
- Do NOT auto-transmute hex discoveries into quests. Hex hooks are sourced from the existing `data/events/` and event-browser, not from hex state.

---

## Affected Files

### New files to create

| File | Purpose |
|------|---------|
| `src/commonMain/kotlin/at/posselt/pfrpg2e/questevent/QuestTemplate.kt` | Campaign-scoped quest template data class (id, name, type, description, recommendedLevel, objectives, rewards, sourceEventTraits, visibility) |
| `src/commonMain/kotlin/at/posselt/pfrpg2e/questevent/CampaignQuest.kt` | Player-facing quest instance extending template with campaign state (status, currentObjectives, turnsRemaining, assignedPCs, visibleToPlayers, generatedByEvent) |
| `src/commonMain/kotlin/at/posselt/pfrpg2e/questevent/QuestObjective.kt` | Sub-objective model (id, description, completed, optional)
| `src/commonMain/kotlin/at/posselt/pfrpg2e/questevent/KingdomEventTemplate.kt` | Event template with quest-generation hooks (triggerConditions, suggestedQuestTemplateIds, consequenceOverrides)
| `src/commonMain/kotlin/at/posselt/pfrpg2e/questevent/CampaignKingdomEvent.kt` | Running kingdom event with quest link (eventTemplateId, generationLog, resolvedAt, spawnedQuestIds)
| `src/commonMain/kotlin/at/posselt/pfrpg2e/questevent/EventGenerationLog.kt` | Audit log table for event generation runs (turnNumber, eventsGenerated, questsCreated, timestamp)
| `src/commonMain/kotlin/at/posselt/pfrpg2e/questevent/QuestGenerator.kt` | Pure-function engine: maps event + kingdom state → quest parameters (type, urgency, rewards, objectives)
| `src/jsMain/kotlin/at/posselt/pfrpg2e/questevent/GenerateQuestDialog.kt` | Main `FormApp` dialog: event selection, generator preview, quest parameter editing, GM-only toggle, commit |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/questevent/QuestGeneratorSettings.kt` | `CrudApplication` dialog: generator default configuration per campaign |
| `src/jsMain/resources/applications/kingdom/generate-quest-dialog.hbs` | Handlebars template: event picker, quest preview card, reward editor, visibility toggle, action buttons |
| `src/jsMain/resources/applications/kingdom/quest-generator-settings.hbs` | Handlebars template: generator settings form |
| `src/jsMain/resources/applications/kingdom/quest-card-generated-badge.hbs` | Partial template: small badge showing "Generated from [Event Name]" on quest cards |
| `src/jsMain/resources/applications/kingdom/quest-generator.hbs` | Generator panel partial embedded in the Events section of the kingdom sheet |
| `src/jsMain/resources/applications/kingdom/quest-generator.css` | New CSS for generator UI elements (event picker, quest preview, badge, settings) |
| `src/commonMain/resources/schemas/quest-template.json` | JSON Schema (draft 2020-12) validating quest template data |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/migrations/migrations/Migration28.kt` | Migration28 — adds `quest_templates`, `campaign_quests`, `kingdom_event_templates`, `campaign_kingdom_events`, `quest_objectives`, `event_generation_log` to KingdomData |
| `src/jsTest/kotlin/at/posselt/pfrpg2e/questevent/QuestTemplateTest.kt` | Unit tests for QuestTemplate data model (Test Q1) |
| `src/jsTest/kotlin/at/posselt/pfrpg2e/questevent/CampaignQuestTest.kt` | Unit tests for CampaignQuest state transitions (Test Q2) |
| `src/jsTest/kotlin/at/posselt/pfrpg2e/questevent/QuestGeneratorTest.kt` | Unit tests for pure-function generator engine (Test Q3) |
| `src/jsTest/kotlin/at/posselt/pfrpg2e/questevent/GenerateQuestDialogTest.kt` | Unit tests for dialog state computation (Test Q4) |
| `src/jsTest/kotlin/at/posselt/pfrpg2e/questevent/QuestGeneratorSettingsTest.kt` | Unit tests for settings defaults and validation (Test Q5) |
| `src/jsTest/kotlin/at/posselt/pfrpg2e/questevent/EventToQuestIntegrationTest.kt` | Integration test: create event → generate quest → verify campaign_quest record (Test I1) |
| `src/jsTest/kotlin/at/posselt/pfrpg2e/questevent/QuestRewardApplicationIntegrationTest.kt` | Integration test: quest completion applies XP/RP/commodity rewards (Test I2) |

### Existing files to modify

| File | Change |
|------|--------|
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/dialogs/AddQuest.kt` | Add `generatedFromEvent` boolean + `sourceEventId` fields; pre-populate from generator payload; add `visibleToPlayers` default false (Decision 2) |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/dialogs/AddEvent.kt` | Add "Generate Quest from Event" button to each event entry in the browser; wire action handler |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/KingdomSheet.kt` | Add quest-generator panel context data; expose `generatedQuestCount` and `activeEventCount` for the generator section |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/data/KingdomData.kt` | Add `questTemplates: List<QuestTemplate>`, `campaignQuests: List<CampaignQuest>`, `kingdomEventTemplates: List<KingdomEventTemplate>`, `campaignKingdomEvents: List<CampaignKingdomEvent>`, `eventGenerationLogs: List<EventGenerationLog>` fields |
| `src/jsMain/resources/applications/kingdom/event-browser.hbs` | Add "Generate Quest" button next to each event entry; add generated-quest badge to events with resolved/generated quests |
| `src/jsMain/resources/applications/kingdom/events.hbs` | Add quest-generator section below the event list; show active generated quests count; add "Open Generator" button |
| `src/jsMain/resources/applications/kingdom/sections/quests/page.hbs` | Add generated-quest badge partial; add "Generated" filter toggle; show source event link on quest cards |
| `src/jsMain/resources/applications/kingdom/sections/turn/page.hbs` | Add quest-auto-advance call on tick hook (call `QuestGenerator.advanceQuestTimers()`) |
| `src/jsMain/resources/applications/kingdom/kingdom-sheet.hbs` | Add "Quest Generator" section tab if not inline in events |
| `src/jsMain/resources/chatmessages/quest-completed.hbs` | Add reward summary display (XP gained, RP gained, commodities awarded); conditionally show for generated quests |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/utils/Localization.kt` | Register new `{{json}}` Handlebars helper for serializing quest data objects in templates |
| `src/jsMain/resources/lang/en.json` | Add ~25 localization keys (see section below) |
| `src/jsMain/kotlin/at/posselt/pfrpg2e/migrations/Migrations.kt` | Register `Migration28` in the migration list |

### New Handlebars helper

Register a `json` helper in `Localization.kt`:

```kotlin
window.Handlebars.registerHelper("json", { obj: Any -> JSON.stringify(obj) })
```

This allows templates to embed quest object data attributes: `<div data-quest="{{json quest}}">`.

### Localization keys to add

All in `src/jsMain/resources/lang/en.json` under a `kingdom.questGenerator` namespace:

```json
"kingdom.questGenerator.title": "Quest Generator",
"kingdom.questGenerator.open": "Open Generator",
"kingdom.questGenerator.generateFromEvent": "Generate Quest from Event",
"kingdom.questGenerator.preview": "Quest Preview",
"kingdom.questGenerator.preview.event": "Source Event",
"kingdom.questGenerator.preview.type": "Quest Type",
"kingdom.questGenerator.preview.urgency": "Urgency",
"kingdom.questGenerator.preview.rewards": "Suggested Rewards",
"kingdom.questGenerator.preview.objectives": "Objectives",
"kingdom.questGenerator.preview.recommendedLevel": "Recommended Level",
"kingdom.questGenerator.settings": "Generator Settings",
"kingdom.questGenerator.settings.title": "Quest Generator Settings",
"kingdom.questGenerator.settings.defaultVisibility": "Default Quest Visibility",
"kingdom.questGenerator.settings.defaultVisibility.gmOnly": "GM Only",
"kingdom.questGenerator.settings.defaultVisibility.players": "Players",
"kingdom.questGenerator.settings.maxActiveQuests": "Max Active Generated Quests",
"kingdom.questGenerator.settings.autoAdvanceTimers": "Auto-Advance Quest Timers on Turn",
"kingdom.questGenerator.visibility.makeVisible": "Reveal to Players",
"kingdom.questGenerator.visibility.makeHidden": "Hide from Players",
"kingdom.questGenerator.generate": "Generate Quest",
"kingdom.questGenerator.regenerate": "Regenerate",
"kingdom.questGenerator.commit": "Add to Campaign",
"kingdom.questGenerator.cancel": "Cancel",
"kingdom.questGenerator.generatedFrom": "Generated from: {eventName}",
"kingdom.questGenerator.generatedBadge": "Generated",
"kingdom.questGenerator.filterGenerated": "Generated Quests",
"kingdom.questGenerator.activeCount": "Active Quests: {count}",
"kingdom.questGenerator.noEvents": "No eligible events — resolve kingdom events first"
```

---

## Data Models

### QuestTemplate (commonMain)

```kotlin
package at.posselt.pfrpg2e.questevent

data class QuestTemplate(
    val id: String,
    val name: String,
    val type: QuestType,          // e.g., COMBAT, EXPLORATION, RP, POLITICAL, CRAFTING
    val description: String,
    val gmNotes: String? = null,
    val recommendedLevel: Int,
    val objectives: List<QuestObjective>,
    val rewards: QuestRewards,
    val sourceEventTraits: List<String> = emptyList(),  // matching KingdomEventTrait names
    val isDefaultVisibleToPlayers: Boolean = false,      // Decision 2: default false
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

enum class QuestType(val value: String) {
    COMBAT("combat"),
    EXPLORATION("exploration"),
    RP("roleplay"),
    POLITICAL("political"),
    CRAFTING("crafting"),
    TRAVEL("travel");

    companion object {
        fun fromString(value: String) = entries.firstOrNull { it.value == value }
    }
}
```

### CampaignQuest (commonMain)

Extends the template with campaign-running state:

```kotlin
package at.posselt.pfrpg2e.questevent

data class CampaignQuest(
    val id: String,
    val templateId: String?,
    val name: String,
    val type: QuestType,
    val description: String,
    val gmNotes: String? = null,
    val recommendedLevel: Int,
    val objectives: List<QuestObjective>,
    val rewards: QuestRewards,     // snapshot from template at creation time
    val status: QuestStatus = QuestStatus.ACTIVE,
    val turnsRemaining: Int? = null,  // null = no deadline
    val assignedPcIds: List<String> = emptyList(),
    val visibleToPlayers: Boolean = false,  // Decision 2: GM-only default
    val generatedByEvent: Boolean = false,
    val sourceEventId: String? = null,
    val sourceEventName: String? = null,   // display name for badge
    val createdAt: String,
    val completedAt: String? = null,
    val campaignId: String,                // campaign_id FK with CASCADE isolation
)

enum class QuestStatus(val value: String) {
    ACTIVE("active"),
    COMPLETED("completed"),
    FAILED("failed"),
    ABANDONED("abandoned"),
    ON_HOLD("on_hold");

    companion object {
        fun fromString(value: String) = entries.firstOrNull { it.value == value }
    }
}
```

**State machine:**
```
ACTIVE → COMPLETED (GM resolves, rewards applied)
ACTIVE → FAILED (quest failed: turns expired, objective failed)
ACTIVE → ON_HOLD (GM marks as paused)
ACTIVE → ABANDONED (GM dismisses)
ON_HOLD → ACTIVE (resume)
ON_HOLD → ABANDONED
```

### QuestObjective (commonMain)

```kotlin
package at.posselt.pfrpg2e.questevent

data class QuestObjective(
    val id: String,
    val description: String,
    val completed: Boolean = false,
    val optional: Boolean = false,
)
```

### QuestRewards (commonMain)

Snapshot from template; decoupled from template for scaling:

```kotlin
package at.posselt.pfrpg2e.questevent

data class QuestRewards(
    val xp: Int = 0,
    val rp: Int = 0,
    val fame: Int = 0,
    val commodities: Map<String, Int> = emptyMap(),  // e.g., {"lumber": 2, "stone": 1}
    val unrestReduction: Int = 0,
    val structureAccessGranted: String? = null,       // template id of unlocked structure
    val customReward: String? = null,
)
```

### KingdomEventTemplate (commonMain)

```kotlin
package at.posselt.pfrpg2e.questevent

data class KingdomEventTemplate(
    val id: String,
    val name: String,
    val description: String,
    val traits: List<String>,
    val triggerConditions: Map<String, Any> = emptyMap(),  // JSON blob for extensibility
    val suggestedQuestTemplateIds: List<String> = emptyList(),
    val consequenceOverrides: Map<String, Any> = emptyMap(),
)
```

### CampaignKingdomEvent (commonMain)

```kotlin
package at.posselt.pfrpg2e.questevent

data class CampaignKingdomEvent(
    val id: String,
    val eventTemplateId: String,
    val name: String,
    val description: String,
    val status: EventStatus = EventStatus.ACTIVE,
    val spawnedQuestIds: List<String> = emptyList(),
    val turnsActive: Int = 0,
    val createdAt: String,
    val resolvedAt: String? = null,
)

enum class EventStatus(val value: String) {
    ACTIVE("active"),
    RESOLVED("resolved"),
    EXPIRED("expired"),
    GENERATED_QUEST("generated_quest");
}
```

### EventGenerationLog (commonMain)

```kotlin
package at.posselt.pfrpg2e.questevent

data class EventGenerationLog(
    val id: String,
    val turnNumber: Int,
    val eventsGenerated: Int = 0,
    val questsCreated: Int = 0,
    val timestamp: String,
    val campaignId: String,
)
```

### GenerateQuestContext (jsMain — JS interface for Handlebars)

```kotlin
@JsPlainObject
external interface QuestGeneratorContext : ValidatedHandlebarsContext {
    val events: List<SummarizedEventContext>
    val selectedEvent: SelectedEventContext?
    val questPreview: QuestPreviewContext?
    val showPreview: Boolean
    val canGenerate: Boolean
    val settings: QuestGeneratorSettingsContext
    val generatedQuestCount: Int
    val maxQuests: Int
}
```

Each sub-context is a small JS-plain object with localized labels and values.

### QuestGeneratorSettings (commonMain + jsMain data model)

Stored as a Foundry setting:

```kotlin
data class QuestGeneratorSettings(
    val defaultVisibilityToPlayers: Boolean = false,
    val maxActiveGeneratedQuests: Int = 10,
    val autoAdvanceQuestTimersOnTurn: Boolean = true,
)
```

---

## Migrations

Migration28 (`Migration28.kt`) adds quest/event generator fields to `KingdomData`. This is a versioned migration consistent with existing patterns (Migration26, Migration27).

### KingdomData additions

| Field | Type | Default | Purpose |
|-------|------|---------|---------|
| `questTemplates` | `List<QuestTemplate>` | `[]` | Campaign-scoped quest templates |
| `campaignQuests` | `List<CampaignQuest>` | `[]` | Active/completed quest instances |
| `kingdomEventTemplates` | `List<KingdomEventTemplate>` | `[]` | Event templates with quest-gen hooks |
| `campaignKingdomEvents` | `List<CampaignKingdomEvent>` | `[]` | Running kingdom events with quest links |
| `eventGenerationLogs` | `List<EventGenerationLog>` | `[]` | Audit log for each generation run |
| `questGeneratorSettings` | `QuestGeneratorSettings` | defaults | Generator configuration |

### CampingData additions

None — quest/event generator is kingdom-sheet only.

### Seeding

Migration28 seeds:
1. Default `QuestGeneratorSettings` (GM-only default, max 10, auto-advance on)
2. Backwards-compatible: existing `RawQuest` records remain untouched; `CampaignQuest` is additive
3. No event template seeding — templates come from `data/events/` data packs, loaded at runtime

### Frontend migration logic

In `KingdomData.kt`:
- Add a `fun Migration28Data(currentData: QuestGeneratorData?): QuestGeneratorData` builder
- On load, check `migrationVersion < 28` → set new fields to defaults

### Rollback

If migration fails, the new fields default to empty lists. No destructive changes to existing data. Rollback is simply not persisting the migration version.

### appFlag keys

| Key | Shape | Purpose |
|-----|-------|---------|
| `kingdom-sheet.quest-data` | QuestGeneratorData wrapper | All quest/event generator state |

---

## Implementation Tasks

### Phase 1: Core Data Models (commonMain)

---

### Task 1: Add failing test for QuestTemplate data class

**Objective:** Lock in QuestTemplate field structure before implementation.

**Files:**
- Create: `src/jsTest/kotlin/at/posselt/pfrpg2e/questevent/QuestTemplateTest.kt`

**Step 1: Write failing test**

```kotlin
package at.posselt.pfrpg2e.questevent

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class QuestTemplateTest {
    @Test
    fun `QuestTemplate defaults match expectations`() {
        val template = QuestTemplate(
            id = "test-1",
            name = "Test Quest",
            type = QuestType.COMBAT,
            description = "A test quest",
            recommendedLevel = 5,
            objectives = emptyList(),
            rewards = QuestRewards(),
        )
        assertFalse(template.isDefaultVisibleToPlayers)  // Decision 2: default false
        assertEquals(emptyList(), template.sourceEventTraits)
    }

    @Test
    fun `GM-only visibility is default`() {
        val tmpl = QuestTemplate(
            id = "test-2",
            name = "Hidden Quest",
            type = QuestType.EXPLORATION,
            description = "GM only quest",
            recommendedLevel = 3,
            objectives = listOf(QuestObjective(id = "obj1", description = "Find the ruin")),
            rewards = QuestRewards(xp = 80, rp = 5),
        )
        assertFalse(tmpl.isDefaultVisibleToPlayers)
    }
}
```

**Step 2: Run test to verify failure**

Run: `JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew jsTest --tests "at.posselt.pfrpg2e.questevent.QuestTemplateTest"`
Expected: FAIL — `QuestTemplate` not found

---

### Task 2: Implement QuestTemplate data class

**Objective:** Create `QuestTemplate`, `QuestObjective`, `QuestRewards`, and `QuestType` models.

**Files:**
- Create: `src/commonMain/kotlin/at/posselt/pfrpg2e/questevent/QuestTemplate.kt`
- Create: `src/commonMain/kotlin/at/posselt/pfrpg2e/questevent/QuestObjective.kt`
- Create: `src/commonMain/kotlin/at/posselt/pfrpg2e/questevent/CampaignQuest.kt`

**Step 1: Implement** — use the data models defined in the Data Models section above.

**Step 2: Run test to verify pass**

Run: `JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew jsTest --tests "at.posselt.pfrpg2e.questevent.QuestTemplateTest"`
Expected: PASS

**Step 3: Commit**

```bash
git add src/commonMain/kotlin/at/posselt/pfrpg2e/questevent/*.kt src/jsTest/kotlin/at/posselt/pfrpg2e/questevent/QuestTemplateTest.kt
git commit -m "feat(quest-event): add QuestTemplate, CampaignQuest, QuestObjective data models"
```

---

### Task 3: Add failing test for CampaignQuest state transitions

**Objective:** Lock in status transitions (ACTIVE → COMPLETED, etc.)

**Files:**
- Create: `src/jsTest/kotlin/at/posselt/pfrpg2e/questevent/CampaignQuestTest.kt`

**Step 1: Write failing test** — test that a quest can transition through valid states:

```kotlin
package at.posselt.pfrpg2e.questevent

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CampaignQuestTest {
    private fun makeQuest(
        status: QuestStatus = QuestStatus.ACTIVE,
        turnsRemaining: Int? = null,
    ) = CampaignQuest(
        id = "cq-1",
        templateId = "qt-1",
        name = "Test Quest",
        type = QuestType.COMBAT,
        description = "Test",
        recommendedLevel = 5,
        objectives = emptyList(),
        rewards = QuestRewards(xp = 40),
        status = status,
        turnsRemaining = turnsRemaining,
        createdAt = "2026-06-01T00:00:00Z",
        campaignId = "camp-1",
    )

    @Test
    fun `new quest defaults to ACTIVE and hidden`() {
        val q = makeQuest()
        assertEquals(QuestStatus.ACTIVE, q.status)
        assertFalse(q.visibleToPlayers)
    }

    @Test
    fun `status values serialize correctly`() {
        assertEquals("active", QuestStatus.ACTIVE.value)
        assertEquals("completed", QuestStatus.COMPLETED.value)
        assertEquals("failed", QuestStatus.FAILED.value)
        assertEquals("abandoned", QuestStatus.ABANDONED.value)
        assertEquals("on_hold", QuestStatus.ON_HOLD.value)
    }

    @Test
    fun `turnsRemaining null means no deadline`() {
        val q = makeQuest()
        assertEquals(null, q.turnsRemaining)
    }
}
```

**Step 2: Run test to verify failure** → Expected: FAIL

---

### Task 4: Implement remaining event models

**Objective:** Add KingdomEventTemplate, CampaignKingdomEvent, EventGenerationLog.

**Files:**
- Create: `src/commonMain/kotlin/at/posselt/pfrpg2e/questevent/KingdomEventTemplate.kt`
- Create: `src/commonMain/kotlin/at/posselt/pfrpg2e/questevent/CampaignKingdomEvent.kt`
- Create: `src/commonMain/kotlin/at/posselt/pfrpg2e/questevent/EventGenerationLog.kt`

**Step 1: Implement** — use models from Data Models section.

**Step 2: Run test** → Expected: PASS

**Step 3: Commit**

```bash
git add src/commonMain/kotlin/at/posselt/pfrpg2e/questevent/*.kt src/jsTest/kotlin/at/posselt/pfrpg2e/questevent/CampaignQuestTest.kt
git commit -m "feat(quest-event): add event models and CampaignQuest state transitions"
```

---

### Task 5: Add failing test for QuestGenerator engine

**Objective:** Lock in generator logic — event + kingdom state → quest params.

**Files:**
- Create: `src/jsTest/kotlin/at/posselt/pfrpg2e/questevent/QuestGeneratorTest.kt`

**Step 1: Write failing test** — test that the generator maps events to quest parameters:

```kotlin
package at.posselt.pfrpg2e.questevent

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class QuestGeneratorTest {
    private val sampleEvent = KingdomEventTemplate(
        id = "evt-crop-failure",
        name = "Crop Failure",
        description = "Crops are failing across the kingdom.",
        traits = listOf("agriculture", "negative"),
        suggestedQuestTemplateIds = listOf("qt-druid-conflict"),
    )

    @Test
    fun `generator maps event traits to quest type`() =
        // Agriculture + negative → EXPLORATION + RP quest
        // (implementation-defined mapping; test what the generator actually produces)
        {
            val result = QuestGenerator.generateFromEvent(
                event = sampleEvent,
                kingdomLevel = 4,
                currentQuests = emptyList(),
            )
            // The generator should produce a non-null quest proposal
            assertEquals("Crop Failure", result.sourceEventName)
            assertFalse(result.preview.isDefaultVisibleToPlayers)
        }

    @Test
    fun `generator respects max active quests limit`() {
        val activeQuests = (1..10).map {
            makeQuest(status = QuestStatus.ACTIVE)
        }
        val result = QuestGenerator.generateFromEvent(
            event = sampleEvent,
            kingdomLevel = 4,
            currentQuests = activeQuests,
            settings = QuestGeneratorSettings(maxActiveGeneratedQuests = 10),
        )
        // Should return null or a result indicating limit reached
        // (define behavior in implementation)
    }
}
```

**Step 2: Run test** → Expected: FAIL

---

### Task 6: Implement QuestGenerator engine

**Objective:** Pure-function generator — maps kingdom events to quest parameters.

**Files:**
- Create: `src/commonMain/kotlin/at/posselt/pfrpg2e/questevent/QuestGenerator.kt`

**Generator logic rules:**

| Event traits | Suggested quest type | Reward bias |
|---|---|---|
| `agriculture` + `negative` | EXPLORATION | RP, commodities |
| `political` + `negative` | POLITICAL | RP, fame |
| `military` + `threat` | COMBAT | XP, RP |
| `supernatural` | EXPLORATION | XP, structure access |
| `positive` | RP or CRAFTING | Fame, commodities |

**Method signature:**

```kotlin
data class QuestGenerationResult(
    val sourceEventId: String,
    val sourceEventName: String,
    val preview: QuestTemplate,
    val isEligible: Boolean,
    val ineligibilityReason: String? = null,
)

object QuestGenerator {
    fun generateFromEvent(
        event: KingdomEventTemplate,
        kingdomLevel: Int,
        currentQuests: List<CampaignQuest>,
        settings: QuestGeneratorSettings = QuestGeneratorSettings(),
    ): QuestGenerationResult?

    fun advanceQuestTimers(
        quests: List<CampaignQuest>,
        kingdomLevel: Int,
    ): List<CampaignQuest>

    fun canGenerateMore(
        currentQuests: List<CampaignQuest>,
        settings: QuestGeneratorSettings,
    ): Boolean
        = currentQuests.count { it.status == QuestStatus.ACTIVE && it.generatedByEvent } < settings.maxActiveGeneratedQuests
}
```

**Step 2: Run test** → Expected: PASS

**Step 3: Commit**

```bash
git add src/commonMain/kotlin/at/posselt/pfrpg2e/questevent/QuestGenerator.kt src/jsTest/kotlin/at/posselt/pfrpg2e/questevent/QuestGeneratorTest.kt
git commit -m "feat(quest-event): implement QuestGenerator pure-function engine"
```

---

### Phase 2: UI and Templates (jsMain)

---

### Task 7: Add failing test for GenerateQuestDialog

**Objective:** Lock in dialog state generation.

**Files:**
- Create: `src/jsTest/kotlin/at/posselt/pfrpg2e/questevent/GenerateQuestDialogTest.kt`

**Step 1: Write failing test** — test dialog state produces correct context:

```kotlin
package at.posselt.pfrpg2e.questevent

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class GenerateQuestDialogTest {
    @Test
    fun `dialog emits QuestGeneratorContext with events list`() {
        // Test that generateContext() produces a context with the events list populated
        // Verify generatedQuestCount and maxQuests are passed through
    }

    @Test
    fun `dialog respects canGenerateMore limit`() {
        // Test that canGenerate is false when max quests reached
    }
}
```

**Step 2: Run test** → Expected: FAIL

---

### Task 8: Implement GenerateQuestDialog

**Objective:** Create the main GM-facing quest generation dialog.

**Files:**
- Create: `src/jsMain/kotlin/at/posselt/pfrpg2e/questevent/GenerateQuestDialog.kt`
- Create: `src/jsMain/resources/applications/kingdom/generate-quest-dialog.hbs`
- Create: `src/jsMain/resources/applications/kingdom/quest-card-generated-badge.hbs`
- Create: `src/jsMain/resources/applications/kingdom/quest-generator.hbs`
- Create: `src/jsMain/resources/applications/kingdom/quest-generator.css`

**Dialog flow:**
1. **Event picker** — list active kingdom events, filterable by traits
2. **Quest preview card** — shows generated quest name, type, urgency, objectives, rewards
3. **Settings gear** — opens `QuestGeneratorSettings` dialog
4. **Visibility toggle** — "GM Only" / "Visible to Players" (default: GM Only)
5. **Action buttons** — "Generate Quest" (preview → confirm), "Regenerate", "Add to Campaign", "Cancel"

**Template layout (generate-quest-dialog.hbs):**

```handlebars
<form>
  <h2>{{localizeKM "kingdom.questGenerator.title"}}</h2>

  {{!-- Event picker --}}
  <section class="km-qg-event-picker">
    <h3>{{localizeKM "kingdom.questGenerator.preview.event"}}</h3>
    {{#if events.length}}
      <ul class="km-qg-events-list">
        {{#each events}}
          <li data-event-id="{{id}}">
            <span class="km-qg-event-name">{{name}}</span>
            {{#each traits}}<span class="km-qg-trait">{{this}}</span>{{/each}}
          </li>
        {{/each}}
      </ul>
    {{else}}
      <p>{{localizeKM "kingdom.questGenerator.noEvents"}}</p>
    {{/if}}
  </section>

  {{!-- Preview card --}}
  {{#if showPreview}}
  <section class="km-qg-preview">
    <div class="km-qg-preview-header">
      <span class="km-qg-badge">{{localizeKM "kingdom.questGenerator.generatedBadge"}}</span>
    </div>
    <h3>{{questPreview.name}}</h3>
    <p>{{questPreview.description}}</p>
    <ul>
      {{#each questPreview.objectives}}
      <li>{{description}}</li>
      {{/each}}
    </ul>
  </section>
  {{/if}}

  {{!-- Visibility --}}
  <section class="km-qg-visibility">
    <label>
      <input type="checkbox" name="visibleToPlayers" {{#unless questPreview.isDefaultVisibleToPlayers}}checked{{/unless}}" />
      {{localizeKM "kingdom.questGenerator.settings.defaultVisibility.players"}}
    </label>
  </section>

  {{!-- Actions --}}
  <footer>
    <button type="button" data-action="generate">{{localizeKM "kingdom.questGenerator.generate"}}</button>
    <button type="button" data-action="commit" {{#unless canGenerate}}disabled{{/unless}}>{{localizeKM "kingdom.questGenerator.commit"}}</button>
    <button type="button" data-action="cancel">{{localizeKM "kingdom.questGenerator.cancel"}}</button>
  </footer>
</form>
```

**Step 2: Run test** → Expected: PASS

**Step 3: Commit**

```bash
git add src/jsMain/kotlin/at/posselt/pfrpg2e/questevent/GenerateQuestDialog.kt \
        src/jsMain/resources/applications/kingdom/generate-quest-dialog.hbs \
        src/jsMain/resources/applications/kingdom/quest-card-generated-badge.hbs \
        src/jsMain/resources/applications/kingdom/quest-generator.hbs \
        src/jsMain/resources/applications/kingdom/quest-generator.css \
        src/jsTest/kotlin/at/posselt/pfrpg2e/questevent/GenerateQuestDialogTest.kt
git commit -m "feat(quest-event): implement GenerateQuestDialog with preview and visibility toggle"
```

---

### Task 9: Implement QuestGeneratorSettings dialog

**Objective:** Let the GM configure generator defaults.

**Files:**
- Create: `src/jsMain/kotlin/at/posselt/pfrpg2e/questevent/QuestGeneratorSettings.kt`
- Create: `src/jsMain/resources/applications/kingdom/quest-generator-settings.hbs`
- Create/test: `src/jsTest/kotlin/at/posselt/pfrpg2e/questevent/QuestGeneratorSettingsTest.kt`

**Settings:**
- Default quest visibility (GM Only / Players)
- Max active generated quests (default 10)
- Auto-advance quest timers on kingdom turn (checkbox, default on)

Follow the existing `KingdomSettings.kt` pattern.

**Step: Commit**

```bash
git add src/jsMain/kotlin/at/posselt/pfrpg2e/questevent/QuestGeneratorSettings.kt \
        src/jsMain/resources/applications/kingdom/quest-generator-settings.hbs \
        src/jsTest/kotlin/at/posselt/pfrpg2e/questevent/QuestGeneratorSettingsTest.kt
git commit -m "feat(quest-event): implement QuestGeneratorSettings dialog"
```

---

### Phase 3: Wire into existing files

---

### Task 10: Modify AddQuest to accept generator payload

**Objective:** Wire the generator output into the existing quest creation dialog.

**Files:**
- Modify: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/dialogs/AddQuest.kt`

**Changes:**
1. Add `generatedFromEvent: Boolean = false` and `sourceEventId: String? = null` to the dialog data class
2. Pre-populate from a generator payload passed via dialog options
3. Default `visibleToPlayers = false` (Decision 2) — GM must explicitly reveal

---

### Task 11: Modify AddEvent / event-browser to add "Generate Quest" button

**Objective:** Add entry point for quest generation from existing event dialogs.

**Files:**
- Modify: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/dialogs/AddEvent.kt`
- Modify: `src/jsMain/resources/applications/kingdom/event-browser.hbs`
- Modify: `src/jsMain/resources/applications/kingdom/events.hbs`

**Changes:**
1. In `event-browser.hbs`: add a "Generate Quest" button next to each event entry (in the `<summary>` row)
2. In `AddEvent.kt`: add `data-action="generate-quest-from-event"` handler that opens `GenerateQuestDialog` with the event pre-selected
3. In `events.hbs`: add a generator panel at the bottom with active quest count and "Open Generator" button

**Step: Commit**

```bash
git add src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/dialogs/AddQuest.kt \
        src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/dialogs/AddEvent.kt \
        src/jsMain/resources/applications/kingdom/event-browser.hbs \
        src/jsMain/resources/applications/kingdom/events.hbs
git commit -m "feat(quest-event): wire generator button into AddEvent and event-browser"
```

---

### Task 12: Modify KingdomData, KingdomSheet, quests page

**Objective:** Add quest/event generator state to the data model and sheet.

**Files:**
- Modify: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/data/KingdomData.kt`
- Modify: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/KingdomSheet.kt`
- Modify: `src/jsMain/resources/applications/kingdom/sections/quests/page.hbs`
- Modify: `src/jsMain/resources/applications/kingdom/kingdom-sheet.hbs`

**Changes in `KingdomData.kt`:**
- Add the 6 quest/event generator fields listed in the Migrations section
- Provide default values (empty lists)

**Changes in `KingdomSheet.kt`:**
- Expose `generatedQuestCount: Int` and `activeKingdomEventCount: Int` via sheet data
- Wire quest generator section data

**Changes in `quests/page.hbs`:**
- Include `quest-card-generated-badge.hbs` partial on quest cards where `generatedByEvent == true`
- Add a "Generated" filter toggle beside existing filters
- Show source event name link on generated quest cards

**Step: Commit**

```bash
git add src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/data/KingdomData.kt \
        src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/KingdomSheet.kt \
        src/jsMain/resources/applications/kingdom/sections/quests/page.hbs \
        src/jsMain/resources/applications/kingdom/kingdom-sheet.hbs
git commit -m "feat(quest-event): add generator state to KingdomData and sheet UI"
```

---

### Task 13: Wire turn-tick hook

**Objective:** Auto-advance quest timers and event generation on kingdom turn.

**Files:**
- Modify: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/TurnTickingEngine.kt`
- Modify: `src/jsMain/resources/applications/kingdom/sections/turn/page.hbs`

**Changes in `TurnTickingEngine.kt`:**
- Add a `tickQuests(kingdom: KingdomData, settings: QuestGeneratorSettings): List<TickChange>` method
- Called from the existing tick pipeline
- Decrements `turnsRemaining` on active generated quests; marks quests as FAILED when timer hits 0
- Each quest timer change produces a `TickChange("quest", "turnsRemaining", old, new)` entry

**Changes in `turn/page.hbs`:**
- Display quest timer changes in the turn summary section

**Step: Commit**

```bash
git add src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/TurnTickingEngine.kt \
        src/jsMain/resources/applications/kingdom/sections/turn/page.hbs
git commit -m "feat(quest-event): wire quest timer advancement into TurnTickingEngine"
```

---

### Task 14: Migration28, localization, Handlebars helper

**Objective:** Add the migration, localization keys, and the `json` Handlebars helper.

**Files:**
- Create: `src/jsMain/kotlin/at/posselt/pfrpg2e/migrations/migrations/Migration28.kt`
- Modify: `src/jsMain/kotlin/at/posselt/pfrpg2e/migrations/Migrations.kt`
- Modify: `src/jsMain/resources/lang/en.json`
- Modify: `src/jsMain/resources/chatmessages/quest-completed.hbs`
- Modify: `src/jsMain/kotlin/at/posselt/pfrpg2e/utils/Localization.kt`

**Migration28 steps:**
1. Read current `KingdomData`
2. Add quest/event generator fields with defaults (empty lists, default settings)
3. Write back with `migrationVersion = 28`

**Handlebars helper:** register `json` helper in `Localization.kt`:
```kotlin
window.Handlebars.registerHelper("json", { obj: Any -> JSON.stringify(obj) })
```

**quest-completed.hbs changes:**
- Add reward summary section: XP, RP, Fame, Commodities earned
- Conditionally show for generated quests with `{{#if generatedByEvent}}`

**Step: Commit**

```bash
git add src/jsMain/kotlin/at/posselt/pfrpg2e/migrations/migrations/Migration28.kt \
        src/jsMain/kotlin/at/posselt/pfrpg2e/migrations/Migrations.kt \
        src/jsMain/resources/lang/en.json \
        src/jsMain/resources/chatmessages/quest-completed.hbs \
        src/jsMain/kotlin/at/posselt/pfrpg2e/utils/Localization.kt
git commit -m "feat(quest-event): add Migration28, localization keys, json helper, quest-completed template"
```

---

## Tests

### Unit tests (TDD — each task writes the failing test first)

| Test | What it covers |
|------|---------------|
| `QuestTemplateTest` | Data model construction, GM-only default (Decision 2) |
| `CampaignQuestTest` | Status transitions, field defaults, no-deadline null turnsRemaining |
| `QuestGeneratorTest` | Event→quest mapping, max quests limit, edge cases |
| `GenerateQuestDialogTest` | Dialog context generation, canGenerate logic |
| `QuestGeneratorSettingsTest` | Settings defaults, validation |

### Integration tests

| Test | What it covers |
|------|---------------|
| `EventToQuestIntegrationTest` | Full flow: create event → generate quest → verify `campaign_quest` record → reward values snapshot correctly |
| `QuestRewardApplicationIntegrationTest` | Quest completion applies XP/RP/commodities/unrest reduction to KingdomData |

### Build verification

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew compileKotlinJs compileTestKotlinJs
```

Expected: PASS. Browser tests in WSL may fail due to FirefoxHeadless — record as environment-only blocker.

---

## Manual Foundry Verification Checklist

Use this checklist after implementation to verify the feature works in Foundry:

### Event-to-Quest Generation

1. **Generate Quest from Event Browser**
   - [ ] Navigate to Kingdom Sheet → Events tab → Event Browser
   - [ ] Event entries show "Generate Quest" button
   - [ ] Click "Generate Quest" → GenerateQuestDialog opens with event pre-selected

2. **Quest Preview**
   - [ ] Dialog shows quest name, type, description, objectives, rewards
   - [ ] Quest preview card shows "Generated" badge
   - [ ] Visibility defaults to "GM Only"

3. **Generate and Commit**
   - [ ] Click "Generate Quest" → preview updates
   - [ ] Click "Add to Campaign" → quest appears in the Quests tab
   - [ ] Generated quest card shows "Generated from: [Event Name]" badge
   - [ ] Quest is NOT visible to players (GM-only by default)

4. **Reveal to Players**
   - [ ] Open a generated quest → click "Reveal to Players"
   - [ ] Quest becomes visible on the player-facing quest list

5. **Visibility Toggle**
   - [ ] Click "Hide from Players" → quest reverts to GM-only

### Generator Settings

6. **Settings Dialog**
   - [ ] Open Generator Settings from the gear icon in GenerateQuestDialog
   - [ ] Default Visibility: GM Only (default)
   - [ ] Max Active Generated Quests: 10 (default)
   - [ ] Auto-Advance Quest Timers: ON (default)
   - [ ] Changes persist across dialog reopens

7. **Max Quests Limit**
   - [ ] Set max active generated quests to 2
   - [ ] Generate 3 quests → third shows ineligibility message

### Quest Timers

8. **Turn Advancement**
   - [ ] Set a quest's "Turns Remaining" to 2
   - [ ] End turn → quest turnsRemaining decrements to 1
   - [ ] End turn again → quest turnsRemaining decrements to 0, status changes to FAILED
   - [ ] Turn log shows quest timer TickChange entries

### Quest Completion & Rewards

9. **Complete Quest**
   - [ ] Open an active generated quest → mark as COMPLETED
   - [ ] Rewards are applied: XP, RP, fame, commodities reflect KingdomData changes
   - [ ] Quest completion chat message shows reward summary

10. **Quest Failed**
    - [ ] Quest with expired timer shows FAILED status
    - [ ] No rewards applied on failure

### Edge Cases

11. **No Events**
    - [ ] With no active kingdom events → dialog shows "No eligible events" message
    - [ ] Generate button is disabled

12. **Existing Quest Flow Unchanged**
    - [ ] Add Quest button (non-generator) still works for manual quest creation
    - [ ] Non-generator quests do not show "Generated" badge
    - [ ] Existing quests are unaffected by the new feature

---

## Risks & Open Questions

### Risks

1. **Quest reward application coupling.** Rewards modify `KingdomData` directly on completion. If the quest is completed while another dialog is editing kingdom data, changes could conflict. **Mitigation:** Quest completion calls the same `ActionDispatcher` pattern as existing kingdom actions, which serializes updates. The `performEndTurn()` extraction pattern from the Kingdom Turn Assistant plan should be followed.

2. **Event data load at runtime.** `KingdomEventTemplate` data comes from `data/events/` data packs. If packs haven't loaded when the generator opens, the event list will be empty. **Mitigation:** The dialog shows a loading state and the localization key `kingdom.questGenerator.noEvents`. Data packs are loaded at module init in Foundry, so this is only a risk during very early init — but the GM won't be opening the generator before the module is ready.

3. **Quest template ↔ event trait mapping ambiguity.** Multiple event traits can suggest conflicting quest types. **Mitigation:** The generator uses a priority ordering: `military` > `supernatural` > `political` > `agriculture` > `positive`. This is a first-pass heuristic. The GM can always override in the preview dialog before committing.

4. **Generated quests vs. manual quests confusion.** GMs may lose track of which quests were auto-generated vs. manually created. **Mitigation:** The "Generated" badge and filter toggle on the quests page provides clear visual distinction. The `generatedByEvent` flag is persisted.

### Open Questions

1. **Quest templates: data pack vs. campaign-scoped?** Should quest templates be loaded from data packs (like existing events) or created per-campaign? — **Decision:** Campaign-scoped. Templates are created by the GM or seeded by the generator. When a data-driven content pack is desired, a separate import feature can be added later. The `QuestTemplate` data class already has `sourceEventTraits` which provides the mapping seam.

2. **Turn-tick quest generation: auto-create or just advance timers?** Should the turn tick auto-create new quests from active events, or only advance existing quest timers? — **Decision:** Only auto-advance timers. Quest creation is GM-triggered via the dialog. Auto-creation risks quest spam and removes GM agency. The `EventGenerationLog` table records what happened each turn for audit purposes.

3. **Should quest completion grant structure access?** The `QuestRewards` model includes a `structureAccessGranted` field for unlocking structures on completion. This depends on Feature 6 (Settlement Benefit Tracker) which isn't built yet. — **Decision:** Include the field in the data model now (it's a string? so zero cost), but the reward application logic is a no-op until Feature 6 ships. Add a localization key placeholder.

4. **Quest generation from event resolution vs. event creation.** Should quests generate when an event is first created, or when the GM resolves it? — **Decision:** When the GM clicks "Generate Quest" in the event browser (at creation time or later). The generator is reactive — it doesn't fire on event creation. This keeps the GM in control and aligns with Decision 2 (GM-only default).

---

## Decision References

| Decision | Reference | Application |
|----------|-----------|-------------|
| Decision 1 | Single Gregory profile seam | Generator reads `KingdomSettings` toggles directly; no RuleProfile object |
| Decision 2 | GM-only quests by default | `visibleToPlayers = false`; GM clicks "Reveal" | 
| Decision 3 | Hybrid strict+soft-pause clocks | Quest timers are strict (auto-fail on expiry); no soft-pause needed for quests — if GM wants more time, they edit the quest |
| Decision 5 | Structured aggregation first | Generator output is structured data; no prose generation. Future LLM-assisted flavor text can be added behind a "Generate Description" seam |
