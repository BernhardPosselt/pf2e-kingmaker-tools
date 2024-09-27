package at.posselt.pfrpg2e.camping.dialogs

import at.posselt.pfrpg2e.actor.party
import at.posselt.pfrpg2e.app.*
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.camping.RecipeData
import at.posselt.pfrpg2e.camping.buildFoodCost
import at.posselt.pfrpg2e.camping.cookingCost
import at.posselt.pfrpg2e.camping.getAllRecipes
import at.posselt.pfrpg2e.camping.getCamping
import at.posselt.pfrpg2e.camping.getCompendiumFoodItems
import at.posselt.pfrpg2e.camping.getTotalCarriedFood
import at.posselt.pfrpg2e.camping.setCamping
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.buildUuid
import at.posselt.pfrpg2e.utils.launch
import at.posselt.pfrpg2e.utils.toMutableRecord
import at.posselt.pfrpg2e.utils.tpl
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.Game
import com.foundryvtt.core.ui.TextEditor
import com.foundryvtt.pf2e.actor.PF2ENpc
import js.array.toTypedArray
import js.core.Void
import kotlinx.coroutines.await
import kotlin.js.Promise

@JsExport
class ManageRecipesApplication(
    private val game: Game,
    private val actor: PF2ENpc,
) : CrudApplication(
    id = "kmManageRecipes",
    title = "Manage Recipes",
    debug = true,
) {
    override fun deleteEntry(id: String) = buildPromise {
        actor.getCamping()?.let { camping ->
            camping.cooking.knownRecipes = camping.cooking.knownRecipes.filter { it != id }.toTypedArray()
            camping.cooking.homebrewMeals = camping.cooking.homebrewMeals.filter { it.name != id }.toTypedArray()
            camping.cooking.actorMeals.forEach {
                if (it.chosenMeal == id) {
                    it.chosenMeal = "nothing"
                }
                if (it.favoriteMeal == id) {
                    it.favoriteMeal = null
                }
            }
            camping.cooking.results = camping.cooking.results.asSequence()
                .filter { it.recipeName != id }
                .toTypedArray()
            actor.setCamping(camping)
            render()
        }
        undefined
    }

    override fun addEntry(): Promise<Void> = buildPromise {
        RecipeApplication(
            game,
            actor,
            afterSubmit = { render() },
        ).launch()
        undefined
    }

    override fun editEntry(id: String) = buildPromise {
        RecipeApplication(
            game,
            actor,
            actor.getCamping()?.cooking?.homebrewMeals?.find { it.name == id },
            afterSubmit = { render() },
        ).launch()
        undefined
    }

    override fun getItems(): Promise<Array<CrudItem>> = buildPromise {
        actor.getCamping()?.let { camping ->
            val foodItems = getCompendiumFoodItems()
            val total = camping.getTotalCarriedFood(game.party(), foodItems)
            val learnedRecipes = camping.cooking.knownRecipes.toSet()
            camping.getAllRecipes()
                .sortedWith(compareBy(RecipeData::level, RecipeData::name))
                .map { recipe ->
                    val recipeName = recipe.name
                    val link = TextEditor.enrichHTML(buildUuid(recipe.uuid, recipeName)).await()
                    val editable = recipe.isHomebrew ?: false
                    val enabled = learnedRecipes.contains(recipeName)
                    val cook = tpl(
                        "components/food-cost/food-cost.hbs",
                        buildFoodCost(recipe.cookingCost(), total, foodItems).unsafeCast<AnyObject>(),
                    )
                    CrudItem(
                        nameIsHtml = true,
                        id = recipeName,
                        name = link,
                        additionalColumns = arrayOf(
                            CrudColumn(value = recipe.rarity, escapeHtml = true),
                            CrudColumn(value = recipe.level.toString(), escapeHtml = true),
                            CrudColumn(value = recipe.cookingLoreDC.toString(), escapeHtml = true),
                            CrudColumn(value = cook, escapeHtml = false),
                            CrudColumn(value = recipe.cost, escapeHtml = true),
                        ),
                        enable = CheckboxInput(
                            value = enabled,
                            label = "Enable",
                            hideLabel = true,
                            name = "enabledIds.$recipeName",
                            disabled = recipeName == "Basic Meal" || recipeName == "Hearty Meal",
                        ).toContext(),
                        canBeEdited = editable,
                        canBeDeleted = editable,
                    )
                }.toTypedArray()
        } ?: emptyArray()
    }

    override fun getHeadings(): Promise<Array<String>> = buildPromise {
        arrayOf("Rarity", "Level", "DC", "Cooking Cost", "Purchase Cost")
    }

    override fun onParsedSubmit(value: CrudData): Promise<Void> = buildPromise {
        console.log("saving", value)
        actor.getCamping()?.let { camping ->
            val enabledRecipes = value.enabledIds + arrayOf("Hearty Meal", "Basic Meal")
            camping.cooking.knownRecipes = enabledRecipes
            camping.cooking.actorMeals.forEach {
                if (it.chosenMeal !in enabledRecipes) it.chosenMeal = "nothing"
                if (it.favoriteMeal !in enabledRecipes) it.favoriteMeal = null
            }
            camping.cooking.results = camping.cooking.results.asSequence()
                .filter { it.recipeName in enabledRecipes }
                .toTypedArray()
            actor.setCamping(camping)
        }
        undefined
    }
}
