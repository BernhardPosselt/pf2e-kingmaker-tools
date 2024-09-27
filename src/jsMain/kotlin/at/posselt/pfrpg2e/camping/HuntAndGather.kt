package at.posselt.pfrpg2e.camping

import at.posselt.pfrpg2e.actor.party
import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.utils.postChatTemplate
import at.posselt.pfrpg2e.utils.roll
import com.foundryvtt.core.Game
import com.foundryvtt.pf2e.actor.PF2EActor
import com.foundryvtt.pf2e.actor.PF2ECreature
import js.objects.recordOf
import kotlinx.js.JsPlainObject
import kotlin.math.min


private suspend fun getHuntAndGatherQuantities(
    degreeOfSuccess: DegreeOfSuccess,
    regionDc: Int,
    regionLevel: Int,
): FoodAmount {
    if (degreeOfSuccess == DegreeOfSuccess.CRITICAL_SUCCESS) {
        val specialIngredients = if (regionLevel >= 14) {
            12
        } else if (regionLevel >= 7) {
            8
        } else {
            4
        }
        return FoodAmount(
            basicIngredients = 2 * regionDc,
            specialIngredients = specialIngredients,
            rations = 0,
        )
    } else if (degreeOfSuccess == DegreeOfSuccess.SUCCESS) {
        val dice = if (regionLevel >= 14) {
            3
        } else if (regionLevel >= 7) {
            2
        } else {
            1
        }
        val specialIngredients = roll("${dice}d4", "Special Ingredients")
        return FoodAmount(
            basicIngredients = regionDc,
            specialIngredients = specialIngredients,
            rations = 0,
        )
    } else if (degreeOfSuccess == DegreeOfSuccess.FAILURE) {
        return FoodAmount(
            basicIngredients = regionDc,
            specialIngredients = 0,
            rations = 0,
        )
    } else {
        return FoodAmount(
            basicIngredients = min(roll("1d4", "Basic Ingredients"), regionDc),
            specialIngredients = 0,
            rations = 0,
        )
    }
}


suspend fun postHuntAndGather(
    actor: PF2ECreature,
    degreeOfSuccess: DegreeOfSuccess,
    zoneDc: Int,
    regionLevel: Int,
) {
    val amount = getHuntAndGatherQuantities(
        degreeOfSuccess = degreeOfSuccess,
        regionDc = zoneDc,
        regionLevel = regionLevel,
    )
    postChatTemplate(
        templatePath = "chatmessages/hunt-and-gather.hbs",
        templateContext = recordOf(
            "actorName" to actor.name,
            "actorUuid" to actor.uuid,
            "basicIngredients" to amount.basicIngredients,
            "specialIngredients" to amount.specialIngredients,
        )
    )
}

@JsPlainObject
external interface HuntAndGatherData {
    val actorUuid: String
    val basicIngredients: Int
    val specialIngredients: Int
}

suspend fun findHuntAndGatherTargetActor(
    game: Game,
    defaultActorUuid: String,
    data: CampingData,
): PF2EActor? {
    val party = game.party()
    return data.huntAndGatherTargetActorUuid?.let {
        if (party != null && party.uuid == it) {
            party
        } else if (data.actorUuids.contains(it)) {
            getCampingActorByUuid(it)
        } else {
            null
        }
    } ?: getCampingActorByUuid(defaultActorUuid)
}

