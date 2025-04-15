@file:Suppress("SpellCheckingInspection")

package at.posselt.pfrpg2e.camping

import at.posselt.pfrpg2e.actor.hasAttribute
import at.posselt.pfrpg2e.camping.dialogs.RegionSetting
import at.posselt.pfrpg2e.camping.dialogs.RegionSettings
import at.posselt.pfrpg2e.camping.dialogs.Track
import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.data.actor.Attribute
import at.posselt.pfrpg2e.data.actor.Lore
import at.posselt.pfrpg2e.data.actor.Skill
import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.data.regions.Terrain
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.utils.getAppFlag
import at.posselt.pfrpg2e.utils.setAppFlag
import at.posselt.pfrpg2e.utils.t
import at.posselt.pfrpg2e.utils.unsetAppFlag
import at.posselt.pfrpg2e.utils.worldTimeSeconds
import com.foundryvtt.core.Game
import com.foundryvtt.core.utils.deepClone
import com.foundryvtt.pf2e.actor.PF2EActor
import com.foundryvtt.pf2e.actor.PF2ECharacter
import com.foundryvtt.pf2e.actor.PF2EParty
import js.objects.recordOf
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface ActorMeal {
    var actorUuid: String
    var favoriteMeal: String?
    var chosenMeal: String
}

@JsPlainObject
external interface CookingResult {
    val recipeId: String
    var result: String?
    val skill: String
}

@JsPlainObject
external interface Cooking {
    var knownRecipes: Array<String>
    var actorMeals: Array<ActorMeal>
    var homebrewMeals: Array<RecipeData>
    var results: Array<CookingResult>
    var minimumSubsistence: Int
}

@JsPlainObject
external interface CampingActivity {
    var activityId: String
    var actorUuid: String?
    var result: String?
    var selectedSkill: String?
}


@JsPlainObject
external interface CampingData {
    var currentRegion: String
    var actorUuids: Array<String>
    var campingActivities: Array<CampingActivity>
    var homebrewCampingActivities: Array<CampingActivityData>
    var lockedActivities: Array<String>
    var cooking: Cooking
    var watchSecondsRemaining: Int
    var gunsToClean: Int
    var dailyPrepsAtTime: Int
    var encounterModifier: Int
    var restRollMode: String
    var increaseWatchActorNumber: Int
    var actorUuidsNotKeepingWatch: Array<String>
    var alwaysPerformActivityIds: Array<String>
    var huntAndGatherTargetActorUuid: String?
    var proxyRandomEncounterTableUuid: String?
    var randomEncounterRollMode: String
    var ignoreSkillRequirements: Boolean
    var minimumTravelSpeed: Int?
    var regionSettings: RegionSettings
    var section: String
    var restingTrack: Track?
    var worldSceneId: String?
    var isActive: Boolean?
}

suspend fun CampingData.getActorsCarryingFood(party: PF2EParty?): List<PF2EActor> =
    getActorsInCamp() + listOfNotNull(party)


suspend fun CampingData.getAveragePartyLevel(): Int =
    getActorsInCamp()
        .filterIsInstance<PF2ECharacter>()
        .map { it.level }
        .takeIf { it.isNotEmpty() }
        ?.average()
        ?.toInt()
        ?: 1

suspend fun CampingData.getActorsInCamp(
    campingActivityOnly: Boolean = false,
): List<PF2EActor> = coroutineScope {
    actorUuids
        .map {
            async {
                if (campingActivityOnly) {
                    getCampingActivityActorByUuid(it)
                } else {
                    getCampingActorByUuid(it)
                }
            }
        }
        .awaitAll()
        .filterNotNull()
}

fun CampingActivity.parseResult() =
    result?.let { fromCamelCase<DegreeOfSuccess>(it) }

fun CampingActivity.checkPerformed() =
    result != null && actorUuid != null

fun CampingActivity.isPrepareCampsite() =
    activityId == "prepare-campsite"

fun CampingActivity.isCookMeal() =
    activityId == "cook-meal"

enum class CampingSheetSection : Translatable, ValueEnum {
    PREPARE_CAMPSITE,
    CAMPING_ACTIVITIES,
    EATING;

    override val value: String
        get() = toCamelCase()
    override val i18nKey: String
        get() = "campingSection.$value"
}

private const val playlistUuid = "Playlist.7CiwVus60FiuKFhK"
private const val capital = "Playlist.7CiwVus60FiuKFhK.PlaylistSound.vGP8BFAN2DaZcpri"
private const val firstWorld = "Playlist.7CiwVus60FiuKFhK.PlaylistSound.3iH2HLuQaSIlm0F7"
private const val glenebon = "Playlist.7CiwVus60FiuKFhK.PlaylistSound.O656E1AtGDTH3EU1"
private const val narlmarches = "Playlist.7CiwVus60FiuKFhK.PlaylistSound.b5LKh5J7ZTrfBXVz"
private const val shrikeHills = "Playlist.7CiwVus60FiuKFhK.PlaylistSound.mKDBa58aNaiJA1Fp"
private const val dunsward = "Playlist.7CiwVus60FiuKFhK.PlaylistSound.CrIyaBio4BycqrwO"

private val rolltableUuids = arrayOf(
    "RollTable.44cfq5QJS2O5tn0K",
    "RollTable.Kb4D0vcAcx6EPEFf",
    "RollTable.DMNH6Bn5BfJLV5cB",
    "RollTable.xYVMEePFnF9KoeVO",
    "RollTable.TtuhcaPJyGQkvOQW",
    "RollTable.Oc45l0EFS1EeECYH",
    "RollTable.p3XuOWVRbg0UcSqf",
    "RollTable.Tt4bNzG2wcPOE8vo",
    "RollTable.1OJqyDO2Ws0fQ77v",
    "RollTable.8UtL2oSZjJCcdIHz",
    "RollTable.3Dcalfi2p4jbQxwf",
    "RollTable.cFG8I1fCtU3bhOD9",
    "RollTable.2BPSdXXHvIHrWbL3",
    "RollTable.hhNaJ7HkSIQVYLsq",
    "RollTable.kjXrJUlapDYo9QaJ",
    "RollTable.7brKA7efwUFA5ef0",
    "RollTable.7QW9OYWDh3MfnSF3",
    "RollTable.6Q5WgEnwgfO39ZMz",
    "RollTable.zhxH34Hz1ixX7l4n"
)

fun getDefaultCamping(game: Game): CampingData {
    return CampingData(
        currentRegion = t("camping.zone", recordOf("id" to "00")),
        actorUuids = emptyArray(),
        campingActivities = emptyArray(),
        homebrewCampingActivities = emptyArray(),
        lockedActivities = lockedCampingActivityIds,
        cooking = Cooking(
            actorMeals = emptyArray(),
            knownRecipes = arrayOf("basic-meal", "hearty-meal"),
            homebrewMeals = emptyArray(),
            results = emptyArray(),
            minimumSubsistence = 0,
        ),
        watchSecondsRemaining = 0,
        gunsToClean = 0,
        dailyPrepsAtTime = game.time.worldTimeSeconds,
        encounterModifier = 0,
        restRollMode = "one",
        increaseWatchActorNumber = 0,
        actorUuidsNotKeepingWatch = emptyArray(),
        ignoreSkillRequirements = false,
        randomEncounterRollMode = "gmroll",
        section = "prepareCampsite",
        alwaysPerformActivityIds = emptyArray(),
        restingTrack = null,
        regionSettings = RegionSettings(
            regions = arrayOf(
                RegionSetting(
                    name = t("camping.zone", recordOf("id" to "00")),
                    zoneDc = 14,
                    encounterDc = 12,
                    level = 0,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = shrikeHills),
                    terrain = Terrain.HILLS.toCamelCase(),
                ),
                RegionSetting(
                    name = t("camping.zone", recordOf("id" to "01")),
                    zoneDc = 15,
                    encounterDc = 12,
                    level = 1,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = shrikeHills),
                    terrain = Terrain.PLAINS.toCamelCase(),
                    rollTableUuid = rolltableUuids[0],
                ),
                RegionSetting(
                    name = t("camping.zone", recordOf("id" to "02")),
                    zoneDc = 16,
                    encounterDc = 14,
                    level = 2,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = shrikeHills),
                    terrain = Terrain.FOREST.toCamelCase(),
                    rollTableUuid = rolltableUuids[1],
                ),
                RegionSetting(
                    name = t("camping.zone", recordOf("id" to "03")),
                    zoneDc = 18,
                    encounterDc = 12,
                    level = 3,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = glenebon),
                    terrain = Terrain.FOREST.toCamelCase(),
                    rollTableUuid = rolltableUuids[2],
                ),
                RegionSetting(
                    name = t("camping.zone", recordOf("id" to "04")),
                    zoneDc = 19,
                    encounterDc = 12,
                    level = 4,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = glenebon),
                    terrain = Terrain.HILLS.toCamelCase(),
                    rollTableUuid = rolltableUuids[3],
                ),
                RegionSetting(
                    name = t("camping.zone", recordOf("id" to "05")),
                    zoneDc = 20,
                    encounterDc = 14,
                    level = 5,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = narlmarches),
                    terrain = Terrain.FOREST.toCamelCase(),
                    rollTableUuid = rolltableUuids[4],
                ),
                RegionSetting(
                    name = t("camping.zone", recordOf("id" to "06")),
                    zoneDc = 20,
                    encounterDc = 12,
                    level = 6,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = glenebon),
                    terrain = Terrain.HILLS.toCamelCase(),
                    rollTableUuid = rolltableUuids[5],
                ),
                RegionSetting(
                    name = t("camping.zone", recordOf("id" to "07")),
                    zoneDc = 18,
                    encounterDc = 12,
                    level = 7,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = dunsward),
                    terrain = Terrain.PLAINS.toCamelCase(),
                    rollTableUuid = rolltableUuids[6],
                ),
                RegionSetting(
                    name = t("camping.zone", recordOf("id" to "08")),
                    zoneDc = 24,
                    encounterDc = 12,
                    level = 8,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = dunsward),
                    terrain = Terrain.HILLS.toCamelCase(),
                    rollTableUuid = rolltableUuids[7],
                ),
                RegionSetting(
                    name = t("camping.zone", recordOf("id" to "09")),
                    zoneDc = 28,
                    encounterDc = 16,
                    level = 9,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = dunsward),
                    terrain = Terrain.MOUNTAIN.toCamelCase(),
                    rollTableUuid = rolltableUuids[8],
                ),
                RegionSetting(
                    name = t("camping.zone", recordOf("id" to "10")),
                    zoneDc = 32,
                    encounterDc = 14,
                    level = 10,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = narlmarches),
                    terrain = Terrain.SWAMP.toCamelCase(),
                    rollTableUuid = rolltableUuids[9],
                ),
                RegionSetting(
                    name = t("camping.zone", recordOf("id" to "11")),
                    zoneDc = 28,
                    encounterDc = 12,
                    level = 11,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = glenebon),
                    terrain = Terrain.PLAINS.toCamelCase(),
                    rollTableUuid = rolltableUuids[10],
                ),
                RegionSetting(
                    name = t("camping.zone", recordOf("id" to "12")),
                    zoneDc = 28,
                    encounterDc = 12,
                    level = 12,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = glenebon),
                    terrain = Terrain.HILLS.toCamelCase(),
                    rollTableUuid = rolltableUuids[11],
                ),
                RegionSetting(
                    name = t("camping.zone", recordOf("id" to "13")),
                    zoneDc = 26,
                    encounterDc = 12,
                    level = 13,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = shrikeHills),
                    terrain = Terrain.PLAINS.toCamelCase(),
                    rollTableUuid = rolltableUuids[12],
                ),
                RegionSetting(
                    name = t("camping.zone", recordOf("id" to "14")),
                    zoneDc = 30,
                    encounterDc = 12,
                    level = 14,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = glenebon),
                    terrain = Terrain.PLAINS.toCamelCase(),
                    rollTableUuid = rolltableUuids[13],
                ),
                RegionSetting(
                    name = t("camping.zone", recordOf("id" to "15")),
                    zoneDc = 29,
                    encounterDc = 12,
                    level = 15,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = capital),
                    terrain = Terrain.PLAINS.toCamelCase(),
                    rollTableUuid = rolltableUuids[14],
                ),
                RegionSetting(
                    name = t("camping.zone", recordOf("id" to "16")),
                    zoneDc = 35,
                    encounterDc = 12,
                    level = 16,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = glenebon),
                    terrain = Terrain.HILLS.toCamelCase(),
                    rollTableUuid = rolltableUuids[15],
                ),
                RegionSetting(
                    name = t("camping.zone", recordOf("id" to "17")),
                    zoneDc = 36,
                    encounterDc = 12,
                    level = 17,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = glenebon),
                    terrain = Terrain.HILLS.toCamelCase(),
                    rollTableUuid = rolltableUuids[16],
                ),
                RegionSetting(
                    name = t("camping.zone", recordOf("id" to "18")),
                    zoneDc = 43,
                    encounterDc = 14,
                    level = 18,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = firstWorld),
                    terrain = Terrain.FOREST.toCamelCase(),
                    rollTableUuid = rolltableUuids[17],
                ),
                RegionSetting(
                    name = t("camping.zone", recordOf("id" to "19")),
                    zoneDc = 41,
                    encounterDc = 16,
                    level = 19,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = glenebon),
                    terrain = Terrain.MOUNTAIN.toCamelCase(),
                    rollTableUuid = rolltableUuids[18],
                ),
            ),
        )
    )
}

typealias CampingActor = PF2EParty

fun CampingActor.getCamping(): CampingData? =
    getAppFlag<CampingActor, CampingData?>("camping-sheet")
        ?.let(::deepClone)

suspend fun CampingActor.setCamping(data: CampingData) {
    setAppFlag("camping-sheet", data)
}

suspend fun CampingActor.clearCamping() {
    unsetAppFlag("camping-sheet")
}

fun Game.getCampingActors(): List<CampingActor> =
    actors.contents
        .filterIsInstance<CampingActor>()
        .filter { it.getCamping() != null }

fun Game.getActiveCamping(): CampingData? {
    val campingActors = getCampingActors()
    return campingActors.firstOrNull()?.getCamping() ?: campingActors
        .mapNotNull { it.getCamping() }
        .firstOrNull { it.isActive == true }
}

fun CampingData.canPerformActivities(): Boolean {
    val prepareCampResult = campingActivities
        .find { it.isPrepareCampsite() }
        ?.result
        ?.let { fromCamelCase<DegreeOfSuccess>(it) }
    return prepareCampResult != null && prepareCampResult != DegreeOfSuccess.CRITICAL_FAILURE
}

fun CampingData.findCurrentRegion(): RegionSetting? =
    regionSettings.regions.find { it.name == currentRegion }


sealed interface MealChoice {
    val actor: PF2ECharacter
    val favoriteMeal: RecipeData?
    val name: String
    val cookingCost: FoodAmount
    val id: String

    data class Nothing(
        override val actor: PF2ECharacter,
        override val favoriteMeal: RecipeData?
    ) : MealChoice {
        override val name = "nothing"
        override val id = "nothing"
        override val cookingCost = FoodAmount()
    }

    data class Rations(
        override val actor: PF2ECharacter,
        override val favoriteMeal: RecipeData?,
    ) : MealChoice {
        override val id: String = "rationsOrSubsistence"
        override val name: String = "rationsOrSubsistence"
        override val cookingCost = FoodAmount(rations = 1)
    }

    data class ParsedMeal(
        override val actor: PF2ECharacter,
        val recipe: RecipeData,
        override val favoriteMeal: RecipeData?,
    ) : MealChoice {
        override val id = recipe.id
        override val name: String = recipe.name
        override val cookingCost = recipe.cookingCost()
    }
}

data class ParsedMeals(
    val cook: PF2ECharacter?,
    val skills: List<Attribute>,
    val results: List<ParsedRecipeResult>,
    val meals: List<MealChoice>,
)

data class ParsedRecipeResult(
    val recipe: RecipeData,
    val selectedSkill: Attribute,
    val degreeOfSuccess: DegreeOfSuccess?,
) {
    val dc
        get() = when (selectedSkill) {
            Skill.SURVIVAL -> recipe.survivalDC
            else -> recipe.cookingLoreDC
        }
}

private fun parseMealChoices(
    camping: CampingData,
    charactersInCamp: Map<String, PF2ECharacter>,
    recipesById: Map<String, RecipeData>
): List<MealChoice> {
    val chosenMeals = camping.cooking.actorMeals.mapNotNull { meal ->
        charactersInCamp[meal.actorUuid]?.let { actor ->
            val favoriteMeal = meal.favoriteMeal?.let { recipesById[it] }
            when (val chosenMeal = meal.chosenMeal) {
                "nothing" -> MealChoice.Nothing(actor = actor, favoriteMeal = favoriteMeal)
                "rationsOrSubsistence" -> MealChoice.Rations(actor = actor, favoriteMeal = favoriteMeal)
                else -> recipesById[chosenMeal]?.let { recipe ->
                    MealChoice.ParsedMeal(
                        actor = actor,
                        recipe = recipe,
                        favoriteMeal = favoriteMeal,
                    )
                }
            }
        }
    }
    return chosenMeals
}

fun CampingData.findCookingChoices(
    charactersInCampByUuid: Map<String, PF2ECharacter>,
    recipesById: Map<String, RecipeData>,
): ParsedMeals {
    val cook = campingActivities
        .find { it.isCookMeal() && it.actorUuid != null }
        ?.let { charactersInCampByUuid[it.actorUuid] }
        ?.takeIf {
            campingActivities.any {
                it.isPrepareCampsite()
                        && it.parseResult() != null
                        && it.parseResult() != DegreeOfSuccess.CRITICAL_FAILURE
            }
        }
    val cookingLore = Lore("cooking")
    val cookingSkills: List<Attribute> = if (cook == null) {
        listOf(Skill.SURVIVAL, cookingLore)
    } else {
        listOfNotNull(Skill.SURVIVAL, if (cook.hasAttribute(cookingLore)) cookingLore else null)
    }
    val mealChoices = parseMealChoices(this, charactersInCampByUuid, recipesById)
    val resultsByRecipeId = cooking.results.associateBy { it.recipeId }
    val effectiveMealChoices = mealChoices.map {
        if (cook == null && it is MealChoice.ParsedMeal) {
            MealChoice.Rations(
                actor = it.actor,
                favoriteMeal = it.favoriteMeal,
            )
        } else {
            it
        }
    }
    return ParsedMeals(
        cook = cook,
        skills = cookingSkills,
        meals = effectiveMealChoices,
        results = recipesById.values.map { recipe ->
            val result: CookingResult = resultsByRecipeId[recipe.id] ?: CookingResult(
                recipeId = recipe.id,
                result = null,
                skill = "survival",
            )
            ParsedRecipeResult(
                recipe = recipe,
                selectedSkill = Attribute.fromString(result.skill)
                    .takeIf { it in cookingSkills }
                    ?: Skill.SURVIVAL,
                degreeOfSuccess = result.result?.let { fromCamelCase<DegreeOfSuccess>(it) },
            )
        }
    )
}