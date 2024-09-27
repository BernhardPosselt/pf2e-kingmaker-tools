package at.posselt.pfrpg2e.camping

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
    val removeAfterRest: Boolean?
    val changeRestDurationSeconds: Int?
    val doublesHealing: Boolean?
    val halvesHealing: Boolean?
    val healFormula: String?
    val damageFormula: String?
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
external interface RecipeData {
    val name: String
    val basicIngredients: Int
    val specialIngredients: Int?
    val cookingLoreDC: Int
    val survivalDC: Int
    val uuid: String
    val icon: String?
    val level: Int
    val cost: String
    val rarity: String
    val isHomebrew: Boolean?
    val criticalSuccess: CookingOutcome
    val success: CookingOutcome
    val criticalFailure: CookingOutcome
    val favoriteMeal: CookingOutcome?
}

enum class HealMode {
    AFTER_CONSUMPTION,
    AFTER_REST,
    AFTER_CONSUMPTION_AND_REST,
}

enum class ReduceConditionMode {
    ALL,
    RANDOM
}


fun RecipeData.canBeFavoriteMeal() = name != "Basic Meal"

fun RecipeData.cookingCost(): FoodAmount =
    FoodAmount(
        basicIngredients = basicIngredients,
        specialIngredients = (specialIngredients ?: 0),
        rations = 1,
    )


fun RecipeData.discoverCost(): FoodAmount =
    cookingCost() * 2


@JsModule("./data/recipes.json")
external val recipes: Array<RecipeData>