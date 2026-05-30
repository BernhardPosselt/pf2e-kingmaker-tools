# Migration Tasks Checklist

- [x] Implement data models & migrations
  - [x] Implement `RawCharacter.kt` schema flags for companions
  - [x] Update `KingdomData.kt` and `Defaults.kt` properties
  - [x] Create database migration `Migration24.kt` and register in `Migrations.kt`
- [x] Implement Settlements comparison matrix
  - [x] Update `SettlementsContext.kt` to include population, maxItemBonus, influence, and specific item group levels
  - [x] Update `toContext()` calculation logic in `SettlementsContext.kt`
  - [x] Update `KingdomSheet.kt` to pass chosen feats list to `toContext()`
  - [x] Update `sections/settlements/page.hbs` to include a Detailed Matrix side-by-side view
- [x] Implement Roster VTT tab & companion syncing
  - [x] Sync companions with native character actor documents
  - [x] Create `roster.hbs` sheet tab
- [x] Implement turn-based simulation engine ticks
- [x] Synchronize custom hex states with Foundry VTT scene drawings
- [x] Run full build & verify compilation
