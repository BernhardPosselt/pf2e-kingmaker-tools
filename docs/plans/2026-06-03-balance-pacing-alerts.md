# Balance & Pacing Alerts Implementation Plan

> **Status:** Draft — pending Gregory review
> **Date:** 2026-06-03
> **Roadmap item:** #13 (Balance and pacing alerts)
> **Design decisions:** Decision 1 (Single Gregory profile), Decision 3 (Strict automation with soft-pause alignment for engine logic)

---

## Executive Summary

This plan describes the implementation of the **Balance & Pacing Alerts** for the pf2e-kingmaker-tools Foundry VTT module. The feature acts as an advisory system to warn the Game Master when the campaign state is drifting away from the intended "pressure curve" (e.g., kingdom level is too high for current threats, or there has been too much peace/too little unrest for too long).

**Key capabilities:**

- **Advisory Warning System:** Generates non-intrusive alerts in the Kingdom Sheet and via chat messages to notify the GM of imbalances.
- **Kingdom Level Alerts:** Warns if the current Kingdom Level is significantly mismatched with the active chapter/threat level (e./g., too high or too low).
- **Tension Monitoring:** Detects periods of "stagnation" where unrest and ruin have not changed for many turns, or where tension is dangerously low/high for the current phase.
- **Resource Imbalance Detection:** Alerts when players have excessive item/loot access (via settlements) relative to their level, or when worksite/claim density is too low for the kingdom's power.
- **Turn Gap Detection:** Warns if a significant number of turns have passed without any campaign-altering events (threat arrivals, quest completions, etc.).
- **Configurable Thresholds:** All thresholds are tied to the "Gregory" profile settings, allowing the GM to tune what constitutes an "alert."

**Relationship to architecture:** The system is purely advisory. It does not change game state; it only observes `KingdomData` and generates transient `RawPacingAlert` objects during the turn tick via `TurnTickingEngine`. All alerts are stored in a history array within `KingdomData`.

---

## Affected Files

### New Kotlin Files (3 files)

| File | Purpose |
|------|----------|
| `src/jsMain/kotlin/.../kingdom/data/RawPacingAlert.kt` | `@JsPlainObject` interface for an individual alert event |
| `src/jsMain/kotlin/.../kingdom/data/PacingAlertStatus.kt` | Enum: `WARNING`, `CRITICAL` |
| `src/jsMain/kotlin/.../kingdom/sheet/contexts/PacingAlertContext.kt` | UI context data class for the alerts panel |

### New Handlebars Templates (2 files)

| File | Purpose |
|------|----------|
| `src/jsMain/resources/applications/kingdom/sections/pacing-alerts/alert-card.hbs` | Reusable alert card component with severity-based styling |
| `src/jsMain/resources/chatmessages/pacing-alert.hbs` | Chat message template for "Alert Triggered" notifications |

### Modified Kotlin Files (5 files)

| File | Changes |
|------|---------|
| `kingdom/KingdomData.kt` | Add `pacingAlerts: Array<RawPacingAlert>?` to track alert history |
| `kingdom/KingdomSettings.kt` | Add thresholds for Level, Unrest, Ruin, and Turn Gap (e.g., `pacingAlertMinUnrestDelta`, etc.) |
| `kingdom/TurnTickingEngine.kt` | Extend `tick()` with pacing metric evaluation logic; add alert generation to `TickResult` |
| `kingdom/sheet/KingdomSheet.kt` | Register new section; wire up alerts panel rendering and event clearing |
| `lang/en.json` | Add ~25 localization keys for alert types, severity labels, and settings |

### Modified Handlebars Files (1 file)

| File | Changes |
|------|---------|
| `src/jsMain/resources/applications/kingdom/kingdom-sheet.hbs` | Include the new `{{> kingdom-pacing-alerts}}` section |

---

## Data Models

### RawPacingAlert
File: `src/jsMain/kotlin/.../kingdom/data/RawPacingAlert.kt`

```kotlin
@JsPlainObject
external interface RawPacingAlert {
    var id: String                      // UUID v4
    var type: String                    // Enum string (e.g., "LEVEL_MISMATCH", "STAGNATION")
    var severity: String               // Enum: "WARNING" | "CRITICAL"
    var message: String                 // Human-readable text (i18n key or raw)
    var turnCreated: Int                // The kingdom turn when the alert was generated
    var relatedEntityId: String?        // Optional link to a Quest, Event, or Settlement ID
}
```

### KingdomSettings Extensions
In `KingdomSettings.kt`, add thresholds for pacing evaluation:

```kotlin
// Pacing Alert Thresholds (Gregory Profile)
var pacingAlertMinUnrestDelta: Int    // Minimum unrest change before "stagnation" warning (e.g., 5)
var pacingAlertMaxTurnGap: Int        // Max turns without an event before "stagnation" alert (e.g., 10)
var pacingAlertLevelMismatchRange: Int // Allowed +/- level difference from chapter target (e.g., 2)
var pacingAlertLootImbalanceEnabled: Boolean // Toggle for item access checks (default: true)
```

---

## Migration Plan

**No breaking migration required.** All new fields in `KingdomData` are nullable arrays or objects, following the established pattern in the repository. Existing kingdom actors will simply initialize with an empty alerts array and default thresholds on their first load after the update.

### Steps:
1. **Code Deployment:** Update Kotlin/JS source and re-compile.
2. **Automatic Initialization:** On next `TurnTickingEngine` tick, if `pacingAlerts` is null, initialize as `emptyArray()`.
3. **Feature Toggle:** The UI section will only render if the user has not explicitly disabled it via a setting (if added later).

---

/## Testing Strategy

### Test Files to Create (~25 tests)

**jsTest — Data Models & Enums (~8 tests)**
- `RawPacingAlertSerializationTest.kt`: JSON round-trip fidelity for alerts.
- `PacingAlertStatusTest.kt`: Enum parsing and i18n key generation.

**jsTest — Logic & Engine Integration (~12 tests)**
- `PacingAlertLogicTest.kt`: Unit tests for the threshold evaluation functions (e.g., verifying "stagnation" triggers when turns > gap).
- `TurnTickingEngineIntegrationTest.kt`: Verify that a turn tick with high unrest/ruin delta correctly generates or clears alerts in `KingdomData`.

**jsTest — UI & Templates (~5 tests)**
- `PacingAlertContextTest.kt`: Context builder with alert history and severity flags.
- `PacingAlertTemplateTest.kt`: Verify `alert-card.hbs` renders different colors for `WARNING` vs `CRITICAL`.

---

## Manual Verification Checklist

### UI Appearance (4 steps)
1. Open Kingdom Sheet → verify "Pacing Alerts" section/tab appears.
2. Verify alert cards display: Type, Message, Turn, and Severity badge.
3. Verify severity-based color coding (Yellow for `WARNING`, Red for `CRITICAL`).
4. Verify empty state message: "No active pacing alerts."

### Alert Triggering (7 steps)
5. **Level Mismatch:** Manually set kingdom level to 10 but target chapter is level 3 → verify alert triggers on next turn tick.
6. **Stagnation (Unrest):** Ensure unrest has not changed for > `pacingAlertMinUnrestDelta` turns → verify "Unrest Stagnation" warning.
7. **Turn Gap:** Advance turns without any quest/event changes → verify "Campaign Stagnation" alert.
8. **Loot Imbalance:** Add a high-level settlement structure to a low-level kingdom $\rightarrow$ verify item access alert.
9. **Clear Alerts:** Trigger an event (e.g., new quest) that breaks stagnation $\rightarrow$ verify old alerts can be cleared or updated via engine logic.
10. **Chat Integration:** Verify a `pacing-alert` chat message is sent to the GM when a `CRITICAL` alert is generated.

### Configuration (3 steps)
11. Modify `pacingAlertMaxTurnGap` in settings $\rightarrow$ verify alert frequency changes accordingly.
17. Disable `pacingAlertLootImbalanceEnabled` $\rightarrow$ verify loot-related alerts no longer appear.
18. Verify all UI text uses i18n keys (no hardcoded English).
