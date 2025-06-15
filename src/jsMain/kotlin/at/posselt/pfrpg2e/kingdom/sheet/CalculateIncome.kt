package at.posselt.pfrpg2e.kingdom.sheet

import at.posselt.pfrpg2e.data.kingdom.RealmData
import at.posselt.pfrpg2e.data.kingdom.settlements.Settlement
import at.posselt.pfrpg2e.data.kingdom.settlements.SettlementSizeType
import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.data.ChosenFeat
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierSelector
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.evaluateModifiers
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.filterModifiersAndUpdateContext
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.ExpressionContext
import at.posselt.pfrpg2e.kingdom.resources.Income
import at.posselt.pfrpg2e.kingdom.resources.calculateIncome
import at.posselt.pfrpg2e.kingdom.resources.calculateStorage
import at.posselt.pfrpg2e.utils.postChatTemplate
import at.posselt.pfrpg2e.utils.roll
import at.posselt.pfrpg2e.utils.t
import js.objects.JsPlainObject
import kotlin.math.max
import kotlin.math.min

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
    expressionContext: ExpressionContext,
    modifiers: List<Modifier>,
): Income {
    val income = calculateIncome(
        realmData = realmData,
        resourceDice = resourceDice,
        increaseGainedLuxuries = increaseGainedLuxuries,
    )
    val ore = calculateModifierResource(modifiers, expressionContext, ModifierSelector.ORE)
    val stone = calculateModifierResource(modifiers, expressionContext, ModifierSelector.STONE)
    val lumber = calculateModifierResource(modifiers, expressionContext, ModifierSelector.LUMBER)
    val rolledRp = roll(income.resourcePointsFormula, flavor = t("kingdom.gainingResourcePoints"))
    postChatTemplate(
        templatePath = "chatmessages/collect-resources.hbs",
        templateContext = CollectResources(
            rp = rolledRp,
            ore = income.ore + ore,
            stone = income.stone + stone,
            lumber = income.lumber + lumber,
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

private fun calculateModifierResource(
    modifiers: List<Modifier>,
    expressionContext: ExpressionContext,
    selector: ModifierSelector
): Int = evaluateModifiers(filterModifiersAndUpdateContext(modifiers, expressionContext, selector)).total

fun KingdomData.getResourceDiceAmount(
    allFeats: List<ChosenFeat>,
    settlements: List<Settlement>,
    kingdomLevel: Int,
) = 4 +
        kingdomLevel +
        allFeats.sumOf { it.feat.resourceDice ?: 0 } +
        resourceDice.now +
        if (settings.settlementsGenerateRd) {
            settlements.sumOf {
                when (it.size.type) {
                    SettlementSizeType.VILLAGE -> 0
                    SettlementSizeType.TOWN -> min(it.maximumCivicRdLimit, 1)
                    SettlementSizeType.CITY -> max(1, min(it.maximumCivicRdLimit, 2))
                    SettlementSizeType.METROPOLIS -> max(1, min(it.maximumCivicRdLimit, 4))
                }
            }
        } else {
            0
        }