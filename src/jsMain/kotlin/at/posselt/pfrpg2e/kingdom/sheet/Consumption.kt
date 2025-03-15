package at.posselt.pfrpg2e.kingdom.sheet

import at.posselt.pfrpg2e.data.kingdom.RealmData
import at.posselt.pfrpg2e.data.kingdom.settlements.Settlement
import at.posselt.pfrpg2e.kingdom.resources.calculateConsumption
import at.posselt.pfrpg2e.utils.postChatMessage
import at.posselt.pfrpg2e.utils.postChatTemplate
import js.objects.recordOf
import kotlin.math.abs

suspend fun payConsumption(
    settlements: List<Settlement>,
    realmData: RealmData,
    armyConsumption: Int,
    availableFood: Int,
    now: Int,
): Int {
    val consumption = calculateConsumption(
        settlements = settlements,
        realmData = realmData,
        armyConsumption = armyConsumption,
        now = now,
    ).total
    return if (consumption > 0) {
        val consumedFood = availableFood - consumption
        val paidFood = consumedFood.coerceIn(0, Int.MAX_VALUE)
        postChatMessage("Reducing Food by $consumption")
        if (consumedFood < 0) {
            postChatTemplate(
                templatePath = "chatmessages/not-enough-food.hbs",
                templateContext = recordOf("food" to abs(consumedFood))
            )
        }
        paidFood
    } else {
        postChatMessage("Paying Consumption")
        availableFood
    }
}