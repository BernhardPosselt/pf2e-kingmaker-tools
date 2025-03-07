package at.posselt.pfrpg2e.kingdom.sheet

import at.posselt.pfrpg2e.data.kingdom.RealmData
import at.posselt.pfrpg2e.data.kingdom.settlements.Settlement
import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.data.ChosenFeat
import at.posselt.pfrpg2e.kingdom.resources.Income
import at.posselt.pfrpg2e.kingdom.resources.calculateIncome
import at.posselt.pfrpg2e.kingdom.resources.calculateStorage
import at.posselt.pfrpg2e.utils.postChatTemplate
import at.posselt.pfrpg2e.utils.roll
import js.objects.JsPlainObject

@JsPlainObject
private external interface CollectResources {
    val rp: Int
    val ore: Int
    val stone: Int
    val lumber: Int
    val luxuries: Int
}

suspend fun collectResources(
    kingdomData: KingdomData,
    realmData: RealmData,
    allFeats: List<ChosenFeat>,
    settlements: List<Settlement>,
): Income {
    val income = calculateIncome(
        kingdomLevel = kingdomData.level,
        realmData = realmData,
        additionalResourceDice = allFeats.sumOf { it.feat.resourceDice ?: 0 } + kingdomData.resourceDice.now,
    )
    val rolledRp = roll(income.resourcePointsFormula, flavor = "Gaining Resource Points")
    postChatTemplate(
        templatePath = "chatmessages/collect-resources.hbs",
        templateContext = CollectResources(
            rp = rolledRp,
            ore = income.ore,
            stone = income.stone,
            lumber = income.lumber,
            luxuries = income.luxuries,
        ),
    )
    return income
        .copy(
            resourcePoints = income.resourcePoints + rolledRp + kingdomData.resourcePoints.now,
            ore = income.ore + kingdomData.commodities.now.ore,
            lumber = income.lumber + kingdomData.commodities.now.lumber,
            luxuries = income.luxuries + kingdomData.commodities.now.luxuries,
            stone = income.stone + kingdomData.commodities.now.stone,
            resourceDice = 0,
        )
        .limitBy(calculateStorage(realmData, settlements))
}