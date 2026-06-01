# Kingmaker Campaign Automation Feature Roadmap

Created: 2026-05-31
Repo: `/home/grego/code/pf2e-kingmaker-tools`

## Goal

Capture suggested future features for automating more of a Kingmaker campaign, including homebrew support, before any implementation starts.

## Planning rule

No feature in this document should be implemented directly from the roadmap.

Before implementation:
1. Pick exactly one feature.
2. Create a dedicated plan in `docs/plans/`.
3. Include affected files, data models, migrations, UI changes, tests, and manual Foundry verification.
4. Review the plan with Gregory.
5. Only then create implementation tasks or Kanban cards.

## Current foundation observed

The repo already has useful building blocks:

- Camping system: `src/jsMain/kotlin/at/posselt/pfrpg2e/camping/`
- Camping data: `data/camping-activities/`, `data/recipes/`
- Kingdom sheet: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/`
- Kingdom quests: `RawQuest.kt`, `sections/quests/page.hbs`
- Turn ticking engine: `TurnTickingEngine.kt`
- Roster/native actor work: `RawCharacter.kt`, `RosterPanel.kt`
- Hex grid sync: `kingdom/map/HexGridSync.kt`
- Events/data packs: `data/events/`, `packs/kingmaker-tools-*`
- House rules doc: `docs/house-rules.md`

## Recommended feature backlog

### 1. Campaign timeline and pressure-clock dashboard

Purpose: track chapter deadlines, escalating threats, and time pressure so travel/kingdom turns matter.

Examples:
- Stag Lord deadline.
- Troll sightings until Hargulka is handled.
- Season of Bloom daily cult-event pressure.
- Varnhold Vanishing rescue timer.
- Blood for Blood and War of the River Kings army pressure.

Why it fits:
- Builds directly on `TurnTickingEngine.kt`.
- Supports the house-rule goal of making kingdom management feel less like a spreadsheet.
- Gives the GM a visible reason to advance turns and enforce consequences.

Plan requirements:
- New data model for campaign clocks/deadlines.
- Turn tick integration.
- Kingdom sheet dashboard panel.
- Chat/journal output when clocks advance or trigger.
- Tests for clock ticking, expiration, pause/resume, and completed threats.

### 2. Quest/event generator tied to kingdom events

Purpose: turn kingdom events into actionable quests, complications, and rewards.

Examples:
- Crop Failure becomes a druid conflict quest.
- Political scandal from an enemy faction appears after Infiltrate.
- Merchant/trainer/refuge rumors become hex hooks.
- Event completion grants XP, RP, commodities, unrest reduction, or structure access.

Why it fits:
- Existing quest model already supports type, target, rewards, and completed flavor text.
- `data/events/` and `event-browser.hbs` already exist.
- Helps automate GM prep while preserving human control.

Plan requirements:
- Extend `RawQuest` with source event, urgency, due date, visibility, and consequence fields.
- Add event-to-quest action in event browser.
- Add quest completion consequence application.
- Add GM-only/generated quest distinction.
- Tests for reward application and status transitions.

### 3. Hex content and discovery manager

Purpose: track what each hex contains, what players know, what is hidden, and what changes after claiming or clearing it.

Examples:
- Landmarks, refuges, worksites, resources, ruins, merchants, trainers, enemy armies.
- Hidden vs discovered vs cleared states.
- Claimed hexes suppress random combat encounters.
- Roads/bridges/settlements change travel cost.
- GM-only notes and player-facing discovered text.

Why it fits:
- Kingmaker is exploration-heavy.
- House rules already call for more rewarding hex content.
- Existing hex grid sync can become the visual layer for discovery state.

Plan requirements:
- Hex content schema with GM/player visibility split.
- Scene drawing or tile metadata sync.
- Quest/event hooks from hex discoveries.
- Random encounter filtering by claimed/cleared state.
- Tests for visibility, discovery transitions, and travel modifiers.

### 4. Travel and route planner

Purpose: calculate travel time, route costs, and arrival estimates from current party state and map conditions.

Examples:
- Horses speed up travel.
- Rivers without bridges increase travel cost.
- Roads, bridges, settlements, terrain, weather, and forced march affect ETA.
- Party speed uses slowest relevant traveler.
- Travel plan can reserve camp stops and watches.

Why it fits:
- Camping data already tracks travel/hexploration seconds, travel mode, forced march, minimum speed, and hex size.
- Roster/actor integration can provide party speed and availability.

Plan requirements:
- Route-cost function independent of Foundry UI.
- UI for selected start/end hexes and route preview.
- Integration with camp/travel timers.
- Tests for roads, rivers, terrain, mounts, forced march, and weather modifiers.

### 5. Kingdom turn assistant

Purpose: guide the GM and players through a kingdom turn with fewer missed steps.

Examples:
- Pre-turn checklist.
- Current RP/commodities/storage/consumption preview.
- Leadership/civic/region activity caps from RAW or homebrew profile.
- Suggested pressure events when unrest/ruin is too low.
- End-turn diff summary from `TurnTickingEngine`.

Why it fits:
- The turn ticking engine already produces change records.
- House rules emphasize adding pressure and limiting action count to reduce analysis paralysis.

Plan requirements:
- Turn wizard state model.
- Activity cap rules by mode/profile.
- End-turn preview before applying changes.
- Chat/journal summary after turn completion.
- Tests for turn diff, limits, and homebrew toggles.

### 6. Settlement benefit and access tracker

Purpose: make structures matter to PCs beyond kingdom bonuses.

Examples:
- Trainers unlocked by buildings.
- Crafting access unlocked by structures.
- Item purchase levels by settlement type and structures.
- Special artisans offering limited magic items.
- Settlement upgrades using the homebrew Improve Settlement activity.

Why it fits:
- `docs/house-rules.md` already lists trainer/crafting structure mappings.
- Existing settlement matrix tracks item levels and settlement stats.

Plan requirements:
- Data schema for structure-granted PC benefits.
- Settlement view showing unlocked trainers/crafting/item access.
- Optional homebrew toggle for non-capital settlement upgrades.
- Tests for access calculation and display context.

### 7. Companion relationship and personal quest manager

Purpose: manage companion influence, camp availability, learning activities, and personal quest hooks.

Examples:
- Companion influence/discovery status.
- One influence/discover attempt per camp session.
- Companion-specific camp activities greyed out when absent.
- Personal quest triggers and rewards.
- Missing custom quests for companions can be filled with homebrew entries.

Why it fits:
- Current TODO already includes companion-learning chart integration.
- Native actor roster work gives companions a durable home.

Plan requirements:
- Companion profile schema linked to actor UUID.
- Camp availability state.
- Activity gating integration.
- Quest trigger model.
- Tests for absent/present NPC activity gating and per-session limits.

### 8. Camping encounter resolver

Purpose: automate the watch encounter flow without removing GM control.

Examples:
- One encounter per watch.
- Watcher rolls Perception vs ambusher Stealth DC.
- Result determines enemy start distance, sleeping/prone/unconscious state, reactions, and wake-up checks.
- Armor comfort trait handling.
- Chat card summary for the GM.

Why it fits:
- `ConfirmWatchApplication.kt`, `RandomEncounters.kt`, and camping watch settings already exist.
- House rules define a clear encounter resolution table.

Plan requirements:
- Encounter resolution model independent of UI.
- Watcher/ambusher input dialog.
- Optional token condition automation.
- Chat output with editable GM decisions.
- Tests for degree-of-success outcomes.

### 9. Homebrew rules profile system

Purpose: let the GM switch between RAW, Vance & Kerenshara-style, and Gregory/custom rules without code edits.

Examples:
- Kingdom XP adjustments.
- Ruin threshold 5 instead of 10.
- Leadership activity count caps.
- No random combat in claimed hexes.
- Settlement upgrade rules.
- Camping activity count equals PC count.

Why it fits:
- The module already has a lot of settings and data-driven content.
- Homebrew rules are a core part of the campaign direction.

Plan requirements:
- Versioned JSON profile schema.
- Settings UI for active profile.
- Import/export profile action.
- Rule resolution helper used by camping/kingdom/hex systems.
- Tests for RAW vs homebrew profile behavior.

### 10. Session prep and recap dashboard

Purpose: produce a GM-facing plan before play and a player-facing recap after play.

Examples:
- Open quests.
- Active clocks/deadlines.
- Unresolved kingdom events.
- Nearby hex hooks.
- Companion moments due.
- Suggested next session outline.
- End-session recap to journal.

Why it fits:
- The user wants to be present with a plan before new features and campaign sessions.
- Existing quests, notes, events, roster, and hex data can feed one dashboard.

Plan requirements:
- Read-only aggregation context first.
- GM-only dashboard UI.
- Journal export action.
- Optional player-safe recap filter.
- Tests for visibility filtering and generated summary data.

### 11. Random encounter and rumor curator

Purpose: improve random encounters so they support campaign pacing instead of just adding combat.

Examples:
- 50/50 combat vs RP encounter weighting.
- Region/level/hex-state filters.
- Rumors that reveal locations or foreshadow threats.
- Merchants with limited special stock.
- Disease, weather, faction, or lore encounters.

Why it fits:
- House rules recommend fewer random combat encounters and more RP/lore encounters.
- Existing rolltable support and random encounter code can be extended.

Plan requirements:
- Encounter category schema.
- GM preview before applying.
- Claimed/cleared hex filtering.
- Rumor-to-quest/hex hook support.
- Tests for weighting and filters.

### 12. Army and war pressure board

Purpose: track army threats, invasions, and chapter war pressure in one place.

Examples:
- Enemy army status and ETA.
- Threat clocks that increase unrest or consume commodities.
- Army attacks until a chapter objective is resolved.
- Links between warfare, quests, and kingdom events.

Why it fits:
- Repo has `kingdom/armies/` and army browser templates.
- House rules call for more army pressure in multiple chapters.

Plan requirements:
- War threat data model.
- Integration with campaign clocks.
- Army browser/dashboard additions.
- Turn tick consequences.
- Tests for ETA/clocks/consequence application.

### 13. Balance and pacing alerts

Purpose: warn the GM when the campaign is drifting away from the intended pressure curve.

Examples:
- Kingdom level too low/high for chapter.
- Too much or too little unrest/ruin.
- Too much loot/item access for party level.
- Too few claimed hexes/worksites for kingdom level.
- Too many turns without a pressure event.

Why it fits:
- House rules explicitly discuss XP curve, loot imbalance, unrest/ruin tension, and worksites.
- This is advisory only, so it helps without forcing automation.

Plan requirements:
- Pacing metrics model.
- Configurable thresholds by chapter/profile.
- Dashboard alert panel.
- Tests for threshold calculations.

## Suggested first plans to write

1. `docs/plans/campaign-timeline-pressure-clocks.md`
2. `docs/plans/hex-content-discovery-manager.md`
3. `docs/plans/kingdom-turn-assistant.md`
4. `docs/plans/homebrew-rules-profile-system.md`
5. `docs/plans/session-prep-recap-dashboard.md`

These five create the strongest automation base without immediately overfitting to one rule subsystem.

## Proposed implementation order

1. Homebrew rules profile system.
2. Campaign timeline and pressure clocks.
3. Hex content and discovery manager.
4. Kingdom turn assistant.
5. Session prep and recap dashboard.
6. Quest/event generator.
7. Travel and route planner.
8. Settlement benefit/access tracker.
9. Companion relationship manager.
10. Camping encounter resolver.
11. Random encounter and rumor curator.
12. Army and war pressure board.
13. Balance and pacing alerts.

Reasoning: start with rule configuration and shared state, then build dashboards and automations on top of stable data.

## Open decisions for Gregory

- Should homebrew support start as one Gregory profile or a general multi-profile system?
- Should generated quests be GM-only by default?
- Should campaign clocks be strict automation or advisory warnings first?
- Should hex content live on scene drawings, module flags, or both?
- Should session prep generate prose automatically or just aggregate structured data first?
