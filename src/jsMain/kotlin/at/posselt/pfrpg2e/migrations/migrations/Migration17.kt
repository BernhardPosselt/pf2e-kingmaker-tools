package at.posselt.pfrpg2e.migrations.migrations

import at.posselt.pfrpg2e.camping.CampingData
import at.posselt.pfrpg2e.camping.RawCost
import at.posselt.pfrpg2e.kingdom.KingdomData
import com.foundryvtt.core.Game

private val costRegex = "^(?<value>\\d+)\\s+(?<currency>cp|sp|gp|pp)$".toRegex()

class Migration17 : Migration(17) {
    override suspend fun migrateKingdom(game: Game, kingdom: KingdomData) {
        kingdom.modifiers = kingdom.modifiers
            .map { it.copy(requiresTranslation = false) }
            .toTypedArray()
    }

    override suspend fun migrateCamping(game: Game, camping: CampingData) {
        camping.cooking.homebrewMeals = camping.cooking.homebrewMeals
            .map {
                val costString = it.cost.unsafeCast<String>().trim()
                val matches = costRegex.find(costString)
                val currency = matches?.groups["currency"]?.value ?: "gp"
                val value =  matches?.groups["value"]?.value?.toInt() ?: 0
                it.copy(
                    cost = RawCost(
                        currency = currency,
                        value = value,
                    )
                )
            }
            .toTypedArray()
    }
}