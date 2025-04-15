package at.posselt.pfrpg2e.kingdom.sheet

import at.posselt.pfrpg2e.data.kingdom.RealmData
import at.posselt.pfrpg2e.data.kingdom.settlements.Settlement
import at.posselt.pfrpg2e.data.kingdom.settlements.SettlementSizeType
import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.data.ChosenFeat
import at.posselt.pfrpg2e.kingdom.resources.Income
import at.posselt.pfrpg2e.kingdom.resources.calculateIncome
import at.posselt.pfrpg2e.kingdom.resources.calculateStorage
import at.posselt.pfrpg2e.utils.postChatTemplate
import at.posselt.pfrpg2e.utils.roll
import at.posselt.pfrpg2e.utils.t
import js.objects.JsPlainObject

@Suppress("unused")
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
    resourceDice: Int,
    increaseGainedLuxuries: Int,
    settlements: List<Settlement>,
): Income {
    val income = calculateIncome(
        realmData = realmData,
        resourceDice = resourceDice,
        increaseGainedLuxuries = increaseGainedLuxuries,
    )
    val rolledRp = roll(income.resourcePointsFormula, flavor = t("kingdom.gainingResourcePoints"))
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

fun KingdomData.getResourceDiceAmount(
    allFeats: List<ChosenFeat>,
    settlements: List<Settlement>,
    kingdomLevel: Int,
) = 4 + kingdomLevel + allFeats.sumOf { it.feat.resourceDice ?: 0 } +
        resourceDice.now +
        settlements.sumOf {
            when (it.size.type) {
                SettlementSizeType.VILLAGE -> settings.resourceDicePerVillage
                SettlementSizeType.TOWN -> settings.resourceDicePerTown
                SettlementSizeType.CITY -> settings.resourceDicePerCity
                SettlementSizeType.METROPOLIS -> settings.resourceDicePerMetropolis
            }
        }