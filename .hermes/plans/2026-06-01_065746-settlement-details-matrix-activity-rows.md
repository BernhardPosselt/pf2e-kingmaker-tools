# Settlement details matrix activity rows migration

## Goal
Bring the workbook `Settlements` sheet rows `A46:A145` from `Copy of Royal Kingdom Sheet for Pathfinder 2E - CURRENT.xlsx` into the Foundry app's settlement Detailed Matrix.

## Source context
- Docs reference root: `/home/grego/code/pf2e-kingmaker-tools/docs`
- Current matrix docs: `docs/migration_tasks.md` says the Settlements comparison matrix is code-complete for core stats, but the workbook rows after Item Levels are not yet represented.
- Workbook source rows A46:A145 contain 82 non-empty labels: kingdom skill section headers plus activity rows from Agriculture through Train Army.
- Current UI only renders rows through item levels in `src/jsMain/resources/applications/kingdom/sections/settlements/page.hbs`.

## Proposed approach
1. Add common Kotlin data for the migrated workbook rows under `src/commonMain/kotlin/at/posselt/pfrpg2e/data/kingdom/settlements/`.
   - Preserve workbook order.
   - Model section headers vs activity rows.
   - Map activity rows to existing activity IDs and optional skill context for variants such as `Rest and Relax (Arts)` and `Quell Unrest (Warfare)`.
2. Add a deterministic common test before implementation.
   - Assert row count is 82.
   - Assert first/last/source row numbers.
   - Assert representative mappings: Agriculture header, Rest and Relax (Arts), Establish Work Site variants, Focused Attention, General/Army headers, Train Army.
3. Wire the row data into `SettlementsContext.kt` so each settlement gets a rendered value for every row.
   - Skill header rows show matching skill-only structure bonus values.
   - Activity rows show the best applicable settlement structure bonus for the row's activity and optional skill.
   - Zero/absent bonuses render as an em dash for readability.
4. Extend `page.hbs` detailed matrix after the item level rows to render the workbook rows.
5. Run targeted tests/build checks.

## Files likely to change
- `src/commonMain/kotlin/at/posselt/pfrpg2e/data/kingdom/settlements/SettlementActivityMatrixRows.kt`
- `src/commonTest/kotlin/at/posselt/pfrpg2e/data/kingdom/settlements/SettlementActivityMatrixRowsTest.kt`
- `src/jsMain/kotlin/at/posselt/pfrpg2e/kingdom/sheet/contexts/SettlementsContext.kt`
- `src/jsMain/resources/applications/kingdom/sections/settlements/page.hbs`

## Validation
- RED: `JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew commonTest --tests at.posselt.pfrpg2e.data.kingdom.settlements.SettlementActivityMatrixRowsTest`
- GREEN: rerun the same test after implementation.
- Compile: `JAVA_HOME=/home/grego/.local/jdks/jdk-25.0.3+9 ./gradlew compileKotlinJs compileTestKotlinJs`

## Risks / notes
- This preserves workbook matrix labels and computes values from existing parsed settlement structure bonuses; it does not import the workbook's cached sample settlement values.
- If a workbook row references an activity not present in the app, the deterministic test should catch the mapping explicitly rather than silently dropping it.
