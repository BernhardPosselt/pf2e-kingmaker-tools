package at.posselt.pfrpg2e.migrations.migrations

import com.foundryvtt.core.Game

/**
 * Backfills the [watchSlots] field that was added to the camping data model.
 *
 * Camping data saved before `watchSlots` existed has it as `undefined`, which crashes
 * the camping sheet (e.g. `camping.watchSlots.map { ... }` reads `.length` of undefined).
 *
 * Uses the `dynamic` receiver because `CampingData.watchSlots` is typed non-nullable,
 * so a typed `== null` check would be flagged as always-false.
 */
class Migration25 : Migration(25) {

    override suspend fun migrateCamping(game: Game, camping: dynamic) {
        if (camping.watchSlots == null) {
            camping.watchSlots = emptyArray<String>()
        }
    }
}
