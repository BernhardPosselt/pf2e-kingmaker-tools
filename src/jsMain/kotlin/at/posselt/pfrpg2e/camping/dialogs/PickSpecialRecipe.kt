package at.posselt.pfrpg2e.camping.dialogs

import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.RadioInput
import at.posselt.pfrpg2e.app.awaitablePrompt
import at.posselt.pfrpg2e.camping.CampingData
import at.posselt.pfrpg2e.camping.FoodCost
import at.posselt.pfrpg2e.camping.RecipeData
import at.posselt.pfrpg2e.camping.buildFoodCost
import at.posselt.pfrpg2e.camping.discoverCost
import at.posselt.pfrpg2e.camping.findCurrentRegion
import at.posselt.pfrpg2e.camping.getAllRecipes
import at.posselt.pfrpg2e.camping.getCompendiumFoodItems
import at.posselt.pfrpg2e.camping.getTotalCarriedFood
import at.posselt.pfrpg2e.utils.buildUuid
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.ui.TextEditor
import com.foundryvtt.pf2e.actor.PF2EParty
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.js.JsPlainObject

@JsPlainObject
private external interface LearnSpecialRecipeData {
    val recipe: String
}

@JsPlainObject
private external interface RecipeContextRow {
    val label: String
    val dc: Int
    val discoverCost: FoodCost
    val input: FormElementContext
}

@JsPlainObject
private external interface LearnSpecialRecipeContext {
    val formRows: Array<RecipeContextRow>
}

suspend fun pickSpecialRecipe(
    partyActor: PF2EParty?,
    camping: CampingData
): RecipeData? = coroutineScope {
    val learnedRecipes = camping.cooking.knownRecipes.toSet()
    val allRecipes = camping.getAllRecipes()
    val items = getCompendiumFoodItems()
    val totalItems = camping.getTotalCarriedFood(partyActor, items)
    val rows = allRecipes.asSequence()
        .filter { it.level < (camping.findCurrentRegion()?.level ?: 0) }
        .filter { it.name !in learnedRecipes }
        .sortedBy { it.level }
        .mapIndexed { index, recipe ->
            async {
                val label = TextEditor.enrichHTML(buildUuid(recipe.uuid, recipe.name)).await()
                RecipeContextRow(
                    label = label,
                    dc = recipe.cookingLoreDC,
                    discoverCost = buildFoodCost(
                        amount = recipe.discoverCost(),
                        totalAmount = totalItems,
                        items = items
                    ),
                    input = RadioInput(
                        name = "recipe",
                        checked = index == 0,
                        value = recipe.name,
                        label = recipe.name,
                        escapeLabel = false,
                        hideLabel = true,
                    ).toContext(),
                )
            }
        }.toList()
        .awaitAll()
        .toTypedArray()
    awaitablePrompt<LearnSpecialRecipeData, RecipeData?>(
        title = "Recipes learnable in Zone",
        templatePath = "applications/camping/learn-recipe.hbs",
        templateContext = LearnSpecialRecipeContext(
            formRows = rows,
        ).unsafeCast<AnyObject>()
    ) { data ->
        allRecipes.find { it.name == data.recipe }
    }
}