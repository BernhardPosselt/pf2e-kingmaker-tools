package at.posselt.pfrpg2e.camping.dialogs

import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.ValidatedHandlebarsContext
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.app.forms.SectionContext
import at.posselt.pfrpg2e.app.forms.SectionsContext
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.TextArea
import at.posselt.pfrpg2e.app.forms.TextInput
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.app.forms.toOption
import at.posselt.pfrpg2e.camping.CampingActor
import at.posselt.pfrpg2e.camping.CookingOutcome
import at.posselt.pfrpg2e.camping.HealMode
import at.posselt.pfrpg2e.camping.MealEffect
import at.posselt.pfrpg2e.camping.RawCost
import at.posselt.pfrpg2e.camping.RecipeData
import at.posselt.pfrpg2e.camping.ReduceConditionMode
import at.posselt.pfrpg2e.camping.ReduceConditions
import at.posselt.pfrpg2e.camping.getCamping
import at.posselt.pfrpg2e.camping.setCamping
import at.posselt.pfrpg2e.data.Currency
import at.posselt.pfrpg2e.data.general.Rarity
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.slugify
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.fromUuidTypeSafe
import at.posselt.pfrpg2e.utils.launch
import at.posselt.pfrpg2e.utils.t
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.Game
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.abstract.DocumentConstructionContext
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.data.dsl.buildSchema
import com.foundryvtt.core.utils.deepClone
import com.foundryvtt.pf2e.item.PF2EEffect
import js.core.Void
import js.objects.recordOf
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.js.Promise

@JsPlainObject
external interface RecipeContext : SectionsContext, ValidatedHandlebarsContext

@JsPlainObject
external interface OutcomeSubmitData {
    val uuid: String
    val removeAfterRest: Boolean
    val doublesHealing: Boolean
    val halvesHealing: Boolean
    val changeRestDurationSeconds: Int
    val healFormula: String
    val damageFormula: String
    val healMode: String
    val reduceConditions: ReduceConditions
    val message: String?
    val changeFatigueDurationSeconds: Int

}

@JsPlainObject
external interface RecipeSubmitData {
    val id: String
    val name: String
    val uuid: String
    val level: Int
    val rarity: String
    val coins: Int
    val currency: String
    val cookingLoreDC: Int
    val survivalDC: Int
    val basicIngredients: Int
    val specialIngredients: Int
    val favoriteMeal: OutcomeSubmitData
    val criticalSuccess: OutcomeSubmitData
    val success: OutcomeSubmitData
    val criticalFailure: OutcomeSubmitData
}

@JsExport
class RecipeDataModel(
    value: AnyObject,
    options: DocumentConstructionContext?
) : DataModel(value, options) {
    companion object {
        @JsStatic
        fun defineSchema() = buildSchema {
            string("id")
            string("name")
            string("uuid")
            int("level")
            enum<Rarity>("rarity")
            enum<Currency>("currency")
            int("coins")
            int("cookingLoreDC")
            int("survivalDC")
            int("basicIngredients")
            int("specialIngredients")
            schema("favoriteMeal") {
                string("uuid")
                string("message", nullable = true)
                string("healFormula", nullable = true)
                string("damageFormula", nullable = true)
                int("changeRestDurationSeconds")
                int("changeFatigueDurationSeconds")
                boolean("removeAfterRest")
                boolean("doublesHealing")
                boolean("halvesHealing")
                enum<HealMode>("healMode")
                schema("reduceConditions") {
                    int("drained")
                    int("enfeebled")
                    int("clumsy")
                    int("stupefied")
                    enum<ReduceConditionMode>("mode")
                }
            }
            schema("criticalSuccess") {
                string("uuid")
                string("message", nullable = true)
                string("healFormula", nullable = true)
                string("damageFormula", nullable = true)
                int("changeRestDurationSeconds")
                int("changeFatigueDurationSeconds")
                boolean("removeAfterRest")
                boolean("doublesHealing")
                boolean("halvesHealing")
                enum<HealMode>("healMode")
                schema("reduceConditions") {
                    int("drained")
                    int("enfeebled")
                    int("clumsy")
                    int("stupefied")
                    enum<ReduceConditionMode>("mode")
                }
            }
            schema("success") {
                string("uuid")
                string("message", nullable = true)
                string("healFormula", nullable = true)
                string("damageFormula", nullable = true)
                int("changeRestDurationSeconds")
                int("changeFatigueDurationSeconds")
                boolean("removeAfterRest")
                boolean("doublesHealing")
                boolean("halvesHealing")
                enum<HealMode>("healMode")
                schema("reduceConditions") {
                    int("drained")
                    int("enfeebled")
                    int("clumsy")
                    int("stupefied")
                    enum<ReduceConditionMode>("mode")
                }
            }
            schema("criticalFailure") {
                string("message", nullable = true)
                string("uuid")
                string("healFormula", nullable = true)
                string("damageFormula", nullable = true)
                int("changeRestDurationSeconds")
                int("changeFatigueDurationSeconds")
                boolean("removeAfterRest")
                boolean("doublesHealing")
                boolean("halvesHealing")
                enum<HealMode>("healMode")
                schema("reduceConditions") {
                    int("drained")
                    int("enfeebled")
                    int("clumsy")
                    int("stupefied")
                    enum<ReduceConditionMode>("mode")
                }
            }
        }
    }
}

@JsExport
class RecipeApplication(
    private val game: Game,
    private val actor: CampingActor,
    recipe: RecipeData? = null,
    private val afterSubmit: () -> Unit,
) : FormApp<RecipeContext, RecipeSubmitData>(
    title = if (recipe == null) t("camping.addRecipe") else t(
        "camping.editRecipe",
        recordOf("recipeName" to recipe.name)
    ),
    template = "components/forms/application-form.hbs",
    debug = true,
    dataModel = RecipeDataModel::class.js,
    id = "kmRecipe-${actor.uuid}",
) {
    private val editRecipeId = recipe?.id
    private var currentRecipe: RecipeData? = recipe?.let(::deepClone)

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "openDocumentLink" -> buildPromise {
                event.preventDefault()
                event.stopPropagation()
                target.dataset["uuid"]?.let { fromUuidTypeSafe<PF2EEffect>(it)?.sheet?.launch() }
            }

            "km-save" -> save()
        }
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<RecipeContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        val effects = game.items.contents
            .filterIsInstance<PF2EEffect>()
        val recipeItem = currentRecipe?.uuid?.let { fromUuidTypeSafe<PF2EEffect>(it) } ?: effects.firstOrNull()
        val favoriteMeal = createMealInputs(
            namePrefix = "favoriteMeal",
            cookingOutcome = currentRecipe?.favoriteMeal,
            allEffects = effects,
        )
        val criticalSuccess = createMealInputs(
            namePrefix = "criticalSuccess",
            cookingOutcome = currentRecipe?.criticalSuccess,
            allEffects = effects,
        )
        val success = createMealInputs(
            namePrefix = "success",
            cookingOutcome = currentRecipe?.success,
            allEffects = effects,
        )
        val criticalFailure = createMealInputs(
            namePrefix = "criticalFailure",
            cookingOutcome = currentRecipe?.criticalFailure,
            allEffects = effects,
        )
        RecipeContext(
            partId = parent.partId,
            isFormValid = isFormValid,
            sections = arrayOf(
                SectionContext(
                    legend = t("camping.basic"),
                    formRows = formContext(
                        TextInput(
                            stacked = false,
                            label = t("applications.id"),
                            name = "id",
                            readonly = editRecipeId != null,
                            value = currentRecipe?.id ?: "",
                            required = true,
                            help = "To override an existing recipe, use the same id",
                        ),
                        TextInput(
                            stacked = false,
                            label = t("applications.name"),
                            name = "name",
                            value = currentRecipe?.name ?: "",
                            required = true,
                        ),
                        Select(
                            label = t("camping.recipeItem"),
                            name = "uuid",
                            help = t("camping.recipeItemHelp"),
                            value = recipeItem?.uuid,
                            options = effects.mapNotNull { it.toOption(useUuid = true) },
                            stacked = false,
                            item = recipeItem,
                        ),
                        Select.level(
                            stacked = false,
                            value = currentRecipe?.level ?: 1,
                        ),
                        Select.fromEnum<Rarity>(
                            name = "rarity",
                            stacked = false,
                            elementClasses = listOf("km-rarity"),
                            value = currentRecipe?.rarity?.let { fromCamelCase<Rarity>(it) } ?: Rarity.COMMON,
                        ),
                        NumberInput(
                            label = t("camping.coins"),
                            name = "coins",
                            stacked = false,
                            value = currentRecipe?.cost?.value ?: 0,
                        ),
                        Select.fromEnum<Currency>(
                            name = "currency",
                            stacked = false,
                            value = Currency.fromString(currentRecipe?.cost?.currency ?: "gp"),
                        ),
                    )
                ),
                SectionContext(
                    legend = t("camping.cooking"),
                    formRows = formContext(
                        Select.dc(
                            label = t("camping.cookingLoreDC"),
                            name = "cookingLoreDC",
                            stacked = false,
                            value = currentRecipe?.cookingLoreDC ?: 13,
                        ),
                        Select.dc(
                            label = t("camping.survivalDC"),
                            name = "survivalDC",
                            stacked = false,
                            value = currentRecipe?.survivalDC ?: 15,
                        ),
                        NumberInput(
                            label = t("camping.basicIngredients"),
                            name = "basicIngredients",
                            stacked = false,
                            value = currentRecipe?.basicIngredients ?: 0,
                        ),
                        NumberInput(
                            label = t("camping.specialIngredients"),
                            name = "specialIngredients",
                            stacked = false,
                            value = currentRecipe?.specialIngredients ?: 0,
                        ),
                    )
                ),
                SectionContext(
                    legend = t("camping.favoriteMeal"),
                    formRows = favoriteMeal,
                ),
                SectionContext(
                    legend = t("degreeOfSuccess.criticalSuccess"),
                    formRows = criticalSuccess,
                ),
                SectionContext(
                    legend = t("degreeOfSuccess.success"),
                    formRows = success,
                ),
                SectionContext(
                    legend = t("degreeOfSuccess.criticalFailure"),
                    formRows = criticalFailure,
                ),
            )
        )
    }


    fun save(): Promise<Void> = buildPromise {
        if (isValid()) {
            actor.getCamping()?.let { camping ->
                currentRecipe?.let { data ->
                    camping.cooking.homebrewMeals = camping.cooking.homebrewMeals
                        .filter { it.id != data.id }
                        .toTypedArray()
                    camping.cooking.homebrewMeals = camping.cooking.homebrewMeals + data
                    actor.setCamping(camping)
                    close().await()
                    afterSubmit()
                }
            }
        }
        undefined
    }

    override fun onParsedSubmit(value: RecipeSubmitData): Promise<Void> = buildPromise {
        currentRecipe = RecipeData(
            id = editRecipeId ?: value.id.slugify(),
            name = value.name,
            basicIngredients = value.basicIngredients,
            specialIngredients = value.specialIngredients,
            cookingLoreDC = value.cookingLoreDC,
            survivalDC = value.survivalDC,
            uuid = value.uuid,
            level = value.level,
            cost = RawCost(value = value.coins, currency = value.currency),
            rarity = value.rarity,
            isHomebrew = true,
            criticalSuccess = toOutcome(value.criticalSuccess),
            success = toOutcome(value.success),
            criticalFailure = toOutcome(value.criticalFailure),
            favoriteMeal = toOutcome(value.favoriteMeal),
        )
        undefined
    }

}

private suspend fun createMealInputs(
    namePrefix: String,
    cookingOutcome: CookingOutcome?,
    allEffects: List<PF2EEffect>,
): Array<FormElementContext> {
    val firstEffect = cookingOutcome?.effects?.firstOrNull()
    val item = firstEffect?.uuid
        ?.let { fromUuidTypeSafe<PF2EEffect>(it) }
        ?: allEffects.firstOrNull()
    return formContext(
        TextArea(
            label = t("camping.cookingMessage"),
            help = t("camping.cookingMessageHelp"),
            value = cookingOutcome?.message ?: "",
            required = false,
            stacked = false,
            name = "$namePrefix.message",
        ),
        Select(
            label = t("camping.effect"),
            name = "$namePrefix.uuid",
            options = allEffects.mapNotNull { it.toOption(useUuid = true) },
            stacked = false,
            item = item,
            value = item?.uuid,
        ),
        CheckboxInput(
            label = t("camping.removeAfterRest"),
            name = "$namePrefix.removeAfterRest",
            stacked = false,
            value = firstEffect?.removeAfterRest == true,
        ),
        CheckboxInput(
            label = t("camping.doublesHealing"),
            help = t("camping.doublesHealingHelp"),
            name = "$namePrefix.doublesHealing",
            value = firstEffect?.doublesHealing == true,
        ),
        CheckboxInput(
            label = t("camping.halvesHealing"),
            help = t("camping.halvesHealingHelp"),
            name = "$namePrefix.halvesHealing",
            value = firstEffect?.halvesHealing == true,
        ),
        TextInput(
            label = t("camping.healFormula"),
            help = t("camping.healFormulaHelp"),
            placeholder = "3d8",
            name = "$namePrefix.healFormula",
            value = firstEffect?.healFormula ?: "",
            required = false,
            stacked = false,
        ),
        TextInput(
            label = t("camping.damageFormula"),
            help = t("camping.damageFormulaHelp"),
            name = "$namePrefix.damageFormula",
            value = firstEffect?.damageFormula ?: "",
            placeholder = "3d8[poison]",
            required = false,
            stacked = false,
        ),
        Select.fromEnum<HealMode>(
            help = t("camping.healModeHelp"),
            name = "$namePrefix.healMode",
            value = firstEffect?.healMode?.let { fromCamelCase<HealMode>(it) } ?: HealMode.AFTER_CONSUMPTION,
            stacked = false,
        ),
        NumberInput(
            label = t("camping.reduceClumsyBy"),
            name = "$namePrefix.reduceConditions.clumsy",
            stacked = false,
            value = firstEffect?.reduceConditions?.clumsy ?: 0,
        ),
        NumberInput(
            label = t("camping.reduceDrainedBy"),
            name = "$namePrefix.reduceConditions.drained",
            stacked = false,
            value = firstEffect?.reduceConditions?.drained ?: 0,
        ),
        NumberInput(
            label = t("camping.reduceEnfeebledBy"),
            name = "$namePrefix.reduceConditions.enfeebled",
            stacked = false,
            value = firstEffect?.reduceConditions?.enfeebled ?: 0,
        ),
        NumberInput(
            label = t("camping.reduceStupefiedBy"),
            name = "$namePrefix.reduceConditions.stupefied",
            stacked = false,
            value = firstEffect?.reduceConditions?.stupefied ?: 0,
        ),
        Select.fromEnum<ReduceConditionMode>(
            help = t("camping.reduceConditionHelp"),
            name = "$namePrefix.reduceConditions.mode",
            value = firstEffect?.reduceConditions?.mode?.let { fromCamelCase<ReduceConditionMode>(it) }
                ?: ReduceConditionMode.ALL,
            stacked = false,
        ),
        NumberInput(
            label = t("camping.restDuration"),
            help = t("camping.restDurationHelp"),
            name = "$namePrefix.changeRestDurationSeconds",
            stacked = false,
            value = firstEffect?.changeRestDurationSeconds ?: 0,
        ),
        NumberInput(
            label = t("camping.fatigueDuration"),
            help = t("camping.fatigueDurationHelp"),
            name = "$namePrefix.changeFatigueDurationSeconds",
            stacked = false,
            value = firstEffect?.changeFatigueDurationSeconds ?: 0,
        ),
    )
}

private fun toOutcome(outcome: OutcomeSubmitData): CookingOutcome =
    CookingOutcome(
        message = outcome.message,
        effects = arrayOf(
            MealEffect(
                uuid = outcome.uuid,
                removeAfterRest = outcome.removeAfterRest,
                changeRestDurationSeconds = outcome.changeRestDurationSeconds,
                changeFatigueDurationSeconds = outcome.changeFatigueDurationSeconds,
                doublesHealing = outcome.doublesHealing,
                halvesHealing = outcome.halvesHealing,
                healFormula = outcome.healFormula,
                damageFormula = outcome.damageFormula,
                healMode = outcome.healMode,
                reduceConditions = ReduceConditions(
                    drained = outcome.reduceConditions.drained,
                    enfeebled = outcome.reduceConditions.enfeebled,
                    clumsy = outcome.reduceConditions.clumsy,
                    stupefied = outcome.reduceConditions.stupefied,
                    mode = outcome.reduceConditions.mode,
                )
            )
        )
    )