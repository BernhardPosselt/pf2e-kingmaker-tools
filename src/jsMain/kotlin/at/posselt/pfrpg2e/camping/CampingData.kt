package at.posselt.pfrpg2e.camping

import at.posselt.pfrpg2e.Config
import at.posselt.pfrpg2e.actor.hasAttribute
import at.posselt.pfrpg2e.actor.party
import at.posselt.pfrpg2e.camping.dialogs.Track
import at.posselt.pfrpg2e.camping.dialogs.RegionSetting
import at.posselt.pfrpg2e.camping.dialogs.RegionSettings
import at.posselt.pfrpg2e.data.actor.Attribute
import at.posselt.pfrpg2e.data.actor.Lore
import at.posselt.pfrpg2e.data.actor.Skill
import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.data.regions.Terrain
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.utils.getAppFlag
import at.posselt.pfrpg2e.utils.setAppFlag
import com.foundryvtt.core.Game
import com.foundryvtt.core.utils.deepClone
import com.foundryvtt.pf2e.actor.PF2EActor
import com.foundryvtt.pf2e.actor.PF2ECharacter
import com.foundryvtt.pf2e.actor.PF2ENpc
import com.foundryvtt.pf2e.actor.PF2EParty
import js.array.toTypedArray
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
    val recipeName: String
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
    var activity: String
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
    var alwaysPerformActivities: Array<String>
    var huntAndGatherTargetActorUuid: String?
    var proxyRandomEncounterTableUuid: String?
    var randomEncounterRollMode: String
    var ignoreSkillRequirements: Boolean
    var minimumTravelSpeed: Int?
    var regionSettings: RegionSettings
    var section: String
    var restingTrack: Track?
    var worldSceneId: String?
}

suspend fun CampingData.getActorsCarryingFood(game: Game): List<PF2EActor> =
    getActorsInCamp() + listOfNotNull(game.party())


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
    activity == "Prepare Campsite"

fun CampingActivity.isCookMeal() =
    activity == "Cook Meal"

enum class CampingSheetSection {
    PREPARE_CAMPSITE,
    CAMPING_ACTIVITIES,
    EATING,
}

private val playlistUuid = "Playlist.7CiwVus60FiuKFhK"
private val capital = "Playlist.7CiwVus60FiuKFhK.PlaylistSound.vGP8BFAN2DaZcpri"
private val firstWorld = "Playlist.7CiwVus60FiuKFhK.PlaylistSound.3iH2HLuQaSIlm0F7"
private val glenebon = "Playlist.7CiwVus60FiuKFhK.PlaylistSound.O656E1AtGDTH3EU1"
private val narlmarches = "Playlist.7CiwVus60FiuKFhK.PlaylistSound.b5LKh5J7ZTrfBXVz"
private val shrikeHills = "Playlist.7CiwVus60FiuKFhK.PlaylistSound.mKDBa58aNaiJA1Fp"
private val dunsward = "Playlist.7CiwVus60FiuKFhK.PlaylistSound.CrIyaBio4BycqrwO"

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
        currentRegion = Config.regions.defaultRegion,
        actorUuids = emptyArray(),
        campingActivities = emptyArray(),
        homebrewCampingActivities = emptyArray(),
        lockedActivities = campingActivityData
            .filter(CampingActivityData::isLocked)
            .map(CampingActivityData::name)
            .toTypedArray(),
        cooking = Cooking(
            actorMeals = emptyArray(),
            knownRecipes = arrayOf("Basic Meal", "Hearty Meal"),
            homebrewMeals = emptyArray(),
            results = emptyArray(),
            minimumSubsistence = 0,
        ),
        watchSecondsRemaining = 0,
        gunsToClean = 0,
        dailyPrepsAtTime = game.time.worldTime,
        encounterModifier = 0,
        restRollMode = "one",
        increaseWatchActorNumber = 0,
        actorUuidsNotKeepingWatch = emptyArray(),
        ignoreSkillRequirements = false,
        randomEncounterRollMode = "gmroll",
        section = "prepareCampsite",
        alwaysPerformActivities = emptyArray(),
        restingTrack = null,
        regionSettings = RegionSettings(
            regions = arrayOf(
                RegionSetting(
                    name = "Zone 00",
                    zoneDc = 14,
                    encounterDc = 12,
                    level = 0,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = shrikeHills),
                    terrain = Terrain.HILLS.toCamelCase(),
                ),
                RegionSetting(
                    name = "Zone 01",
                    zoneDc = 15,
                    encounterDc = 12,
                    level = 1,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = shrikeHills),
                    terrain = Terrain.PLAINS.toCamelCase(),
                    rollTableUuid = rolltableUuids[0],
                ),
                RegionSetting(
                    name = "Zone 02",
                    zoneDc = 16,
                    encounterDc = 14,
                    level = 2,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = shrikeHills),
                    terrain = Terrain.FOREST.toCamelCase(),
                    rollTableUuid = rolltableUuids[1],
                ),
                RegionSetting(
                    name = "Zone 03",
                    zoneDc = 18,
                    encounterDc = 12,
                    level = 3,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = glenebon),
                    terrain = Terrain.FOREST.toCamelCase(),
                    rollTableUuid = rolltableUuids[2],
                ),
                RegionSetting(
                    name = "Zone 04",
                    zoneDc = 19,
                    encounterDc = 12,
                    level = 4,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = glenebon),
                    terrain = Terrain.HILLS.toCamelCase(),
                    rollTableUuid = rolltableUuids[3],
                ),
                RegionSetting(
                    name = "Zone 05",
                    zoneDc = 20,
                    encounterDc = 14,
                    level = 5,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = narlmarches),
                    terrain = Terrain.FOREST.toCamelCase(),
                    rollTableUuid = rolltableUuids[4],
                ),
                RegionSetting(
                    name = "Zone 06",
                    zoneDc = 20,
                    encounterDc = 12,
                    level = 6,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = glenebon),
                    terrain = Terrain.HILLS.toCamelCase(),
                    rollTableUuid = rolltableUuids[5],
                ),
                RegionSetting(
                    name = "Zone 07",
                    zoneDc = 18,
                    encounterDc = 12,
                    level = 7,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = dunsward),
                    terrain = Terrain.PLAINS.toCamelCase(),
                    rollTableUuid = rolltableUuids[6],
                ),
                RegionSetting(
                    name = "Zone 08",
                    zoneDc = 24,
                    encounterDc = 12,
                    level = 8,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = dunsward),
                    terrain = Terrain.HILLS.toCamelCase(),
                    rollTableUuid = rolltableUuids[7],
                ),
                RegionSetting(
                    name = "Zone 09",
                    zoneDc = 28,
                    encounterDc = 16,
                    level = 9,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = dunsward),
                    terrain = Terrain.MOUNTAIN.toCamelCase(),
                    rollTableUuid = rolltableUuids[8],
                ),
                RegionSetting(
                    name = "Zone 10",
                    zoneDc = 32,
                    encounterDc = 14,
                    level = 10,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = narlmarches),
                    terrain = Terrain.SWAMP.toCamelCase(),
                    rollTableUuid = rolltableUuids[9],
                ),
                RegionSetting(
                    name = "Zone 11",
                    zoneDc = 28,
                    encounterDc = 12,
                    level = 11,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = glenebon),
                    terrain = Terrain.PLAINS.toCamelCase(),
                    rollTableUuid = rolltableUuids[10],
                ),
                RegionSetting(
                    name = "Zone 12",
                    zoneDc = 28,
                    encounterDc = 12,
                    level = 12,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = glenebon),
                    terrain = Terrain.HILLS.toCamelCase(),
                    rollTableUuid = rolltableUuids[11],
                ),
                RegionSetting(
                    name = "Zone 13",
                    zoneDc = 26,
                    encounterDc = 12,
                    level = 13,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = shrikeHills),
                    terrain = Terrain.PLAINS.toCamelCase(),
                    rollTableUuid = rolltableUuids[12],
                ),
                RegionSetting(
                    name = "Zone 14",
                    zoneDc = 30,
                    encounterDc = 12,
                    level = 14,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = glenebon),
                    terrain = Terrain.PLAINS.toCamelCase(),
                    rollTableUuid = rolltableUuids[13],
                ),
                RegionSetting(
                    name = "Zone 15",
                    zoneDc = 29,
                    encounterDc = 12,
                    level = 15,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = capital),
                    terrain = Terrain.PLAINS.toCamelCase(),
                    rollTableUuid = rolltableUuids[14],
                ),
                RegionSetting(
                    name = "Zone 16",
                    zoneDc = 35,
                    encounterDc = 12,
                    level = 16,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = glenebon),
                    terrain = Terrain.HILLS.toCamelCase(),
                    rollTableUuid = rolltableUuids[15],
                ),
                RegionSetting(
                    name = "Zone 17",
                    zoneDc = 36,
                    encounterDc = 12,
                    level = 17,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = glenebon),
                    terrain = Terrain.HILLS.toCamelCase(),
                    rollTableUuid = rolltableUuids[16],
                ),
                RegionSetting(
                    name = "Zone 18",
                    zoneDc = 43,
                    encounterDc = 14,
                    level = 18,
                    combatTrack = Track(playlistUuid = playlistUuid, trackUuid = firstWorld),
                    terrain = Terrain.FOREST.toCamelCase(),
                    rollTableUuid = rolltableUuids[17],
                ),
                RegionSetting(
                    name = "Zone 19",
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

fun PF2ENpc.getCamping(): CampingData? =
    getAppFlag<PF2ENpc, CampingData?>("camping-sheet")
        ?.let(::deepClone)

suspend fun PF2ENpc.setCamping(data: CampingData) {
    setAppFlag("camping-sheet", data)
}

fun Game.getCampingActor(): PF2ENpc? =
    actors.contents
        .filterIsInstance<PF2ENpc>()
        .find { it.getCamping() != null }

fun CampingData.getAllActivities(): Array<CampingActivityData> {
    val homebrewNames = homebrewCampingActivities.map { it.name }.toSet()
    return campingActivityData
        .filter { it.name !in homebrewNames }
        .toTypedArray() + homebrewCampingActivities
}

fun CampingData.getAllRecipes(): Array<RecipeData> {
    val homebrewNames = cooking.homebrewMeals.map { it.name }.toSet()
    return recipes
        .filter { it.name !in homebrewNames }
        .toTypedArray() + cooking.homebrewMeals
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

fun Cooking.findCookingResult(recipeName: String): CookingResult =
    results.find { it.recipeName == recipeName } ?: CookingResult(
        recipeName = recipeName,
        result = null,
        skill = "survival",
    )

sealed interface MealChoice {
    val actor: PF2ECharacter
    val favoriteMeal: RecipeData?
    val name: String
    val cookingCost: FoodAmount

    data class Nothing(
        override val actor: PF2ECharacter,
        override val favoriteMeal: RecipeData?
    ) : MealChoice {
        override val name = "nothing"
        override val cookingCost = FoodAmount()
    }

    data class Rations(
        override val actor: PF2ECharacter,
        override val favoriteMeal: RecipeData?,
    ) : MealChoice {
        override val name: String = "rationsOrSubsistence"
        override val cookingCost = FoodAmount(rations = 1)
    }

    data class ParsedMeal(
        override val actor: PF2ECharacter,
        val recipe: RecipeData,
        override val favoriteMeal: RecipeData?,
    ) : MealChoice {
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
    recipes: Map<String, RecipeData>
): List<MealChoice> {
    val chosenMeals = camping.cooking.actorMeals.mapNotNull { meal ->
        charactersInCamp[meal.actorUuid]?.let { actor ->
            val favoriteMeal = meal.favoriteMeal?.let { recipes[it] }
            when (val chosenMeal = meal.chosenMeal) {
                "nothing" -> MealChoice.Nothing(actor = actor, favoriteMeal = favoriteMeal)
                "rationsOrSubsistence" -> MealChoice.Rations(actor = actor, favoriteMeal = favoriteMeal)
                else -> recipes[chosenMeal]?.let { recipe ->
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
    recipesByName: Map<String, RecipeData>,
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
    val mealChoices = parseMealChoices(this, charactersInCampByUuid, recipesByName)
    val resultsByRecipeName = cooking.results.associateBy { it.recipeName }
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
        results = recipesByName.values.map { recipe ->
            val result: CookingResult = resultsByRecipeName[recipe.name] ?: CookingResult(
                recipeName = recipe.name,
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

// -------------------------------------------------------------------------------------------
// unused but begin of refactoring the current state
data class ParsedCampingActivity(
    val name: String,
    val result: DegreeOfSuccess?,
    val actor: PF2ECharacter?,
    val selectedSkill: ParsedCampingSkill?,
    val skills: List<ParsedCampingSkill>,
    val enabled: Boolean,
    val alwaysEnabled: Boolean,
) {
    val requiresACheck
        get() = skills.any { !it.validateOnly }
    val isPrepareCampsite
        get() = name == "Prepare Campsite"
}

class ParsedCamping(
    actorsInCamp: List<PF2EActor>,
    activities: List<ParsedCampingActivity>,
    var section: CampingSheetSection,
    chosenMeals: List<MealChoice>,
    val recipes: List<RecipeData>,
    var gunsToClean: Int,
    var increaseWatchActorNumber: Int,
    var watchSecondsRemaining: Int,
    actorsNotKeepingWatch: Set<String>,
    knownRecipes: Set<String>,
    val regions: List<RegionSetting>,
    currentRegion: String,
    val ignoreSkillRequirements: Boolean,
) {
    private var _knownRecipes = knownRecipes.toMutableSet()
    private var _actorsNotKeepingWatch = actorsNotKeepingWatch.toMutableSet()
    private var _actorsInCamp = actorsInCamp.toMutableList()
    private var _chosenMeals = chosenMeals.toMutableList()
    private var _activities = activities.associateBy { it.name }.toMutableMap()
    private var _currentRegion = currentRegion

    val activities
        get() = _activities.values.toList()
    val actorsKeepingWatch
        get() = _actorsInCamp.filterIsInstance<PF2ECharacter>()
            .filter { it.uuid !in _actorsNotKeepingWatch }
            .toList()
    val prepareCampsite: ParsedCampingActivity
        get() = _activities["Prepare Campsite"]!!
    val cookMeal: ParsedCampingActivity
        get() = _activities["Cook Meal"]!!
    val actorsInCamp
        get() = _actorsInCamp.toList()
    val chosenMeals
        get() = _chosenMeals.toList()
    val knownRecipes
        get() = _knownRecipes.toSet()
    val currentRegion
        get() = regions.find { it.name == _currentRegion }!!

    fun removeRecipe(name: String) {
        val affected = _chosenMeals
            .filterIsInstance<MealChoice.ParsedMeal>()
            .filter { it.recipe.name == name }
        _chosenMeals.removeAll(affected)
        _chosenMeals.addAll(affected.map { MealChoice.Nothing(actor = it.actor, favoriteMeal = it.favoriteMeal) })
        _knownRecipes.remove(name)
    }

    fun removeActorByUuid(actorUuid: String) {
        _actorsInCamp.removeAll { it.uuid == actorUuid }
        _chosenMeals.removeAll { it.actor.uuid == actorUuid }
        _actorsNotKeepingWatch.remove(actorUuid)
        _activities
            .filter { it.value.actor?.uuid == actorUuid }
            .forEach { _activities.remove(it.key) }
    }

    fun addActor(actor: PF2EActor) {
        _actorsInCamp.add(actor)
    }

    suspend fun getTotalCarriedFood(
        party: PF2EParty?,
        foodItems: FoodItems,
    ): FoodAmount = coroutineScope {
        val actors = _actorsInCamp + (party?.let { listOf(it) } ?: emptyList())
        actors.map {
            it.getTotalCarriedFood(foodItems = foodItems)
        }.sum()
    }
}

suspend fun PF2ENpc.getParsedCamping(game: Game): ParsedCamping? {
    val camping = getCamping() ?: return null
    val actorsInCamp = camping.getActorsInCamp()
    val charactersInCamp = actorsInCamp.filterIsInstance<PF2ECharacter>().associateBy { it.uuid }
    val activitiesByName = camping.campingActivities.associateBy { it.activity }
    val recipes = camping.getAllRecipes().associateBy { it.name }
    val lockedActivities = camping.lockedActivities.toSet()
    val alwaysPerformActivities = camping.alwaysPerformActivities.toSet()
    val activities = camping.getAllActivities()
        .asSequence()
        .sortedBy { it.name }
        .map { data ->
            val activity: CampingActivity? = activitiesByName[data.name] ?: CampingActivity(
                activity = data.name,
                actorUuid = null,
                result = null,
                selectedSkill = null,
            )
            val actor = activity?.let { charactersInCamp[it.actorUuid] }
            val skills = data.getCampingSkills(actor)
            val selectedSkill = activity?.selectedSkill?.let { skillName ->
                val attribute = Attribute.fromString(skillName)
                skills.find { it.attribute == attribute }
            }
            ParsedCampingActivity(
                name = data.name,
                actor = actor,
                result = activity?.let { it.result?.let { degree -> fromCamelCase<DegreeOfSuccess>(degree) } },
                selectedSkill = selectedSkill,
                enabled = data.name !in lockedActivities,
                alwaysEnabled = data.name in alwaysPerformActivities,
                skills = skills,
            )
        }
        .toList()
    val chosenMeals = parseMealChoices(camping, charactersInCamp, recipes)
    return ParsedCamping(
        actorsInCamp = actorsInCamp,
        activities = activities,
        section = fromCamelCase<CampingSheetSection>(camping.section) ?: CampingSheetSection.PREPARE_CAMPSITE,
        chosenMeals = chosenMeals,
        recipes = camping.getAllRecipes().toList(),
        gunsToClean = camping.gunsToClean,
        increaseWatchActorNumber = camping.increaseWatchActorNumber,
        watchSecondsRemaining = camping.watchSecondsRemaining,
        actorsNotKeepingWatch = camping.actorUuidsNotKeepingWatch.toMutableSet(),
        knownRecipes = camping.cooking.knownRecipes.toMutableSet(),
        regions = camping.regionSettings.regions.toList(),
        currentRegion = camping.currentRegion,
        ignoreSkillRequirements = camping.ignoreSkillRequirements
    )
}

suspend fun PF2ENpc.setParsedCamping(data: ParsedCamping) {
    val camping = getCamping() ?: return
    camping.section = data.section.toCamelCase()
    camping.campingActivities = data.activities.map {
        CampingActivity(
            activity = it.name,
            actorUuid = it.actor?.uuid,
            result = it.result?.toCamelCase(),
            selectedSkill = it.selectedSkill?.attribute?.value,
        )
    }.toTypedArray()
    camping.actorUuids = data.actorsInCamp.map { it.uuid }.toTypedArray()
    camping.cooking.actorMeals = data.chosenMeals.map {
        ActorMeal(
            actorUuid = it.actor.uuid,
            favoriteMeal = it.favoriteMeal?.name,
            chosenMeal = it.name,
        )
    }.toTypedArray()
    val charactersKeepingWatch = data.actorsKeepingWatch.map { it.uuid }.toSet()
    camping.actorUuidsNotKeepingWatch = data.actorsInCamp
        .asSequence()
        .filterIsInstance<PF2ECharacter>()
        .filter { it.uuid !in charactersKeepingWatch }
        .map { it.uuid }
        .toTypedArray()
    camping.increaseWatchActorNumber = data.increaseWatchActorNumber
    camping.gunsToClean = data.gunsToClean
    camping.watchSecondsRemaining = data.watchSecondsRemaining
    camping.cooking.knownRecipes = data.knownRecipes.toTypedArray()
    camping.currentRegion = data.currentRegion.name
    camping.ignoreSkillRequirements = data.ignoreSkillRequirements
    camping.lockedActivities = data.activities.filter { !it.enabled }.map { it.name }.toTypedArray()
    camping.alwaysPerformActivities = data.activities.filter { it.alwaysEnabled }.map { it.name }.toTypedArray()
    setCamping(camping)
}