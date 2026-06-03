# Implementation Plan: Settlement Benefit & Access Tracker

## Overview
This plan outlines the implementation of a system to make settlements and their structures more meaningful for players (PCs). Instead of just providing kingdom bonuses, certain buildings will now unlock specific player-facing benefits like character retraining, specialized crafting, and upgraded item purchase availability.

**Feature ID:** #6 in `feature-roadmap.md`
**Status:** Plan Only

## 1. Affected Files
### Kotlin Source (Data Models & Logic)
- `src/commonMain/kotlin/at/posselt/pfrpg2e/data/kingdom/structures/Structure.kt`: To add capability for structures to define PC benefits (trainers, crafting).
- `src/commonMain/kotlin/at/posselt/pfrpg2e/data/kingdom/settlements/SettlementType.kt`: To ensure settlement types correctly drive the item purchase level logic.
- `src/commonMain/kotlin/at/posselt/pfrpg2e/data/kingdom/settlements/Settlement.kt`: To implement the aggregation logic that scans a settlement's structures for active benefits.
- `src/commonMain/kotlin/at/posselt/pfrpg2e/data/kingdom/structures/AvailableItems.kt`: (If necessary) to link item availability to structural presence.

### Templates (UI)
- `src/jsMain/resources/applications/kingdom/sections/settlements/*.hbs`: To add the "Settlement Benefits" or "Unlocked Access" UI component, listing available trainers and crafting types.

### Data Files
- `data/kingdom/...` (Relevant JSON files): To update structure definitions with new benefit metadata if using externalized data loading.

## rag_ref: docs/house-rules.md

## 2. Data Models
### New/Modified Entities
- **Structure Benefit**: A new concept or property within `Structure` that defines what a building grants to PCs.
    - `type`: Enum (e.g., `TRAINER`, `CRAFTER`, `MERCHANT`)
    - `identifier`: String (e.g., `"Investigator"`, `"Metallic Items"`, `"Magic Item Level 5"`)
- **Settlement Benefit Aggregator**: A logic layer that iterates through all structures in a settlement and collects unique benefits into a set for the UI to render.

## 3. Migrations
- **Schema Update**: If adding properties to `Structure` class, ensure any JSON/data-loading mechanism is updated to handle the new fields without breaking existing structure definitions.
- **Data Backfill**: No data backfill required as benefits are derived from existing structures.

## 4. UI/Template Changes
- **Settlement View Component**: A new section titled "Unlocked Access" or "Settlement Benefits".
    - **Trainers Sub-section**: List of classes available for retraining at this settlement (e.g., "Library: Investigator, Thaumaturge").
    - **Crafting Sub-section**: List of crafting types enabled here (e.g., "Smithy: Metallic items").
    - **Shopping Sub-section**: A clear indicator of the current item purchase level based on the settlement type (Town/City/Metropolis).

## 5. Tests
### Unit Tests
- `StructureBenefitTest`: Verify that a structure with `TRAINER` benefit correctly registers its class list.
- `SettlementBenefitAggregationTest`: Verify that if a Settlement has both a 'Library' and an 'Alchemy Lab', the aggregated benefits include all associated classes/crafting types.
- `ItemPurchaseLevelTest`: Ensure item purchase level matches the rules for the current settlement type (Town=3, City=9, Metropolis=15).

### Integration Tests
- Test that adding a structure to a Settlement in the data model correctly triggers an update to the UI component in the simulated Kingdom Sheet.

## 6. Manual Foundry Verification Checklist
- [ ] **Scenario: Basic Town**
    - Create a 'Town' level settlement with no structures.
    - Verify "Unlocked Access" is empty or shows only base town items.
- [ ] **Scenario: Trainer Unlocking**
    - Add a 'Library' structure to a settlement.
    - Verify that "Investigator, Thaumaturge, Psychic" appear in the trainer list on the UI.
- [ ] **Scenario: Crafting Access**
    - Add an 'Alchemy Laboratory' structure to a settlement.
    - Verify that "Alchemical items" appears under the crafting section.
- [ ] **Scenario: Item Purchase Level**
    - Upgrade a settlement from 'Town' to 'City'.
    - Verify the item purchase level limit has increased (e.g., from 3 to 9) in the UI display.
- [ ] **Scenario: Homebrew Toggle (Future/Optional)**
    - If implemented, verify that turning off "Non-capital upgrades" hides benefits for all settlements except the capital.

## 7. Design Decisions Reference
- Follows decisions from `docs/house-rules.md` regarding Trainer and Crafting structure mappings.
- Integrates with existing `SettlementType` hierarchy (Town, City, Metropolis).
