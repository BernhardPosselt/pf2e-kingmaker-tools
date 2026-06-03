# Implementation Plan: Travel & Route Planner

## Overview
This plan outlines the implementation of the "Travel & route planner" feature, which allows GMs and players to calculate travel time, route costs, and arrival estimates based on current party state and map conditions (roads, rivers, terrain, weather).

**Target File**: `docs/plans/travel-route-planner.md`
**Status**: Draft

## 1. Affected Files
### Kotlin Source
- `src/jsMain/kotlin/at/posselt/pfrpg2e/camping/`: New service for route calculation and new travel models (e.g., `TravelRoute`, `RouteCost`).
- `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/`: Integration with kingdom/hex state to pull terrain and infrastructure data.
- `src/jsMain/kotlin/at/posselt/pfrpg2e/settings/Pfrpg2eKingdomCampingWeatherSettings.kt`: Ensure weather modifiers are pulled into cost calculation.

### Templates (Handlebars)
- `src/jsMain/resources/applications/camping/camping-sheet.hbs`: Add a new UI panel or button to launch the Travel Planner.
- `src/jsMain/resources/applications/camping/camping-tile.hbs` (if applicable): Display travel cost overlays on hex tiles.

### Testing
- `src/commonTest/kotlin/at/posselt/pfrpg2e/camping/TravelRouteTest.kt`: New unit tests for the route cost algorithm.
- Existing camping tests: Update to ensure new travel features don't regress existing camping logic.

## 2. Data Models
### `TravelRoute` (New)
- `startHex: HexCoordinate`
- `endHex: HexCoordinate`
- `path: List<HexCoordinate>`
- `totalCost: Double`
- `estimatedDurationSeconds: Long`

### `TravelPlan` (New)
- `partySpeedMultiplier: Double` (derived from the slowest traveler in the roster)
- `terrainModifiers: Map<TerrainType, Double>`
- `infrastructureModifiers: Map<InfrastructureType, Double>` (roads, bridges)
- `weatherModifier: Double`

## 3. Migrations
No database migrations are required for the initial implementation as all travel calculations will be performed in-memory based on existing hex and actor data. If persistent "Travel History" is required later, a migration for `CampingActor` or `KingdomState` may be needed.

## 4. UI/Template Changes
### Camping Sheet Integration
- **Panel**: A new "Route Planner" tab or collapsible section in the camping sheet.
- **Inputs**: Selection of Start Hex and End Hex (using existing hex selection tools).
- **Output**: A visual list or summary showing:
    - Total Distance (in hexes/kilometers).
    - Estimated Arrival Time (ETA) based on current turn time.
    - Breakdown of cost modifiers (e.                roads, rivers, terrain, weather).

## 5. Tests
### Unit Tests
- **Cost Algorithm**: Verify that a route through a forest costs more than a road.
- **River Logic**: Verify that crossing a river without a bridge applies the correct penalty.
- **Party Speed**: Verify that adding a slow character (e.g., heavy armor) to the active roster decreases the overall travel speed.
- **Weather Impact**: Ensure heavy rain or snow increases the travel cost according to `Pfrpg2eKingdomCampingWeatherSettings`.

### Integration Tests
- Validate that selecting a route correctly updates the "Arrival" field in the camping session if integrated with the timer.

## 6. Manual Foundry Verification Checklist
1. **Setup**: Open a running Foundry instance with the `pf2e-kingmaker-tools` module loaded.
2. **Navigation**: Navigate to the Camping Sheet for an active party/actor.
3. **Route Calculation**:
    - Select two hexes connected by a road. Note the estimated time.
    - Temporarily "remove" the road (via terrain change) and verify the time increases.
    - Check a path with a river crossing where no bridge exists; verify the penalty is applied.
4. **Party Dynamics**: 
    - Add a character with a low movement speed to the roster. Verify that the travel ETA for all active routes updates immediately.
5. **Weather**: Change the global weather setting (e.g., to heavy rain) and ensure the travel cost calculation reflects this change.
