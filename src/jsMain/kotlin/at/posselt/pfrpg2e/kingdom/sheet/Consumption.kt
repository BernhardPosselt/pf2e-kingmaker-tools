package at.posselt.pfrpg2e.kingdom.sheet

import at.posselt.pfrpg2e.data.kingdom.RealmData
import at.posselt.pfrpg2e.data.kingdom.settlements.Settlement
import at.posselt.pfrpg2e.kingdom.KingdomActor
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.ExpressionContext
import at.posselt.pfrpg2e.kingdom.resources.calculateConsumption
import at.posselt.pfrpg2e.utils.postChatMessage
import at.posselt.pfrpg2e.utils.postChatTemplate
import at.posselt.pfrpg2e.utils.t
import js.objects.recordOf
import kotlin.math.abs

suspend fun payConsumption(
    kingdomActor: KingdomActor,
    settlements: List<Settlement>,
    realmData: RealmData,
    armyConsumption: Int,
    availableFood: Int,
    now: Int,
    expressionContext: ExpressionContext,
    modifiers: List<Modifier>,
): Int {
    val consumption = calculateConsumption(
        settlements = settlements,
        realmData = realmData,
        armyConsumption = armyConsumption,
        now = now,
        expressionContext = expressionContext,
        modifiers = modifiers,
    ).total
    return if (consumption > 0) {
        val consumedFood = availableFood - consumption
        val paidFood = consumedFood.coerceIn(0, Int.MAX_VALUE)
        postChatMessage(t("kingdom.reducingFoodBy", recordOf("consumption" to consumption)))
        if (consumedFood < 0) {
            val missingFood = abs(consumedFood)
            postChatTemplate(
                templatePath = "chatmessages/not-enough-food.hbs",
                templateContext = recordOf(
                    "food" to missingFood,
                    "actorUuid" to kingdomActor.uuid,
                    "loseRp" to missingFood * 5,
                )
            )
        }
        paidFood
    } else {
        postChatMessage(t("kingdom.payingConsumption"))
        availableFood
    }
}