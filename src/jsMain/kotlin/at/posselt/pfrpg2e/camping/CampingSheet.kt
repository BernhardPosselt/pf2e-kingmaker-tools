package at.posselt.pfrpg2e.camping

import at.posselt.pfrpg2e.actions.ActionDispatcher
import at.posselt.pfrpg2e.actions.ActionMessage
import at.posselt.pfrpg2e.actions.handlers.ClearMealEffectsMessage
import at.posselt.pfrpg2e.actions.handlers.OpenCampingSheetAction
import at.posselt.pfrpg2e.actor.openActor
import at.posselt.pfrpg2e.app.ActorRef
import at.posselt.pfrpg2e.app.DocumentRef
import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.MenuControl
import at.posselt.pfrpg2e.app.ValidatedHandlebarsContext
import at.posselt.pfrpg2e.app.confirm
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.app.forms.toOption
import at.posselt.pfrpg2e.calculateHexplorationActivities
import at.posselt.pfrpg2e.camping.dialogs.CampingSettingsApplication
import at.posselt.pfrpg2e.camping.dialogs.ConfirmWatchApplication
import at.posselt.pfrpg2e.camping.dialogs.FavoriteMealsApplication
import at.posselt.pfrpg2e.camping.dialogs.ManageActivitiesApplication
import at.posselt.pfrpg2e.camping.dialogs.ManageRecipesApplication
import at.posselt.pfrpg2e.camping.dialogs.RegionConfig
import at.posselt.pfrpg2e.camping.dialogs.pickSpecialRecipe
import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.resting.EIGHT_HOURS_SECONDS
import at.posselt.pfrpg2e.resting.getTotalRestDuration
import at.posselt.pfrpg2e.resting.rest
import at.posselt.pfrpg2e.takeIfInstance
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.utils.asSequence
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.formatSeconds
import at.posselt.pfrpg2e.utils.fromDateInputString
import at.posselt.pfrpg2e.utils.fromUuidTypeSafe
import at.posselt.pfrpg2e.utils.getPF2EWorldTime
import at.posselt.pfrpg2e.utils.isDay
import at.posselt.pfrpg2e.utils.launch
import at.posselt.pfrpg2e.utils.openItem
import at.posselt.pfrpg2e.utils.openJournal
import at.posselt.pfrpg2e.utils.postChatMessage
import at.posselt.pfrpg2e.utils.t
import at.posselt.pfrpg2e.utils.toDateInputString
import at.posselt.pfrpg2e.utils.toMutableRecord
import com.foundryvtt.core.Game
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.documents.onCreateItem
import com.foundryvtt.core.documents.onDeleteItem
import com.foundryvtt.core.documents.onUpdateItem
import com.foundryvtt.core.helpers.onUpdateWorldTime
import com.foundryvtt.core.ui
import com.foundryvtt.pf2e.actor.PF2EActor
import com.foundryvtt.pf2e.actor.PF2ECharacter
import com.foundryvtt.pf2e.actor.PF2ECreature
import com.foundryvtt.pf2e.item.itemFromUuid
import js.array.component1
import js.array.component2
import js.core.Void
import js.objects.Object
import js.objects.ReadonlyRecord
import js.objects.recordOf
import kotlinx.coroutines.await
import kotlinx.datetime.LocalTime
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.js.Promise
import kotlin.math.max


@JsPlainObject
external interface BaseActorContext {
    val name: String
    val uuid: String
    val image: String?
}

@Suppress("unused")
@JsPlainObject
external interface CampingSheetActor : BaseActorContext {
    val choseActivity: Boolean
    val degreeOfSuccess: FormElementContext?
    val chosenMealImg: String?
    val chosenMeal: String?
}

@Suppress("unused")
@JsPlainObject
external interface CampingSheetActivity {
    val id: String
    val journalUuid: String?
    val actor: CampingSheetActor?
    val name: String
    val hidden: Boolean
    val requiresCheck: Boolean
    val secret: Boolean
    val skills: FormElementContext?
}

fun CampingSheetActivity.isPrepareCampsite() = id == "prepare-campsite"

@Suppress("unused")
@JsPlainObject
external interface NightModes {
    val retract2: Boolean
    val retract1: Boolean
    val retractHex: Boolean
    val time: Boolean
    val advanceHex: Boolean
    val advance1: Boolean
    val advance2: Boolean
    val rest: Boolean
    val travelMode: Boolean
}

@Suppress("unused")
@JsPlainObject
external interface RecipeActorContext : BaseActorContext {
    val chosenMeal: String
    val favoriteMeal: String?
}

@Suppress("unused")
@JsPlainObject
external interface RecipeContext {
    val name: String
    val targetRecipe: String
    val cost: FoodCost?
    val uuid: String?
    val icon: String
    val requiresCheck: Boolean
    val hidden: Boolean
    val rations: Boolean
    val consumeRationsEnabled: Boolean
    val actors: Array<RecipeActorContext>
    val skills: FormElementContext?
    val degreeOfSuccess: FormElementContext?
}

@Suppress("unused")
@JsPlainObject
external interface CampingSheetContext : ValidatedHandlebarsContext {
    var actors: Array<CampingSheetActor>
    var prepareCamp: CampingSheetActivity?
    var activities: Array<CampingSheetActivity>
    var isDay: Boolean
    var isGM: Boolean
    var time: String
    var terrain: String
    var pxTimeOffset: Int
    var night: NightModes
    var hexplorationActivityDuration: String
    var hexplorationActivitiesAvailable: Int
    var hexplorationActivitiesMax: String
    var adventuringFor: String
    var travelingFor: String
    var restDuration: String
    var restDurationLeft: String?
    var encounterDc: Int
    var region: FormElementContext
    var section: String
    var prepareCampSection: Boolean
    var campingActivitiesSection: Boolean
    var eatingSection: Boolean
    var travelMode: FormElementContext
    var recipes: Array<RecipeContext>
    var totalFoodCost: FoodCost
    var availableFood: FoodCost
    var canRollEncounter: Boolean
}

@JsPlainObject
external interface CampingSheetActivitiesFormData {
    val degreeOfSuccess: ReadonlyRecord<String, String?>?
    val selectedSkill: ReadonlyRecord<String, String?>?
}

@JsPlainObject
external interface RecipeFormData {
    val selectedSkill: ReadonlyRecord<String, String?>?
    val degreeOfSuccess: ReadonlyRecord<String, String?>?
}

@JsPlainObject
external interface CampingSheetFormData {
    val region: String
    val activities: CampingSheetActivitiesFormData
    val recipes: RecipeFormData?
    val travelModeActive: Boolean

}

private fun isNightMode(
    now: LocalTime,
    visibleAfter: String,
    visibleBefore: String,
): Boolean {
    val start = LocalTime.fromDateInputString(visibleAfter)
    val end = LocalTime.fromDateInputString(visibleBefore)
    return if (start > end) {
        !((now < end) || (now > start))
    } else {
        !((start < now) && (now < end))
    }
}

private fun calculateNightModes(time: LocalTime): NightModes {
    return NightModes(
        travelMode = isNightMode(time, "16:00", "05:30"),
        retract2 = isNightMode(time, "10:00", "23:00"),
        retract1 = isNightMode(time, "09:00", "22:00"),
        retractHex = isNightMode(time, "08:00", "21:00"),
        time = isNightMode(time, "06:00", "19:00"),
        advanceHex = isNightMode(time, "04:00", "17:00"),
        advance1 = isNightMode(time, "03:00", "16:00"),
        advance2 = isNightMode(time, "02:00", "15:00"),
        rest = isNightMode(time, "19:00", "08:00"),
    )
}

private const val windowWidth = 970


@JsName("CampingSheet")
class CampingSheet(
    private val game: Game,
    private val actor: CampingActor,
    private val dispatcher: ActionDispatcher,
) : FormApp<CampingSheetContext, CampingSheetFormData>(
    title = t("applications.camping"),
    template = "applications/camping/camping-sheet.hbs",
    id = "kmCamping-${actor.uuid}",
    width = windowWidth,
    dataModel = CampingSheetDataModel::class.js,
    classes = setOf("km-camping-sheet"),
    controls = arrayOf(
        MenuControl(label = t("camping.showPlayers"), action = "show-players", gmOnly = true),
        MenuControl(label = t("camping.resetActivities"), action = "reset-activities", gmOnly = true),
        MenuControl(label = t("camping.resetMeals"), action = "reset-meals", gmOnly = true),
        MenuControl(label = t("camping.favoriteMeals"), action = "favorite-meals", gmOnly = false),
        MenuControl(label = t("camping.activities"), action = "configure-activities", gmOnly = true),
        MenuControl(label = t("camping.recipes"), action = "configure-recipes", gmOnly = true),
        MenuControl(label = t("camping.regions"), action = "configure-regions", gmOnly = true),
        MenuControl(label = t("applications.settings"), action = "settings", gmOnly = true),
        MenuControl(label = t("applications.quickstart"), action = "quickstart", gmOnly = true),
        MenuControl(label = t("applications.help"), action = "help"),
    ),
    scrollable = setOf(".km-camping-activities-wrapper", ".km-camping-actors"),
    syncedDocument = actor,
    debug = true,
) {
    init {
        onDocumentRefDragstart(".km-camping-actor")
        onDocumentRefDragstart(".km-recipe-actor")
        onDocumentRefDrop(".km-camping-add-actor") { _, documentRef ->
            if (documentRef is ActorRef) {
                buildPromise {
                    addActor(documentRef.uuid)
                }
            }
        }
        onDocumentRefDrop(
            ".km-camping-actor",
            { it.type == "Item" }
        ) { event, documentRef ->
            buildPromise {
                val target = event.target as HTMLElement
                val tile = target.closest(".km-camping-actor") as HTMLElement?
                val actor = tile?.dataset?.get("uuid")
                    ?.let { fromUuidTypeSafe<PF2EActor>(it) }
                if (actor != null) {
                    addItemToActor(documentRef, actor)
                }
            }
        }

        onDocumentRefDrop(
            ".km-camping-activity",
            { it.dragstartSelector == ".km-camping-actor" || it.type == "Item" }
        ) { event, documentRef ->
            buildPromise {
                val target = event.target as HTMLElement
                val tile = target.closest(".km-camping-activity") as HTMLElement?
                val actor = tile?.dataset?.get("actorUuid")
                    ?.let { fromUuidTypeSafe<PF2EActor>(it) }
                val activityId = tile?.dataset?.get("activityId")
                if (documentRef is ActorRef && activityId != null) {
                    assignActivityTo(documentRef.uuid, activityId)
                } else if (actor != null && activityId != null) {
                    addItemToActor(documentRef, actor)
                }
            }
        }
        onDocumentRefDrop(
            ".km-camping-recipe",
            { it.dragstartSelector == ".km-camping-actor" || it.dragstartSelector == ".km-recipe-actor" }
        ) { event, documentRef ->
            buildPromise {
                val target = event.target as HTMLElement
                val tile = target.closest(".km-camping-recipe") as HTMLElement?
                val recipeId = tile?.dataset?.get("recipeId")
                if (documentRef is ActorRef && recipeId != null) {
                    assignRecipeTo(documentRef.uuid, recipeId)
                }
            }
        }
        appHook.onUpdateWorldTime { _, _, _, _ -> render() }
        appHook.onCreateItem { _, _, _ -> render() }
        appHook.onDeleteItem { _, _, _ -> render() }
        appHook.onUpdateItem { _, _, _, _ -> render() }
    }

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "configure-regions" -> RegionConfig(actor).launch()
            "configure-recipes" -> ManageRecipesApplication(game, actor).launch()
            "configure-activities" -> ManageActivitiesApplication(game, actor).launch()
            "reset-activities" -> buildPromise {
                if (confirm(t("camping.confirmActivityReset"))) {
                    resetActivities()
                }
            }

            "reset-meals" -> buildPromise {
                if (confirm(t("camping.confirmMealReset"))) {
                    resetMeals()
                }
            }

            "settings" -> CampingSettingsApplication(game, actor).launch()
            "rest" -> buildPromise {
                actor.getCamping()?.let { camping ->
                    if (camping.watchSecondsRemaining > 0) {
                        // continue watch
                        buildPromise {
                            rest(
                                game = game,
                                dispatcher = dispatcher,
                                campingActor = actor,
                                camping = camping,
                                skipWatch = false,
                                skipDailyPreparations = false,
                                disableRandomEncounter = false,
                                skipWeather = camping.restSettings.skipWeather,
                                party = actor,
                            )
                        }
                    } else {
                        ConfirmWatchApplication(
                            camping = camping,
                            game = game,
                        ) { enableWatch, enableDailyPreparations, checkRandomEncounter, checkWeather ->
                            buildPromise {
                                rest(
                                    game = game,
                                    dispatcher = dispatcher,
                                    campingActor = actor,
                                    camping = camping,
                                    skipWatch = !enableWatch,
                                    skipDailyPreparations = !enableDailyPreparations,
                                    disableRandomEncounter = !checkRandomEncounter,
                                    skipWeather = !checkWeather,
                                    party = actor,
                                )
                            }
                        }.launch()
                    }
                }
            }

            "roll-recipe-check" -> buildPromise {
                target.closest(".km-camping-recipe")
                    ?.takeIfInstance<HTMLElement>()
                    ?.dataset["recipeId"]
                    ?.let { rollRecipeCheck(it) }
            }

            "consume-rations" -> buildPromise {
                consumeRations()
            }

            "roll-camping-check" -> buildPromise {
                target.closest(".km-camping-activity")
                    ?.takeIfInstance<HTMLElement>()
                    ?.let { tile ->
                        tile.dataset["activityId"]?.let { activity ->
                            tile.dataset["actorUuid"]?.let { actorUuid ->
                                rollCheck(activity, actorUuid)
                            }
                        }
                    }
            }

            "favorite-meals" -> buildPromise { FavoriteMealsApplication(game, actor).launch() }
            "next-section" -> buildPromise { nextSection() }
            "previous-section" -> buildPromise { previousSection() }
            "check-encounter" -> buildPromise { rollEncounter(includeFlatCheck = true) }
            "roll-encounter" -> buildPromise { rollEncounter(includeFlatCheck = false) }
            "advance-hour" -> buildPromise {
                advanceHours(target)
            }

            "advance-hexploration" -> buildPromise {
                advanceHexplorationActivities(target)
            }

            "clear-actor" -> {
                buildPromise {
                    target.dataset["uuid"]?.let { actor.deleteCampingActor(it) {} }
                }
            }

            "clear-activity" -> {
                buildPromise {
                    target.dataset["id"]?.let { clearActivity(it) }
                }
            }

            "show-players" -> buildPromise {
                dispatcher.dispatch(
                    ActionMessage(
                        action = "openCampingSheet",
                        data = OpenCampingSheetAction(actorUuid = actor.uuid)
                    )
                )
            }

            "open-journal" -> {
                event.preventDefault()
                event.stopPropagation()
                buildPromise {
                    target.dataset["uuid"]?.let { openJournal(it) }
                }
            }

            "open-item" -> {
                event.preventDefault()
                event.stopPropagation()
                buildPromise {
                    target.dataset["uuid"]?.let { openItem(it) }
                }
            }

            "quickstart" -> buildPromise {
                openJournal("Compendium.pf2e-kingmaker-tools.kingmaker-tools-journals.JournalEntry.kd8cT1Uv9hZOrpgS")
            }


            "help" -> buildPromise {
                openJournal("Compendium.pf2e-kingmaker-tools.kingmaker-tools-journals.JournalEntry.iAQCUYEAq4Dy8uCY.JournalEntryPage.7z4cDr3FMuSy22t1")
            }

            "open-actor" -> {
                event.preventDefault()
                event.stopPropagation()
                buildPromise {
                    target.dataset["uuid"]?.let { openActor(it) }
                }
            }

            "increase-encounter-dc" -> buildPromise {
                target.dataset["value"]?.toInt()?.let { changeEncounterDcModifier(it) }
            }

            "reset-encounter-dc" -> buildPromise {
                changeEncounterDcModifier(null)
            }

            "reset-adventuring-for" -> buildPromise {
                resetAdventuringTimeTracker()
            }
        }
    }

    private suspend fun consumeRations() {
        // the following lines should all be non-null if everything went right
        val camping = actor.getCamping()
        checkNotNull(camping) { "Could not find camping data on actor ${actor.uuid}" }
        val parsed = camping.findCookingChoices(
            charactersInCampByUuid = camping.getActorsInCamp()
                .associateBy { it.uuid },
            recipesById = camping.getAllRecipes()
                .associateBy { it.id },
        )
        val rations = parsed.meals
            .filterIsInstance<MealChoice.Rations>()
            .map { it.cookingCost }
            .sum()
        reduceFoodBy(
            actors = camping.getActorsCarryingFood(actor),
            foodAmount = rations,
            foodItems = getCompendiumFoodItems(),
        )
    }

    private suspend fun rollRecipeCheck(recipeId: String) {
        // the following lines should all be non-null if everything went right
        val camping = actor.getCamping()
        checkNotNull(camping) { "Could not find camping data on actor ${actor.uuid}" }
        val region = camping.findCurrentRegion()
        checkNotNull(region) { "Could not determine current region" }
        val activityData = camping.groupActivities().find { it.isCookMeal() }
        checkNotNull(activityData) { "Could not find Cook Meal activity" }
        val parsed = camping.findCookingChoices(
            charactersInCampByUuid = camping.getActorsInCamp()
                .filterIsInstance<PF2ECharacter>()
                .associateBy { it.uuid },
            recipesById = camping.getAllRecipes()
                .associateBy { it.id },
        )
        val cook = parsed.cook
        checkNotNull(cook) { "Trying to cook a meal without a selected cook" }
        val mealToCook = parsed.results.find { it.recipe.id == recipeId }
        checkNotNull(mealToCook) { "Could not find meal with id $recipeId" }

        val result = cook.campingActivityCheck(
            data = CampingCheckData(
                region = region,
                activityData = activityData,
                skill = ParsedCampingSkill(
                    attribute = mealToCook.selectedSkill,
                    dcType = DcType.STATIC,
                    dc = mealToCook.dc
                )
            ),
            overrideDc = mealToCook.dc,
        )
        val existing = camping.cooking.results.find { it.recipeId == recipeId }
        if (existing == null) {
            camping.cooking.results = camping.cooking.results + CookingResult(
                recipeId = recipeId,
                skill = mealToCook.selectedSkill.value,
                result = result?.toCamelCase(),
            )
        } else {
            existing.result = result?.toCamelCase()
        }
        actor.setCamping(camping)
    }

    private suspend fun rollCheck(activityId: String, actorUuid: String) {
        val checkActor = getCampingActivityCreatureByUuid(actorUuid)
        checkNotNull(checkActor) { "Could not find camping actor with uuid $actorUuid" }

        val camping = actor.getCamping()
        checkNotNull(camping) { "Could not find camping data on actor ${actor.uuid}" }

        val campingCheckData = checkActor.getCampingCheckData(camping, activityId)
        checkNotNull(campingCheckData) { "Could not resolve skill or region" }

        val activity = campingCheckData.activityData.data

        // preparing check removes all meal effects; note that this is prone to races
        // when prepare camp would receive meal bonuses which technically shouldn't happen
        if (activity.isPrepareCampsite()) {
            val message = ActionMessage(
                action = "clearMealEffects",
                data = ClearMealEffectsMessage(campingActorUuid = actor.uuid)
            )
            dispatcher.dispatch(message)
            postChatMessage(t("camping.preparingCampsite"))
            val existingCampingResult = camping.worldSceneId?.let { findExistingCampsiteResult(game, it, actor) }
            if (existingCampingResult != null
                && confirm(
                    t(
                        "camping.reuseCampsiteConfirmation",
                        recordOf("degreeOfSuccess" to t(existingCampingResult))
                    )
                )
            ) {
                camping.campingActivities[activityId]?.result = existingCampingResult.toCamelCase()
                postPassTimeMessage(t("camping.reuseCampsite"), 1)
                actor.setCamping(camping)
                return
            }
        }

        // if it's a recipe we need to know the dc
        val recipe = if (activity.isDiscoverSpecialMeal()) askRecipe(camping) else null
        checkActor.campingActivityCheck(
            data = campingCheckData,
            overrideDc = recipe?.cookingLoreDC,
        )?.let { result ->
            camping.campingActivities[activityId]?.result = result.toCamelCase()
            actor.setCamping(camping)

            if (activity.isHuntAndGather()) {
                postHuntAndGather(
                    actor = checkActor,
                    degreeOfSuccess = result,
                    zoneDc = campingCheckData.region.zoneDc,
                    regionLevel = campingCheckData.region.level,
                    campingActor = actor,
                )
            } else if (activity.isDiscoverSpecialMeal() && recipe != null) {
                postDiscoverSpecialMeal(
                    actorUuid = checkActor.uuid,
                    recipe = recipe,
                    degreeOfSuccess = result,
                    campingActorUuid = actor.uuid,
                )
            } else if (activity.isPrepareCampsite()) {
                postPassTimeMessage(t("camping.prepareNewCampsite"), 2)
            }
        }
    }

    private suspend fun askRecipe(
        camping: CampingData
    ): RecipeData? =
        try {
            pickSpecialRecipe(camping = camping, partyActor = actor)
        } catch (_: Exception) {
            ui.notifications.error(t("camping.noRecipeChosen"))
            null
        }


    private suspend fun resetMeals() {
        actor.getCamping()?.let { camping ->
            camping.cooking.actorMeals.forEach { it.chosenMeal = "nothing" }
            actor.setCamping(camping)
        }
    }


    private suspend fun resetActivities() {
        actor.getCamping()?.let { camping ->
            val ids = Object.keys(camping.campingActivities)
                .filter { it != prepareCampsiteId }
                .toSet()
            actor.deleteCampingActivities(ids) {}
        }
    }

    private suspend fun previousSection() {
        actor.getCamping()?.let { camping ->
            camping.section = when (fromCamelCase<CampingSheetSection>(camping.section)) {
                CampingSheetSection.EATING -> if (camping.canPerformActivities()) {
                    CampingSheetSection.CAMPING_ACTIVITIES
                } else {
                    CampingSheetSection.PREPARE_CAMPSITE
                }

                else -> CampingSheetSection.PREPARE_CAMPSITE
            }.toCamelCase()
            actor.setCamping(camping)
        }
    }

    private suspend fun nextSection() {
        actor.getCamping()?.let { camping ->
            camping.section = when (fromCamelCase<CampingSheetSection>(camping.section)) {
                CampingSheetSection.PREPARE_CAMPSITE -> if (camping.canPerformActivities()) {
                    CampingSheetSection.CAMPING_ACTIVITIES
                } else {
                    CampingSheetSection.EATING
                }

                else -> CampingSheetSection.EATING
            }.toCamelCase()
            actor.setCamping(camping)
        }
    }

    private suspend fun rollEncounter(includeFlatCheck: Boolean) {
        rollRandomEncounter(game, actor, includeFlatCheck)
    }

    private suspend fun assignRecipeTo(actorUuid: String, recipeId: String) {
        val isNotACharacter = getCampingActivityActorByUuid(actorUuid) == null
        val isNotARation = recipeId != "rationsOrSubsistence" && recipeId != "nothing"
        if (isNotARation && isNotACharacter) {
            ui.notifications.error(t("camping.onlyCharactersConsumeMeals"))
            return
        }
        actor.getCamping()?.let { camping ->
            val existingMeal = camping.cooking.actorMeals.find { it.actorUuid == actorUuid }
            if (existingMeal == null) {
                camping.cooking.actorMeals = camping.cooking.actorMeals + ActorMeal(
                    actorUuid = actorUuid,
                    chosenMeal = recipeId,
                )
            } else {
                existingMeal.chosenMeal = recipeId
            }
            actor.setCamping(camping)
        }
    }

    private suspend fun assignActivityTo(actorUuid: String, activityId: String) {
        actor.getCamping()?.let { camping ->
            val activity = camping.getAllActivities().find { it.id == activityId }
            val activityActor = getCampingActivityCreatureByUuid(actorUuid)
            if (activityActor == null) {
                ui.notifications.error(t("camping.onlyCharactersCanPerformActivities"))
            } else if (activity == null) {
                ui.notifications.error(t("camping.activityNotFound", recordOf("id" to activityId)))
            } else if (!activityActor.satisfiesAnyActivitySkillRequirement(activity, camping.ignoreSkillRequirements)) {
                ui.notifications.error(
                    t(
                        "camping.actorLacksSkillRequirements",
                        recordOf("activityName" to activity.name)
                    )
                )
            } else if (activity.requiresACheck() && !activityActor.hasAnyActivitySkill(activity)) {
                ui.notifications.error(t("camping.actorLacksSkills", recordOf("activityName" to activity.name)))
            } else {
                val skill = activityActor
                    .findCampingActivitySkills(activity, camping.ignoreSkillRequirements)
                    .filterNot { it.validateOnly }
                    .firstOrNull()
                val existing = camping.campingActivities[activityId]
                if (existing == null) {
                    camping.campingActivities[activity.id] = CampingActivity(
                        actorUuid = actorUuid,
                        selectedSkill = skill?.attribute?.value,
                    )
                } else {
                    existing.actorUuid = actorUuid
                    existing.selectedSkill = skill?.attribute?.value
                }
                actor.setCamping(camping)
            }
        }
    }

    private suspend fun changeEncounterDcModifier(modifier: Int?) {
        actor.getCamping()?.let { camping ->
            if (modifier == null) {
                camping.encounterModifier = 0
            } else {
                val encounterDc = camping.findCurrentRegion()?.encounterDc ?: 0
                val totalDc = encounterDc + camping.encounterModifier + modifier
                val mod = if (totalDc > 20) {
                    20 - encounterDc
                } else if (totalDc < 0) {
                    -encounterDc
                } else {
                    camping.encounterModifier + modifier
                }
                camping.encounterModifier = mod
            }
            actor.setCamping(camping)
        }
    }

    private suspend fun resetAdventuringTimeTracker() {
        actor.getCamping()?.let { camping ->
            camping.resetTimeTracking(game)
            actor.setCamping(camping)
        }
    }

    private suspend fun addActor(uuid: String) {
        actor.getCamping()?.let { camping ->
            if (uuid !in camping.actorUuids) {
                val campingActor = getCampingActorByUuid(uuid)
                if (campingActor == null) {
                    ui.notifications.error(t("camping.wrongActorAddedToSheet"))
                } else {
                    camping.actorUuids = camping.actorUuids + uuid
                    camping.cooking.actorMeals = camping.cooking.actorMeals + ActorMeal(
                        actorUuid = uuid,
                        favoriteMeal = null,
                        chosenMeal = "nothing",
                    )
                    actor.setCamping(camping)
                }
            }
        }
    }

    private suspend fun addItemToActor(documentRef: DocumentRef<*>, actor: PF2EActor) {
        val document = documentRef.getDocument()
        if (allowedDnDItems.any { it.isInstance(document) }) {
            actor.addToInventory(document.toObject())
        } else {
            ui.notifications.error(t("camping.wrongItemAddedToActor"))
        }
    }

    private suspend fun clearActivity(id: String) {
        actor.getCamping()?.let {
            it.campingActivities[id]?.actorUuid = null
            actor.setCamping(it)
        }
    }

    private suspend fun advanceHexplorationActivities(target: HTMLElement) {
        val seconds = getHexplorationActivitySeconds() * (target.dataset["activities"]?.toInt() ?: 0)
        game.time.advance(seconds).await()
    }

    private fun getAvailableHexplorationSeconds(): Int = EIGHT_HOURS_SECONDS

    private fun getHexplorationActivitySeconds(): Int =
        ((getAvailableHexplorationSeconds()) / getHexplorationActivities()).toInt()

    private fun getHexplorationActivities(): Double {
        val camping = actor.getCamping()
        val travelSpeed = actor.system.movement.speeds.travel.value
        val override = max(camping?.minimumTravelSpeed ?: 0, travelSpeed)
        return calculateHexplorationActivities(override)
    }

    private fun getHexplorationActivitiesDuration(): String =
        LocalTime.fromSecondOfDay(getHexplorationActivitySeconds()).toDateInputString()

    private fun getHexplorationActivitiesAvailable(camping: CampingData): Int =
        max(
            0,
            (getAvailableHexplorationSeconds() - camping.secondsSpentHexploring) / getHexplorationActivitySeconds()
        )

    private fun getAdventuringFor(camping: CampingData): String {
        return formatSeconds(camping.secondsSpentHexploring)
    }

    private fun getTravelingFor(camping: CampingData): String {
        return formatSeconds(camping.secondsSpentTraveling)
    }

    private suspend fun advanceHours(target: HTMLElement) {
        game.time.advance(3600 * (target.dataset["hours"]?.toInt() ?: 0)).await()
    }

    private suspend fun getRecipeContext(
        parsedCookingChoices: ParsedMeals,
        foodItems: FoodItems,
        total: FoodAmount,
        camping: CampingData,
        section: CampingSheetSection,
    ): Array<RecipeContext> {
        val actorsByChosenMeal = parsedCookingChoices.meals
            .map { meal ->
                RecipeActorContext(
                    name = meal.actor.name,
                    uuid = meal.actor.uuid,
                    image = meal.actor.img,
                    chosenMeal = meal.name,
                    favoriteMeal = meal.favoriteMeal?.name,
                )
            }
            .groupBy { it.chosenMeal }
        val starving = RecipeContext(
            name = t("camping.skipMeal"),
            targetRecipe = "nothing",
            icon = "icons/containers/kitchenware/bowl-clay-brown.webp",
            requiresCheck = false,
            hidden = section != CampingSheetSection.EATING,
            rations = false,
            consumeRationsEnabled = false,
            actors = actorsByChosenMeal["nothing"]?.toTypedArray() ?: emptyArray(),
        )
        val rationActors: Array<RecipeActorContext> =
            actorsByChosenMeal["rationsOrSubsistence"]?.toTypedArray() ?: emptyArray()
        val rations = RecipeContext(
            name = t("camping.rations"),
            targetRecipe = "rationsOrSubsistence",
            icon = "icons/consumables/food/berries-ration-round-red.webp",
            requiresCheck = false,
            cost = buildFoodCost(
                FoodAmount(rations = 1),
                totalAmount = total,
                items = foodItems,
            ),
            rations = true,
            consumeRationsEnabled = rationActors.isNotEmpty(),
            actors = rationActors,
            hidden = section != CampingSheetSection.EATING,
        )
        val cookMealActor = parsedCookingChoices.cook
        val cookingSkillOptions = parsedCookingChoices.skills.map { it.toOption() }
        val knownRecipes = camping.cooking.knownRecipes.toSet()
        return arrayOf(starving, rations) + camping.getAllRecipes()
            .sortedBy { it.name }
            .map { recipe ->
                val item = itemFromUuid(recipe.uuid)
                val cookingCost = buildFoodCost(
                    recipe.cookingCost(),
                    totalAmount = total,
                    items = foodItems
                )
                val result = parsedCookingChoices.results.find { it.recipe.name == recipe.name }
                RecipeContext(
                    name = recipe.name,
                    targetRecipe = recipe.id,
                    cost = cookingCost,
                    uuid = recipe.uuid,
                    icon = recipe.icon ?: item?.img ?: "icons/consumables/food/shank-meat-bone-glazed-brown.webp",
                    requiresCheck = true,
                    hidden = section != CampingSheetSection.EATING || cookMealActor == null || recipe.id !in knownRecipes,
                    rations = false,
                    consumeRationsEnabled = false,
                    actors = actorsByChosenMeal[recipe.name]?.toTypedArray() ?: emptyArray(),
                    skills = Select(
                        label = t("camping.selectedSkill"),
                        name = "recipes.selectedSkill.${recipe.name}",
                        hideLabel = true,
                        options = cookingSkillOptions,
                        elementClasses = listOf("km-proficiency"),
                        value = result?.selectedSkill?.value,
                    ).toContext(),
                    degreeOfSuccess = Select.fromEnum<DegreeOfSuccess>(
                        hideLabel = true,
                        required = false,
                        name = "recipes.degreeOfSuccess.${recipe.name}",
                        value = result?.degreeOfSuccess,
                        elementClasses = listOf("km-degree-of-success"),
                    ).toContext(),
                )
            }
            .toTypedArray()
    }

    private fun calculateTotalFoodCost(
        actorMeals: List<MealChoice>,
        availableFood: FoodAmount,
        foodItems: FoodItems,
    ): FoodCost {
        val amount = actorMeals
            .map { it.cookingCost }
            .sum()
        return buildFoodCost(amount, totalAmount = availableFood, items = foodItems)
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<CampingSheetContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        val time = game.getPF2EWorldTime().time
        val dayPercentage = time.toSecondOfDay().toFloat() / 86400f
        val widthWithoutBorder = windowWidth - 2
        val pxTimeOffset = -((dayPercentage * widthWithoutBorder).toInt() - widthWithoutBorder / 2)
        val camping = actor.getCamping() ?: getDefaultCamping(game)
        val actorsByUuid = getCampingActorsByUuid(camping.actorUuids).associateBy(PF2EActor::uuid)
        val charactersByUuid: Map<String, PF2EActor> = actorsByUuid
            .mapNotNull {
                val value = it.value
                it.key to value
            }
            .toMap()
        val groupActivities = camping.groupActivities().sortedBy { it.data.id }
        val section = fromCamelCase<CampingSheetSection>(camping.section) ?: CampingSheetSection.PREPARE_CAMPSITE
        val prepareCampSection = section == CampingSheetSection.PREPARE_CAMPSITE
        val campingActivitiesSection = section == CampingSheetSection.CAMPING_ACTIVITIES
        val eatingSection = section == CampingSheetSection.EATING
        val foodItems = getCompendiumFoodItems()
        val totalFood = camping.getTotalCarriedFood(actor, foodItems)
        val availableFood = buildFoodCost(totalFood, items = foodItems)
        val parsedCookingChoices = camping.findCookingChoices(
            charactersInCampByUuid = charactersByUuid,
            recipesById = camping.getAllRecipes().associateBy { it.id },
        )
        val recipesContext = getRecipeContext(
            parsedCookingChoices = parsedCookingChoices,
            foodItems = foodItems,
            total = totalFood,
            camping = camping,
            section = section,
        )
        val chosenMealsByActorUuid = recipesContext.asSequence()
            .flatMap { recipe ->
                recipe.actors.asSequence()
                    .filter { it.chosenMeal != "nothing" }
                    .map { it.uuid to recipe }
            }
            .toMap()
        val activities = groupActivities.mapIndexed { _, groupedActivity ->
            val (data, result) = groupedActivity
            val actor = result.actorUuid?.let { actorsByUuid[it] }?.unsafeCast<PF2ECreature>()
            val requiresCheck = !data.doesNotRequireACheck()
            val skills = getActivitySkills(
                actor = actor,
                groupedActivity = groupedActivity,
                ignoreSkillRequirements = camping.ignoreSkillRequirements,
            )
            val hidden = camping.lockedActivities.contains(data.id)
                    || (prepareCampSection && !groupedActivity.isPrepareCamp())
                    || (campingActivitiesSection && groupedActivity.isPrepareCamp())
                    || eatingSection
                    || camping.alwaysPerformActivityIds.contains(data.id)
            CampingSheetActivity(
                id = data.id,
                secret = data.isSecret && !game.user.isGM,
                journalUuid = data.journalUuid,
                name = data.name,
                hidden = hidden,
                requiresCheck = requiresCheck,
                skills = skills,
                actor = actor?.let { act ->
                    val degree = result.result?.let { fromCamelCase<DegreeOfSuccess>(it) }
                    CampingSheetActor(
                        name = act.name,
                        uuid = act.uuid,
                        image = act.img,
                        choseActivity = true,
                        degreeOfSuccess = Select.fromEnum<DegreeOfSuccess>(
                            hideLabel = true,
                            required = false,
                            name = "activities.degreeOfSuccess.${data.id}",
                            value = degree,
                            elementClasses = listOf("km-degree-of-success"),
                        ).toContext(),
                    )
                },
            )
        }.toTypedArray()
        val recipes = camping.getAllRecipes()
        val fullRestDuration = getTotalRestDuration(
            watchers = actorsByUuid.values.filter { !camping.actorUuidsNotKeepingWatch.contains(it.uuid) },
            recipes = recipes.toList(),
            gunsToClean = camping.gunsToClean,
            increaseActorsKeepingWatch = camping.increaseWatchActorNumber,
            remainingSeconds = camping.watchSecondsRemaining,
            skipWatch = false,
            skipDailyPreparations = false,
        )
        val currentRegion = camping.findCurrentRegion()
        val regions = camping.regionSettings.regions
        val isGM = game.user.isGM
        val uncookedMeals = parsedCookingChoices.results
            .filter { it.degreeOfSuccess == null }
            .map { it.recipe.name }
            .toSet()
        val hexplorationActivityDuration = getHexplorationActivitiesDuration()
        val hexplorationActivitiesAvailable = getHexplorationActivitiesAvailable(camping)
        val hexplorationActivitiesMax = "${getHexplorationActivities()}"
        val nightModes = calculateNightModes(time)
        CampingSheetContext(
            canRollEncounter = currentRegion?.rollTableUuid != null,
            availableFood = availableFood,
            totalFoodCost = calculateTotalFoodCost(
                actorMeals = parsedCookingChoices.meals
                    .filter { it.name in uncookedMeals || it.id == "rationsOrSubsistence" },
                foodItems = foodItems,
                availableFood = totalFood,
            ),
            partId = parent.partId,
            recipes = recipesContext,
            terrain = currentRegion?.terrain ?: "plains",
            region = Select(
                label = t("camping.region"),
                value = currentRegion?.name,
                options = regions.map {
                    SelectOption(label = it.name, value = it.name)
                },
                required = true,
                name = "region",
                disabled = !isGM,
                stacked = false,
            ).toContext(),
            pxTimeOffset = pxTimeOffset,
            time = time.toDateInputString(),
            isGM = isGM,
            isDay = time.isDay(),
            prepareCamp = activities.find { it.isPrepareCampsite() },
            activities = activities.filter { !it.isPrepareCampsite() }.toTypedArray(),
            actors = camping.actorUuids.mapNotNull { uuid ->
                actorsByUuid[uuid]?.let { actor ->
                    val meal = if (eatingSection) {
                        chosenMealsByActorUuid[uuid]
                    } else {
                        null
                    }
                    CampingSheetActor(
                        image = actor.img,
                        uuid = uuid,
                        name = actor.name,
                        choseActivity = campingActivitiesSection
                                && groupActivities.any {
                            it.isNotPrepareCamp()
                                    && it.result.actorUuid == uuid
                                    && it.done()
                        },
                        chosenMeal = meal?.name,
                        chosenMealImg = meal?.icon
                    )
                }
            }.toTypedArray(),
            night = nightModes,
            hexplorationActivityDuration = hexplorationActivityDuration,
            hexplorationActivitiesAvailable = hexplorationActivitiesAvailable,
            hexplorationActivitiesMax = hexplorationActivitiesMax,
            adventuringFor = getAdventuringFor(camping),
            travelingFor = getTravelingFor(camping),
            restDuration = fullRestDuration.total.label,
            restDurationLeft = fullRestDuration.left?.label,
            encounterDc = findEncounterDcModifier(camping, game.getPF2EWorldTime().time.isDay()),
            section = t(section),
            prepareCampSection = prepareCampSection,
            campingActivitiesSection = campingActivitiesSection,
            eatingSection = eatingSection,
            isFormValid = isFormValid,
            travelMode = CheckboxInput(
                value = camping.travelModeActive,
                label = t("camping.traveling"),
                name = "travelModeActive",
                elementClasses = if (nightModes.travelMode) listOf("white-checkbox") else listOf("black-checkbox")
            ).toContext(),
        )
    }

    override fun onParsedSubmit(value: CampingSheetFormData): Promise<Void> = buildPromise {
        actor.getCamping()?.let { camping ->
            camping.currentRegion = value.region
            camping.campingActivities = camping.campingActivities
                .asSequence().map {
                    val id = it.component1()
                    id to CampingActivity(
                        actorUuid = it.component2().actorUuid,
                        result = value.activities.degreeOfSuccess?.get(id),
                        selectedSkill = value.activities.selectedSkill?.get(id),
                    )
                }.toMutableRecord()
            val cookingResultsByRecipe = camping.cooking.results.associateBy { it.recipeId }
            camping.cooking.results = camping.getAllRecipes().map {
                val result = cookingResultsByRecipe[it.id] ?: CookingResult(
                    recipeId = it.id,
                    result = null,
                    skill = "survival",
                )
                CookingResult.copy(
                    result,
                    result = value.recipes?.degreeOfSuccess?.get(it.name),
                    skill = value.recipes?.selectedSkill?.get(it.name) ?: "survival",
                )
            }.toTypedArray()
            camping.travelModeActive = value.travelModeActive
            actor.setCamping(camping)
        }
        undefined
    }
}


private fun getActivitySkills(
    actor: PF2ECreature?,
    groupedActivity: ActivityAndData,
    ignoreSkillRequirements: Boolean,
): FormElementContext {
    return groupedActivity.data.getCampingSkills(actor).let { skillsAndProficiencies ->
        val options = skillsAndProficiencies
            .filter {
                if (ignoreSkillRequirements || actor == null) {
                    true
                } else {
                    actor.satisfiesSkillRequirement(it)
                }
            }
            .filter { !it.validateOnly }
            .map {
                SelectOption(
                    label = t(it.attribute),
                    value = it.attribute.value,
                    classes = listOf("km-proficiency-${it.proficiency.toCamelCase()}")
                )
            }
            .sortedBy { it.label }
        Select(
            label = t("camping.selectedSkill"),
            name = "activities.selectedSkill.${groupedActivity.data.id}",
            hideLabel = true,
            options = options,
            elementClasses = listOf("km-proficiency"),
            value = groupedActivity.result.selectedSkill,
        ).toContext()
    }
}

suspend fun openOrCreateCampingSheet(game: Game, dispatcher: ActionDispatcher, actor: CampingActor) {
    if (actor.getCamping() == null) {
        actor.setCamping(getDefaultCamping(game))
        openJournal("Compendium.pf2e-kingmaker-tools.kingmaker-tools-journals.JournalEntry.kd8cT1Uv9hZOrpgS")
    }
    CampingSheet(game, actor, dispatcher).launch()
}