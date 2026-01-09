package at.posselt.pfrpg2e.camping

import at.posselt.pfrpg2e.data.Currency
import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.utils.t
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface ReduceConditions {
    val drained: Int?
    val enfeebled: Int?
    val clumsy: Int?
    val stupefied: Int?
    val mode: String? // all or random
}

fun ReduceConditions.reducesAnyCondition() =
    listOfNotNull(drained, enfeebled, clumsy, stupefied).any { it > 0 }

@JsPlainObject
external interface MealEffect {
    val uuid: String
    val removeWhenPreparingCampsite: Boolean?
    val removeAfterRest: Boolean?
    val changeRestDurationSeconds: Int?
    val doublesHealing: Boolean?
    val halvesHealing: Boolean?
    val healFormula: String?
    val damageFormula: String?
    val changeFatigueDurationSeconds: Int?
    val healMode: String? // afterConsumption, afterRest, afterConsumptionAndRest
    val reduceConditions: ReduceConditions?
}


@JsPlainObject
external interface CookingOutcome {
    val effects: Array<MealEffect>?
    val chooseRandomly: Boolean?
    val message: String?
}

@JsPlainObject
external interface RawCost {
    val currency: String
    val value: Int
}

fun RawCost.format() =
    "$value ${Currency.fromString(currency)?.let { t(it) }}"

@JsPlainObject
external interface RecipeData {
    val id: String
    val name: String
    val isSpecialMeal: Boolean?
    val basicIngredients: Int
    val specialIngredients: Int?
    val cookingLoreDC: Int
    val survivalDC: Int
    val uuid: String
    val icon: String?
    val level: Int
    val cost: RawCost
    val rarity: String
    val isHomebrew: Boolean?
    val criticalSuccess: CookingOutcome
    val success: CookingOutcome
    val criticalFailure: CookingOutcome
    val favoriteMeal: CookingOutcome?
}

enum class HealMode : ValueEnum, Translatable {
    AFTER_CONSUMPTION,
    AFTER_REST,
    AFTER_CONSUMPTION_AND_REST;

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "healMode.$value"
}

enum class ReduceConditionMode : ValueEnum, Translatable {
    ALL,
    RANDOM;

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "reduceConditionMode.$value"
}


fun RecipeData.canBeFavoriteMeal() = id != "basic-meal"

fun RecipeData.cookingCost(): FoodAmount =
    FoodAmount(
        basicIngredients = basicIngredients,
        specialIngredients = (specialIngredients ?: 0),
        rations = 1,
    )


fun RecipeData.discoverCost(): FoodAmount =
    cookingCost() * 2


@JsModule("./recipes.json")
private external val recipes: Array<RecipeData>

private fun CookingOutcome.translate() =
    CookingOutcome.copy(
        this,
        message = message?.let { t(it) }
    )

private fun RecipeData.translate() =
    RecipeData.copy(
        this,
        name = t(name),
        criticalSuccess = criticalSuccess.translate(),
        success = success.translate(),
        criticalFailure = criticalFailure.translate(),
        favoriteMeal = favoriteMeal?.translate(),
    )

private var translatedRecipes = emptyArray<RecipeData>()

fun translateRecipes() {
    translatedRecipes = recipes
        .map { it.translate() }
        .toTypedArray()
}

fun CampingData.getAllRecipes(): Array<RecipeData> {
    val homebrewIds = cooking.homebrewMeals.map { it.id }.toSet()
    return translatedRecipes
        .filter { it.id !in homebrewIds }
        .toTypedArray() + cooking.homebrewMeals
}
