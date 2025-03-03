package at.posselt.pfrpg2e.migrations.migrations

import at.posselt.pfrpg2e.kingdom.KingdomData
import com.foundryvtt.core.Game

class Migration9 : Migration(9) {
    override suspend fun migrateKingdom(game: Game, kingdom: KingdomData) {
        val recoverArmyIds = arrayOf(
            "recover-army-damaged",
            "recover-army-defeated",
            "recover-army-lost",
            "recover-army-mired-pinned",
            "recover-army-shaken",
            "recover-army-weary",
        )
        kingdom.modifiers.forEach { m ->
            val activities = m.asDynamic().activities
            if (activities != null && activities.contains("recover-army")) {
                m.asDynamic().activities = activities.unsafeCast<Array<String>>().filter { it != "recover-army" }.toTypedArray() + recoverArmyIds
            }
        }
    }
}