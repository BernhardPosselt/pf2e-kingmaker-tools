package at.posselt.pfrpg2e.migrations.migrations

import at.posselt.pfrpg2e.camping.ActorMeal
import at.posselt.pfrpg2e.camping.CampingActivity
import at.posselt.pfrpg2e.camping.CampingData
import at.posselt.pfrpg2e.camping.CookingResult
import at.posselt.pfrpg2e.utils.toMutableRecord
import com.foundryvtt.core.Game
import kotlinx.js.JsPlainObject

@JsPlainObject
private external interface OldCampingActivity {
    var activityId: String
    var actorUuid: String?
    var result: String?
    var selectedSkill: String?
}

@JsPlainObject
private external interface OldCookingResult {
    val recipeId: String
    var result: String?
    val skill: String
}

@JsPlainObject
external interface OldActorMeal {
    var actorUuid: String
    var favoriteMeal: String?
    var chosenMeal: String
}

class Migration20 : Migration(20) {
    override suspend fun migrateCamping(game: Game, camping: CampingData) {
        camping.campingActivities = camping.campingActivities.unsafeCast<Array<OldCampingActivity>>()
            .map {
                it.activityId to CampingActivity(
                    actorUuid = it.actorUuid,
                    result = it.result,
                    selectedSkill = it.selectedSkill,
                )
            }
            .toMutableRecord()
        camping.cooking.results = camping.cooking.results.unsafeCast<Array<OldCookingResult>>()
            .map {
                it.recipeId to CookingResult(
                    result = it.result,
                    skill = it.skill,
                )
            }
            .toMutableRecord()
        camping.cooking.actorMeals = camping.cooking.actorMeals.unsafeCast<Array<OldActorMeal>>()
            .map {
                it.actorUuid to ActorMeal(
                    favoriteMeal = it.favoriteMeal,
                    chosenMeal = it.chosenMeal,
                )
            }
            .toMutableRecord()
    }
}