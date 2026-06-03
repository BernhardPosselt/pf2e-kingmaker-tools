# Settlement Details Matrix Workbook Rows Implementation Plan

> **For Hermes:** Use subagent-driven-development skill to implement this plan task-by-task.

**Goal:** Bring the `Settlements` sheet rows `A46:A145` from `Copy of Royal Kingdom Sheet for Pathfinder 2E - CURRENT.xlsx` into the app's settlement Detailed Matrix so each settlement column can show structure-derived bonuses for the same skill/activity rows as the workbook.

**Architecture:** Add a small common Kotlin source-of-truth list for the workbook rows and a pure helper that calculates the displayed settlement bonus from a `Settlement`'s existing `highestUniqueBonuses`. Then expose those rows through the JS sheet context and render them under the existing settlement statistics/item-level matrix.

**Tech Stack:** Kotlin Multiplatform commonMain/commonTest, Kotlin/JS Foundry sheet contexts, Handlebars templates, Gradle validation/compile tasks.

---

## Source of truth

- Workbook: `/home/grego/code/pf2e-kingmaker-tools/Copy of Royal Kingdom Sheet for Pathfinder 2E - CURRENT.xlsx`
- Sheet/range: `Settlements!A46:A145`
- Existing target matrix:
  - `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/contexts/SettlementsContext.kt`
  - `src/jsMain/resources/applications/kingdom/sections/settlements/page.hbs`
  - `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/KingdomSheet.kt`
- Existing source of settlement bonuses: `Settlement.highestUniqueBonuses`, derived from constructed structure `skillBonusRules` and `activityBonusRules`.

## Non-goals

- Do not migrate new structures or activity JSON in this task.
- Do not change roll modifier semantics; this only exposes already-computed settlement bonuses in the matrix.
- Do not edit unrelated dirty pack files or existing unrelated workbook migration files.
- Do not require browser/Karma execution in WSL if FirefoxHeadless remains unavailable; use compile/deterministic checks instead.

---

### Task 1: Add failing common tests for workbook row coverage and bonus matching

**Objective:** Lock in the workbook row labels and the bonus calculation semantics before production code.

**Files:**
- Create: `src/commonTest/kotlin/at/posselt/pfrpg2e/data/kingdom/settlements/SettlementDetailsMatrixTest.kt`
- Later create: `src/commonMain/kotlin/at/posselt/pfrpg2e/data/kingdom/settlements/SettlementDetailsMatrix.kt`

**Step 1: Write failing test**

Add tests that assert:
- `settlementDetailsMatrixRows` contains the workbook labels in order from rows 46-145, including representative labels:
  - `Agriculture`, `Establish Farmland`, `Rest and Relax (Arts)`, `Boating`, `Establish Work Site (Mine)`, `Pledge of Fealty (Warfare)`, `Focused Attention`, `Build Structure`, `Recover Army`, `Train Army`.
- `matrixBonusFor(row)` returns:
  - a skill-only bonus for a skill row;
  - the best of skill-only, activity-only, and skill+activity bonuses for a skill-qualified activity row;
  - the best matching activity bonus for a general activity row;
  - `null` for pure section rows with no mapped skill/activity if any remain.

**Step 2: Run test to verify failure**

Run:

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew compileTestKotlinJs
```

Expected: FAIL because `SettlementDetailsMatrix.kt` and its symbols do not exist yet.

---

### Task 2: Add common settlement details matrix source-of-truth and helper

**Objective:** Implement the smallest commonMain code needed to satisfy the failing tests.

**Files:**
- Create: `src/commonMain/kotlin/at/posselt/pfrpg2e/data/kingdom/settlements/SettlementDetailsMatrix.kt`

**Implementation details:**
- Define `SettlementDetailsMatrixRow(workbookRow: Int, label: String, skill: KingdomSkill? = null, activityId: String? = null, isSection: Boolean = false)`.
- Define `settlementDetailsMatrixRows` from workbook `A46:A145` in workbook order, skipping blank separator rows but preserving all named rows.
- Map parenthesized labels to both skill and activity where appropriate, e.g. `Rest and Relax (Arts)` -> skill `ARTS`, activity `rest-and-relax`.
- Map general rows like `Build Structure`, `Claim Hex`, `Recover Army`, `Train Army` by activity id and no fixed skill.
- Add `fun Settlement.matrixBonusFor(row: SettlementDetailsMatrixRow): Int?` using `highestUniqueBonuses`.

**Bonus matching rule:**
- Skill-only row: bonuses with matching skill and no activity.
- Skill-qualified activity row: max of matching skill-only, matching activity-only, and exact skill+activity bonuses.
- Activity-only/general row: max of bonuses with matching activity, regardless of skill.
- Return `null` when no bonus applies so the template can render an em dash.

**Step 2: Run test to verify pass**

Run:

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew compileTestKotlinJs
```

Expected: PASS compilation.

---

### Task 3: Expose details matrix rows through the JS sheet context

**Objective:** Make each workbook row available to `page.hbs` with one cell per settlement column.

**Files:**
- Modify: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/contexts/SettlementsContext.kt`
- Modify: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/contexts/KingdomSheetContext.kt`
- Modify: `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/KingdomSheet.kt`

**Implementation details:**
- Add JS context interfaces:
  - `SettlementDetailsMatrixCellContext(value: String)`
  - `SettlementDetailsMatrixRowContext(workbookRow: Int, label: String, isSection: Boolean, cells: Array<SettlementDetailsMatrixCellContext>)`
- Add `fun Array<SettlementsContext>.toDetailsMatrixRows(...)` or build rows directly from parsed `Settlement` objects before converting to `SettlementsContext`.
- Prefer computing rows once from parsed settlements so cell order exactly matches settlement column order.
- Format bonus values as `+N`; render `—` for null/no bonus.

**Verification:**

Run:

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew compileKotlinJs compileTestKotlinJs
```

Expected: PASS.

---

### Task 4: Render the workbook rows in the detailed matrix template

**Objective:** Display the migrated rows under the existing item-level rows in the Detailed Matrix view.

**Files:**
- Modify: `src/jsMain/resources/applications/kingdom/sections/settlements/page.hbs`
- Optional modify: `src/jsMain/resources/components/simple-app.css` or existing settlement CSS if minimal styling is needed.

**Implementation details:**
- Add a section header row after item levels: `Settlement Bonuses` or `Workbook Activity Bonuses`.
- Iterate over `settlementDetailsRows`.
- First cell: label from workbook row A.
- Data cells: preformatted `+N` or `—`.
- Apply a section/header CSS class to skill/category rows, but keep cells visible.

**Verification:**

Run:

```bash
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew compileKotlinJs
```

Expected: PASS.

---

### Task 5: Deterministic workbook reconciliation and build validation

**Objective:** Prove the code row list matches the workbook range and the app still validates/builds.

**Files:**
- Create optional scratch verifier only if useful: `scripts/verify_settlement_details_matrix.py` or keep as a one-off command in the task handoff.

**Verification commands:**

```bash
python3 - <<'PY'
import openpyxl, pathlib, re
wb = openpyxl.load_workbook('Copy of Royal Kingdom Sheet for Pathfinder 2E - CURRENT.xlsx', data_only=False)
labels = [wb['Settlements'].cell(r, 1).value for r in range(46, 146)]
labels = [x for x in labels if x]
source = pathlib.Path('src/commonMain/kotlin/at/posselt/pfrpg2e/data/kingdom/settlements/SettlementDetailsMatrix.kt').read_text()
missing = [label for label in labels if f'"{label}"' not in source]
extra_count = source.count('SettlementDetailsMatrixRow(')
print({'workbook_labels': len(labels), 'missing': missing, 'row_ctor_count': extra_count})
assert not missing
PY
JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew validateStructures validateKingdomActivities compileTestKotlinJs compileKotlinJs
```

Expected: no missing workbook labels; Gradle commands pass. If `jsBrowserTest`/full `assemble` is run and fails due to FirefoxHeadless in WSL, record it as an environment-only blocker only after the compile/schema checks pass.

## Acceptance criteria

- A plan for this migration chunk exists under `docs/plans/`.
- Tests covering the row list and bonus calculation exist before production implementation.
- Detailed Matrix includes all nonblank labels from `Settlements!A46:A145` in workbook order.
- Each displayed cell uses existing settlement structure bonuses and shows `+N` or `—` consistently.
- Deterministic workbook reconciliation and Gradle validation/compile checks pass, or environment-only browser blockers are clearly separated from code failures.
