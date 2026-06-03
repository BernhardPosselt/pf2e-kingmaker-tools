# Plan: Camping Encounter Resolver

## Overview
This plan outlines the implementation of the "Camping Encounter Resolver" feature (Feature #8 in `docs/feature-roadmap.md`). The goal is to automate the resolution of encounters that occur during a camping watch, providing a structured outcome (Perception vs Stealth) while maintaining GM control through an interactive dialog and informative chat cards.

## Objectives
- Automate the encounter flow for a single watch period.
- Provide a standardized calculation of "degree of success" based on Perception vs Stealth rolls.
- Output clear results to the GM via Foundry Chat Cards (distance, conditions applied, etc.).
- Integrate with existing camping infrastructure (`ConfirmWatchApplication.kt`, `RandomEncounters.kt`).

## Affected Files
### Code
- `src/jsMain/kotlin/at/posselt/pfrpg2e/camping/ConfirmWatchApplication.kt`: Logic for triggering the encounter resolution at the end of a watch.
- `src/jsMain/kotlin/at/posselt/pfrpg2e/camping/RandomEncounters.kt`: Integration with existing random encounter generation to apply the new resolution logic.
- `src/jsMain/kotlin/at/posselt/pfrpg2e/camping/CampingData.kt` (or similar): Storage for recent encounter results if needed for history tracking.
- New file: `src/jsMain/kotlin/at/the_new_package/EncounterResolverEngine.kt`: The core logic engine for calculating outcomes from rolls.

### Templates & UI
- `src/jsMain/resources/templates/camping/encounter_card.hbs`: Handlebars template for the GM encounter summary chat card.
- `src/jsMain/kotlin/at/posselt/pfrpg2e/ui/components/EncounterResolutionDialog.kt` (New): UI component for the GM to input ambush parameters and confirm outcomes.

### Data
- New JSON schema/data files for encounter outcome templates if necessary.

## Data Models
### `EncounterResolutionResult`
A structured object representing the calculated outcome:
- `attackerStealthDc: Int`: The Stealth DC used for the comparison.
- `watcherPerceptionRoll: Int`: The value of the watch roll.
/ `distanceToEnemy: Float`: Calculated distance (e.g., 10ft, 30ft, or 'Immediate').
- `appliedConditions: Array<String>`: List of conditions applied to tokens (e.g., "Prone", "Unconscious").
- `ambusherState: String`: The state the ambushers are in (e.g., "Hidden", "Revealed").
- `gmNotes: String?`: Optional text for the GM summary.

## Implementation Steps

### 1. Core Resolution Engine (`EncounterResolverEngine`)
- Implement a function `resolve(watcherRoll, stealthDc): EncounterResolutionResult`.
- **Logic Rules**:
    - **Critical Success (Watcher)**: Enemy is startled; distance increases; no ambush penalty to party.
    - **Success (Watcher)**: Enemy detected but still present; standard encounter start.
    - **Failure (Watcher)**: Ambush succeeds; enemy starts closer or with advantage; tokens may be marked "Prone" or "Unconscious".
    - **Critical Failure (Watcher)**: Severe ambush; party takes immediate damage/conditions.

### 2. Integration with `ConfirmWatchApplication`
- When a watch period ends, if an encounter is triggered:
  1. Invoke the `EncounterResolutionDialog`.
  2. On "Apply", execute the engine and update the campaign state.
  3. Generate the Foundry Chat Card.

### 3. UI & Presentation
- **Input Dialog**: A simple modal for the GM to confirm/input:
    - Stealth DC (if not pre-set).
    - Any special modifiers (e.g., "Armor comfort" influence).
- **Chat Card**: A rich, readable card in Foundry containing:
    - Summary of the roll result.
    - List of affected PCs/Tokens.
    - New encounter details (type, distance).

### 4. Automation (Optional/Advanced)
- If possible, use Foundry's API via Kotlin JS to automatically apply `Prone` or `Unconscious` conditions to tokens identified in the encounter if they were sleeping.

## Testing Strategy
### Unit Tests
- Test `EncounterResolverEngine` with a matrix of roll outcomes (Crit Success through Crit Failure).
- Verify correct calculation of `distanceToEnemy` and `appliedConditions`.

### Integration Tests
- Mock `ConfirmWatchApplication` flow to ensure the dialog triggers and results are passed correctly.
- Verify that chat card templates render all required fields.

## Foundry Verification Checklist (for Gregory)
- [ ] Trigger a watch end with an encounter.
- [ ] Confirm the Encounter Resolution Dialog appears and accepts input.
- [ ] Check that the resulting Chat Card is sent to the GM and contains accurate info.
- [ ] Verify that token conditions (e.g., "Prone") are correctly updated in the Foundry scene if using automation.
- [ ] Ensure no breaking changes to existing `RandomEncounters.kt` logic for non-camping encounters.
