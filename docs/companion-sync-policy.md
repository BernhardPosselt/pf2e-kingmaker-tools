# Companion Actor-to-Roster Sync Policy — Analysis & Recommendation

**Date:** 2026-06-01
**Decision needed:** Should edits on a companion's Foundry actor sheet propagate back to the kingdom roster?

---

## 1. Current Architecture Summary

### Data model

- `KingdomData.companions: Array<RawCharacter>?` lives on the kingdom actor (a `PF2EParty`) under the app flag `"kingdom-sheet"`.
- Each `RawCharacter` holds: `name`, `actorUuid`, `speed`, `destinationX/Y`, `eta`, `traveling`, `active`, `role`, `plotHook`, `img`.
- `RawCharacter` is a plain JS object with no behavior — it is the roster's source of truth.

### Write path (kingdom -> actor)

`KingdomActor.setKingdom()` (in `Kingdom.kt:18-29`) does two things:
1. Writes the full `KingdomData` to the kingdom actor's `"kingdom-sheet"` flag.
2. Iterates `data.companions` and, for each companion with a non-null `actorUuid`, writes the `RawCharacter` to that actor's `"companion-data"` flag via `setAppFlag("companion-data", companion)`.

This means every `setKingdom()` call overwrites the actor flag with the current roster state.

### Read path (for rendering)

`KingdomSheet._prepareContext()` (`KingdomSheet.kt:1878-1888`) builds the roster display:
```kotlin
rosterContext = (kingdom.companions ?: emptyArray()).map { character ->
    val uuid = character.actorUuid
    if (uuid != null) {
        val companionActor = game.actors.get(uuid)
        if (companionActor != null) {
            character.name = companionActor.name   // refresh name from actor
            character.img = companionActor.img     // refresh image from actor
        }
    }
    character
}.toTypedArray().toRosterContext(isGM)
```

This reads `name` and `img` from the linked Foundry actor at render time to keep the roster visually current. But it does **not** write those back to `KingdomData` — the in-memory clone is mutated, not persisted. The roster context is a derived view, not a sync mechanism.

### What the "companion-data" actor flag is used for

**Nothing reads it.** The flag is written by `setKingdom()` but there is no `getAppFlag("companion-data")` call anywhere in the codebase outside of the migration verification docs. The flag exists as a one-way push — it's dead data on the actor.

### How the kingdom sheet re-renders

The sheet listens to `onUpdateActor` via `appHook` and calls `checkUpdateActorReRenders()`, which only re-renders if the updated actor is one of the 8 leaders. Companion actor changes do **not** trigger re-renders.

### How companions are modified

All companion CRUD flows through `KingdomSheet._onClickAction`:
- **Add**: `RosterAddDialog` -> `actor.setKingdom(current)` (line 464)
- **Edit**: `RosterEditDialog` -> `actor.setKingdom(current)` (line 484)
- **Delete**: -> `actor.setKingdom(current)` (line 500)
- **Toggle traveling/active**: -> `actor.setKingdom(current)` (lines 526, 539)
- **Link actor**: -> `actor.setKingdom(current)` (line 513)

Every mutation reads the full kingdom data, modifies the `companions` array, and writes it all back via `setKingdom()`. There is no way to modify a companion from the actor sheet.

---

## 2. Option A — Keep Write-Only (No Read-Back)

### How it works today

- GM makes all companion edits on the kingdom sheet roster tab.
- `setKingdom()` pushes companion state to actor flags (unused by anything).
- If a player edits a companion actor's name, image, or other fields on the actor sheet, those changes are **not** reflected in the roster except for `name` and `img` which are read at render time only.
- The `companion-data` actor flag is silently overwritten on the next `setKingdom()` call, erasing any actor-side changes.

### Strengths

- **Zero complexity.** No sync logic, no conflict resolution, no event handlers.
- **No data loss risk.** The kingdom sheet is the single source of truth; there's nothing to conflict with.
- **Predictable.** GMs know that what they see on the roster is what the game uses.
- **No sync loops.** Since nothing reads back, there's no possibility of A -> B -> A cycles.

### Limitations

- Players cannot manage their companion details (plot hooks, speed, travel state) from the actor sheet — they must use the kingdom sheet.
- The `companion-data` actor flag is wasted writes — it consumes bandwidth and storage with no consumer.
- If a GM renames a companion on the actor sheet, the name change is visible in the roster (read at render) but **not persisted** to `KingdomData`. This creates a confusing situation where the name appears correct in the UI but the underlying data hasn't changed.
- Actor sheet companion management UX is impossible without this feature.

### Maintenance burden

**None.** This is the current state and it works.

---

## 3. Option B — Add Read-Back With Conflict Policy

### What it would require

**Read-back mechanism:**

Two broad approaches:

1. **Polling at render time** (minimal change): Extend the existing `_prepareContext()` name/img read to persist all `RawCharacter` fields back into `KingdomData` when they differ. This is the simplest approach but means changes are only detected when the kingdom sheet renders, and it mixes read and write concerns in the rendering path.

2. **Reactive sync via hooks** (cleaner): Register an `onUpdateActor` hook (similar to the existing `checkUpdateActorReRenders()` for leaders) that detects when a linked companion actor changes and updates `KingdomData.companions` accordingly. This requires:
   - A new function like `KingdomData.hasCompanionUuid(uuid: String)` (parallel to `hasLeaderUuid`).
   - In the hook: read `getAppFlag<Actor, RawCharacter?>("companion-data")` from the updated actor, diff it against the current roster entry, apply the conflict policy, and call `setKingdom()`.
   - Careful handling to avoid re-render loops (the hook triggers `setKingdom()` which triggers `onUpdateActor` on the kingdom actor, not the companion actor — so no loop from that, but the kingdom actor update could re-trigger the hook if not filtered).

**Conflict resolution policy options:**

| Policy | Behavior | Complexity | Risk |
|--------|----------|------------|------|
| **Kingdom-wins** (last `setKingdom()` wins) | Ignore actor changes; next kingdom save overwrites them | Trivial | Actor edits are silently lost — confusing for users |
| **Actor-wins** | Actor changes always propagate to roster | Low | GM could lose roster-only state (traveling, active, destination) if the actor flag doesn't carry those fields |
| **Last-write-wins** | Compare timestamps, newer wins | Medium | Requires timestamp tracking on both sides; clock skew in multiplayer |
| **Field-level merge** | Some fields are kingdom-owned (traveling, active, destination), some are actor-owned (name, img, plotHook) | Medium-High | Cleanest UX but requires field ownership metadata and careful merge logic |

**Recommended conflict policy: Field-level merge with kingdom-ownership for roster state.**

Rationale:
- `name`, `img`, `plotHook` → actor-owned (players should be able to edit these on the actor sheet)
- `traveling`, `active`, `destinationX/Y`, `eta`, `speed`, `role` → kingdom-owned (these are roster/GM management fields)
- On actor update: read `companion-data` flag, update only actor-owned fields in `KingdomData.companions`, call `setKingdom()`.

**Implementation sketch:**

Key files to modify:
- `Kingdom.kt` — add `KingdomActor.syncCompanionFromActor(companionUuid: String)` that reads the actor flag and merges into `KingdomData`.
- `KingdomData.kt` — add `hasCompanionUuid(uuid: String)` helper.
- `KingdomSheet.kt` — extend `checkUpdateActorReRenders()` or add a companion-specific hook that calls `syncCompanionFromActor`.
- Optionally: remove the `companion-data` write from `setKingdom()` if it's no longer needed as a push mechanism (or keep it for backward compat).

Estimated complexity: **Medium**. ~100-200 lines of Kotlin across 3 files. The main risk is not the code volume but getting the hook filtering right to prevent loops.

### Risks

1. **Sync loops**: If `setKingdom()` triggers an `onUpdateActor` on the kingdom actor, and the hook doesn't filter by actor type, it could re-read companion data unnecessarily. Mitigation: filter by `actor is PF2ECharacter || actor is PF2ENpc` in the hook.

2. **Data loss on concurrent edits**: If a GM edits a companion on the kingdom sheet while a player edits the same companion on the actor sheet, the last `setKingdom()` call wins. With field-level merge this is mitigated but not eliminated for shared fields.

3. **Stale `companion-data` flag**: The actor flag currently only gets updated by `setKingdom()`. If we start reading from it, we need to ensure it's always in sync with the actor's actual state. Currently it's a partial copy of `RawCharacter` — it doesn't include fields that the actor itself owns (like the actor's `system` data). This means the flag is an incomplete sync vehicle.

4. **Multiplayer race conditions**: Foundry's `onUpdateActor` fires for all connected GMs/players. Two GMs editing the same companion simultaneously could produce unpredictable results. This is a general Foundry problem, not specific to this feature, but adding sync makes it more visible.

5. **Scope creep**: Once actor sheet edits propagate, users will expect the reverse too (roster edits -> actor sheet). The current `setKingdom()` push to `companion-data` flag doesn't update the actor's actual `name` or `img` — it only sets a custom flag. Full bidirectional sync would require updating the actor document itself.

### Maintenance burden

**Medium.** Adds a reactive data flow that must be kept in sync with the `RawCharacter` schema. Every new field added to `RawCharacter` needs an ownership decision (kingdom vs actor). Hook filtering must be maintained as actor types evolve.

---

## 4. Comparison Matrix

| Dimension | Option A (Write-Only) | Option B (Read-Back + Merge) |
|-----------|----------------------|------------------------------|
| **Complexity** | None — current state | Medium — ~150 LOC, 3 files, new hook |
| **Risk** | None | Low-Medium — sync loops, race conditions, data loss |
| **User Experience** | GMs must use kingdom sheet for everything | Players can edit name/img/plotHook on actor sheet |
| **Maintenance** | Zero | Medium — field ownership decisions, hook upkeep |
| **Data integrity** | High — single source of truth | Good with field-level merge, but adds edge cases |
| **Implementation effort** | N/A | 2-4 hours of development + testing |

---

## 5. Recommendation

**Stick with Option A (write-only) for now.**

Justification:

1. **The current system works.** GMs manage companions from the kingdom sheet, which is the natural place for roster management. The `name`/`img` read at render time already provides visual freshness without persistence complexity.

2. **The `companion-data` actor flag is dead code.** It's written but never read. Rather than building a read-back path on top of it, consider removing the write in `setKingdom()` to eliminate the wasted work. This is a net simplification.

3. **Option B's complexity is not justified by the use case.** The primary benefit is letting players edit `name`, `img`, and `plotHook` on the actor sheet. But these are low-frequency edits that can be done on the kingdom sheet with minimal friction. The cost (sync logic, conflict resolution, hook maintenance, edge cases) outweighs the convenience.

4. **If actor-sheet companion management is desired in the future**, the right approach is not to build a read-back path from the `companion-data` flag (which is an incomplete data vehicle), but to either:
   - Build a dedicated companion actor sheet that reads/writes `KingdomData` directly via the kingdom actor's UUID.
   - Or extend the kingdom sheet with a "companion details" panel that feels like an actor sheet but writes to the roster directly.

5. **The field-level merge policy is the right design if Option B is ever implemented**, but it should be deferred until there's concrete user demand.

### Suggested immediate action

Remove the companion `setAppFlag("companion-data", ...)` loop from `KingdomActor.setKingdom()`. It writes data that nothing reads, and keeping it creates a false impression that actor-side companion state is being maintained. This is a 4-line deletion in `Kingdom.kt:20-28`.

---

## 6. If Option B Is Chosen Later

Implementation sketch for field-level merge:

```kotlin
// KingdomData.kt
fun KingdomData.hasCompanionUuid(uuid: String) =
    companions?.any { it.actorUuid == uuid } == true

fun KingdomData.updateCompanionFromActor(companionUuid: String, actor: Actor): KingdomData {
    val companions = (companions ?: emptyArray()).copyOf()
    val index = companions.indexOfFirst { it.actorUuid == companionUuid }
    if (index == -1) return this
    companions[index] = companions[index].also {
        it.name = actor.name
        it.img = actor.img
        // Only update actor-owned fields
    }
    return copy(companions = companions)
}

// KingdomSheet.kt — in init block, alongside the leader hook:
appHook.onUpdateActor { actor, _, _, _ ->
    val kingdom = getKingdom()
    if (kingdom.hasCompanionUuid(actor.uuid)) {
        actor.getAppFlag<Actor, RawCharacter?>("companion-data")?.let { flagData ->
            // Merge actor-owned fields from flagData into kingdom
            actor.setKingdom(kingdom.updateCompanionFromActor(actor.uuid, actor))
        }
    }
}
```

Key files:
- `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/Kingdom.kt` — add `updateCompanionFromActor()`
- `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/KingdomData.kt` — add `hasCompanionUuid()`
- `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/KingdomSheet.kt` — add companion `onUpdateActor` hook
