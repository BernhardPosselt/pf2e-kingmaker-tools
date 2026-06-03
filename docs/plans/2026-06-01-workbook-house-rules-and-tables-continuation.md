# Workbook migration continuation: house rules and workbook tables

Date: 2026-06-01

## Context

The migration from `Copy of Royal Kingdom Sheet for Pathfinder 2E - CURRENT.xlsx` is only partially complete. The army catalog chunk is complete, but the next verified gaps are still missing from structured data/code:

- Companion structures from `docs/house-rules.md`: Companion Shacks, Companion Rooms, Companion Quarters.
- House-rule kingdom activities from `docs/house-rules.md`: Naval Support, Cleanse Item.
- Workbook Tables sheet data not represented in Kotlin yet: advancement, milestone XP, RP-to-XP, water adjacency.
- Larger workbook sheets still requiring reconciliation: README, Kingdom Sheet, Urban Grid Template.
- Settings/automation gaps: V&K activity/structure toggles and hex state synchronization.

## Source of truth

- House-rule structure/activity details come from `docs/house-rules.md`.
- Workbook table details come from the extracted file `/tmp/pf2e_next_workbook_tables.json` and the workbook itself.
- JSON shape must follow:
  - `src/commonMain/resources/schemas/structure.json`
  - `src/commonMain/resources/schemas/kingdom-activity.json`
- Existing localization style must follow `lang/en.json`.

## Chunk scope

This chunk intentionally stays small and data-focused:

1. Add missing Companion structure JSON data files.
2. Add missing Naval Support and Cleanse Item kingdom activity JSON data files.
3. Add English localization entries required by those new files.
4. Add deterministic validation coverage for the new migrated records.
5. Run targeted schema/build verification.

The following are deferred to later chunks because they likely require UI/state logic rather than only data migration:

- Advancement/milestone/RP-to-XP/water adjacency Kotlin data classes.
- README, Kingdom Sheet, and Urban Grid Template workbook reconciliation.
- V&K settings wiring beyond matching existing `enabled: false` V&K data-file convention.
- Hex explored/cleared/roads state synchronization.

## Implementation details

### Structures

Create these files in `data/structures/`:

- `Companion Shacks.json`
  - id: `companion-shacks`
  - level: 2
  - lots: 1
  - construction: 4 RP, 4 lumber, 4 stone, Engineering trained, DC 16
  - bonus: +1 to companion activities, represented as documented notes unless matching activity IDs exist
  - maximum: one per settlement, represented as documented notes
  - upgrades to Lodges per house rules, but only `upgradeFrom` can be represented by schema; do not invent nonexistent target structures.

- `Companion Rooms.json`
  - id: `companion-rooms`
  - level: 9
  - lots: 2
  - construction: 30 RP, 5 lumber, 5 stone, Engineering trained, DC 26
  - upgradeFrom: `companion-shacks`
  - bonus: +2 to companion activities, represented as notes unless matching activity IDs exist
  - maximum: one per settlement, represented as notes

- `Companion Quarters.json`
  - id: `companion-quarters`
  - level: 15
  - lots: 4
  - construction: 45 RP, 10 lumber, 20 stone, 8 luxuries, Engineering trained, DC 34
  - upgrade path ambiguity: source says upgrades from Lodges, but no Lodges structure exists. Do not create an invalid reference unless the loader supports missing IDs; preserve in notes.
  - bonus: +3 to companion activities, represented as notes unless matching activity IDs exist

### Kingdom activities

Create these files in `data/kingdom-activities/`:

- `Naval Support.json`
  - id: `naval-support`
  - phase: leadership
  - skill: Boating
  - DC: control
  - default enabled: true unless later V&K/house-rule toggles require otherwise
  - critical success: +2 region activities
  - success: +1 region activity
  - failure: +1 region activity and lose 2d6 RP
  - critical failure: lose 2d6 RP

- `Cleanse Item.json`
  - id: `cleanse-item`
  - phase: leadership
  - skill: Magic
  - DC: custom, because the source DC is item level + 10 rather than the standard control DC
  - special text captures counteract level, item-level DC, and luxury/structure requirements
  - critical success: curse removed and half materials are consumed
  - success: curse removed
  - failure: curse remains
  - critical failure: item destroyed

### Tests/verification

Follow a lightweight TDD/data-verification cycle:

1. Add a deterministic test or script that asserts the expected files and key migrated fields before adding the data; verify it fails because the files are absent.
2. Add the JSON/localization data.
3. Re-run the deterministic check and schema validation:
   - `JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew validateStructures validateKingdomActivities`
4. Run compile/build checks where practical:
   - `JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew compileTestKotlinJs`
   - `JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew clean assemble`
5. Do not treat browser test timeout as a code failure if FirefoxHeadless still fails in this WSL environment.

## Acceptance criteria

- New structure and activity files exist and pass JSON schema validation.
- English localization keys resolve for every new `name`, `title`, `description`, `special`, and result `msg` key used by the new files.
- The deterministic migration check confirms all expected IDs, levels, lots, costs, skills, DCs, phases, and key result text references.
- Build/compile checks pass, or any environment-only failure is clearly identified.
