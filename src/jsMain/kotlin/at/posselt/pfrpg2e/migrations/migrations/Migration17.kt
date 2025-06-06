package at.posselt.pfrpg2e.migrations.migrations

import at.posselt.pfrpg2e.camping.CampingData
import at.posselt.pfrpg2e.camping.RawCost
import at.posselt.pfrpg2e.camping.RecipeData
import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.RawModifier
import at.posselt.pfrpg2e.kingdom.data.RawGroup
import com.foundryvtt.core.Game

private val costRegex = "^(?<value>\\d+)\\s+(?<currency>cp|sp|gp|pp)$".toRegex()

class Migration17 : Migration(17) {
    override suspend fun migrateKingdom(game: Game, kingdom: KingdomData) {
        kingdom.modifiers = kingdom.modifiers
            .map { RawModifier.copy(it, requiresTranslation = false) }
            .toTypedArray()
        kingdom.groups = kingdom.groups.map {
            RawGroup.copy(it, preventPledgeOfFealty = false)
        }.toTypedArray()
    }

    override suspend fun migrateCamping(game: Game, camping: CampingData) {
        camping.cooking.homebrewMeals = camping.cooking.homebrewMeals
            .map {
                val costString = it.cost.unsafeCast<String>().trim()
                val matches = costRegex.find(costString)
                val currency = matches?.groups["currency"]?.value ?: "gp"
                val value =  matches?.groups["value"]?.value?.toInt() ?: 0
                RecipeData.copy(
                    it,
                    cost = RawCost(
                        currency = currency,
                        value = value,
                    )
                )
            }
            .toTypedArray()
    }
}