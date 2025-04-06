package at.posselt.pfrpg2e.migrations.migrations

import at.posselt.pfrpg2e.camping.ActorMeal
import at.posselt.pfrpg2e.camping.CampingActivity
import at.posselt.pfrpg2e.camping.CampingData
import at.posselt.pfrpg2e.camping.CookingResult
import at.posselt.pfrpg2e.camping.getAllActivities
import at.posselt.pfrpg2e.camping.getAllRecipes
import at.posselt.pfrpg2e.kingdom.structures.StructureRef
import at.posselt.pfrpg2e.kingdom.structures.getRawStructureData
import at.posselt.pfrpg2e.kingdom.structures.isStructure
import at.posselt.pfrpg2e.kingdom.structures.isStructureRef
import at.posselt.pfrpg2e.kingdom.structures.setStructureData
import at.posselt.pfrpg2e.kingdom.structures.structures
import at.posselt.pfrpg2e.slugify
import com.foundryvtt.core.Game
import com.foundryvtt.pf2e.actor.PF2ENpc
import kotlinx.js.JsPlainObject

@JsPlainObject
private external interface OldCookingResult {
    val recipeName: String
    var result: String?
    val skill: String
}

@JsPlainObject
external interface OldCampingActivity {
    var activity: String
    var actorUuid: String?
    var result: String?
    var selectedSkill: String?
}

class Migration15 : Migration(15) {
    override suspend fun migrateOther(game: Game) {
        val structuresByName = structures.associateBy { it.name }
        game.actors
            .contents
            .filterIsInstance<PF2ENpc>()
            .filter { it.isStructure() }
            .forEach {
                val data = it.getRawStructureData()
                if (data != null && isStructureRef(data)) {
                    val name = data.ref
                    val id = structuresByName[name]?.id
                    if (id != null) {
                        it.setStructureData(StructureRef(ref = id))
                    }
                }
            }
    }

    override suspend fun migrateCamping(game: Game, camping: CampingData) {
        camping.cooking.homebrewMeals = camping.cooking.homebrewMeals.map {
            it.copy(id = it.name.slugify())
        }.toTypedArray()
        camping.homebrewCampingActivities = camping.homebrewCampingActivities.map {
            it.copy(id = it.name.slugify())
        }.toTypedArray()
        val recipesByName = camping.getAllRecipes().associateBy { it.name }
        val activitiesByName = camping.getAllActivities().associateBy { it.name }
        camping.alwaysPerformActivityIds = camping.asDynamic()
            .alwaysPerformActivities
            .unsafeCast<Array<String>>()
            .mapNotNull { activitiesByName[it]?.id }
            .toTypedArray()
        camping.campingActivities = camping.campingActivities.mapNotNull {
            val activity = it.unsafeCast<OldCampingActivity>()
            activitiesByName[activity.activity]?.let { act ->
                CampingActivity(
                    activityId = act.id,
                    actorUuid = activity.actorUuid,
                    result = activity.result,
                    selectedSkill = activity.selectedSkill,
                )
            }
        }.toTypedArray()
        camping.cooking.knownRecipes = camping.cooking.knownRecipes
            .mapNotNull { recipesByName[it]?.id }
            .toTypedArray()
        camping.lockedActivities = camping.lockedActivities
            .mapNotNull { activitiesByName[it]?.id }
            .toTypedArray()
        camping.cooking.results = camping.cooking.results
            .mapNotNull { result ->
                val res = result.unsafeCast<OldCookingResult>()
                recipesByName[res.recipeName]?.let {
                    CookingResult(
                        recipeId = it.id,
                        result = result.result,
                        skill = result.skill,
                    )
                }

            }.toTypedArray()
        camping.cooking.actorMeals = camping.cooking.actorMeals.mapNotNull {
            recipesByName[it.chosenMeal]?.let { chosenMeal ->
                ActorMeal(
                    actorUuid = it.actorUuid,
                    favoriteMeal = it.favoriteMeal?.let { recipesByName[it] }?.id,
                    chosenMeal = chosenMeal.id,
                )
            }
        }.toTypedArray()
    }
}