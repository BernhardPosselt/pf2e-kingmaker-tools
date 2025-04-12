package at.posselt.pfrpg2e.migrations.migrations

import at.posselt.pfrpg2e.camping.CampingData
import at.posselt.pfrpg2e.camping.RawCost
import at.posselt.pfrpg2e.camping.getAllRecipes
import at.posselt.pfrpg2e.kingdom.KingdomData
import com.foundryvtt.core.Game

val costRegex = "^(?<value>\\d+)\\s+(?<currency>cp|sp|gp|pp)$".toRegex()

class Migration16 : Migration(16) {
    override suspend fun migrateKingdom(game: Game, kingdom: KingdomData) {
        kingdom.homebrewFeats = kingdom.homebrewFeats.map {
            it.copy(rollOptions = it.asDynamic().flags)
        }.toTypedArray()
        kingdom.modifiers = kingdom.modifiers.filter {
            it.valueExpression == null
        }.toTypedArray()
        kingdom.settings.eventDc = 16
        kingdom.settings.eventDcStep = 5
        kingdom.settings.cultEventDc = 20
        kingdom.settings.cultEventDcStep = 2
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