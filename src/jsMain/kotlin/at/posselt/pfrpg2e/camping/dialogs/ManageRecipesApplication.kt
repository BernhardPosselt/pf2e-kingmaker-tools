package at.posselt.pfrpg2e.camping.dialogs

import at.posselt.pfrpg2e.app.CrudApplication
import at.posselt.pfrpg2e.app.CrudColumn
import at.posselt.pfrpg2e.app.CrudData
import at.posselt.pfrpg2e.app.CrudItem
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.camping.CampingActor
import at.posselt.pfrpg2e.camping.RecipeData
import at.posselt.pfrpg2e.camping.buildFoodCost
import at.posselt.pfrpg2e.camping.cookingCost
import at.posselt.pfrpg2e.camping.format
import at.posselt.pfrpg2e.camping.getAllRecipes
import at.posselt.pfrpg2e.camping.getCamping
import at.posselt.pfrpg2e.camping.getCompendiumFoodItems
import at.posselt.pfrpg2e.camping.getTotalCarriedFood
import at.posselt.pfrpg2e.camping.setCamping
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.buildUuid
import at.posselt.pfrpg2e.utils.launch
import at.posselt.pfrpg2e.utils.t
import at.posselt.pfrpg2e.utils.tpl
import com.foundryvtt.core.AnyMutableObject
import com.foundryvtt.core.Game
import com.foundryvtt.core.ui.TextEditor
import js.array.toTypedArray
import js.core.Void
import kotlinx.coroutines.await
import kotlin.js.Promise

@JsExport
class ManageRecipesApplication(
    private val game: Game,
    private val actor: CampingActor,
) : CrudApplication(
    id = "kmManageRecipes-${actor.uuid}",
    title = t("camping.manageRecipes"),
    debug = true,
) {
    override fun deleteEntry(id: String) = buildPromise {
        actor.getCamping()?.let { camping ->
            camping.cooking.knownRecipes = camping.cooking.knownRecipes.filter { it != id }.toTypedArray()
            camping.cooking.homebrewMeals = camping.cooking.homebrewMeals.filter { it.id != id }.toTypedArray()
            camping.cooking.actorMeals.forEach {
                if (it.chosenMeal == id) {
                    it.chosenMeal = "nothing"
                }
                if (it.favoriteMeal == id) {
                    it.favoriteMeal = null
                }
            }
            camping.cooking.results = camping.cooking.results.asSequence()
                .filter { it.recipeId != id }
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
            actor.getCamping()?.cooking?.homebrewMeals?.find { it.id == id },
            afterSubmit = { render() },
        ).launch()
        undefined
    }

    override fun getItems(): Promise<Array<CrudItem>> = buildPromise {
        actor.getCamping()?.let { camping ->
            val foodItems = getCompendiumFoodItems()
            val total = camping.getTotalCarriedFood(actor, foodItems)
            val learnedRecipes = camping.cooking.knownRecipes.toSet()
            camping.getAllRecipes()
                .sortedWith(compareBy(RecipeData::level, RecipeData::name))
                .map { recipe ->
                    val id = recipe.id
                    val link = TextEditor.enrichHTML(buildUuid(recipe.uuid, recipe.name)).await()
                    val editable = recipe.isHomebrew == true
                    val enabled = learnedRecipes.contains(id)
                    val cook = tpl(
                        "components/food-cost/food-cost.hbs",
                        buildFoodCost(recipe.cookingCost(), total, foodItems).unsafeCast<AnyMutableObject>().apply {
                            this["title"] = t("camping.consumed")
                        },
                    )
                    CrudItem(
                        nameIsHtml = true,
                        id = id,
                        name = link,
                        additionalColumns = arrayOf(
                            CrudColumn(value = recipe.rarity, escapeHtml = true),
                            CrudColumn(value = recipe.level.toString(), escapeHtml = true),
                            CrudColumn(value = recipe.cookingLoreDC.toString(), escapeHtml = true),
                            CrudColumn(value = cook, escapeHtml = false),
                            CrudColumn(value = recipe.cost.format(), escapeHtml = true),
                        ),
                        enable = CheckboxInput(
                            value = enabled,
                            label = t("applications.enable"),
                            hideLabel = true,
                            name = "enabledIds.$id",
                            disabled = id == "basic-meal" || id == "hearty-meal",
                        ).toContext(),
                        canBeEdited = editable,
                        canBeDeleted = editable,
                    )
                }.toTypedArray()
        } ?: emptyArray()
    }

    override fun getHeadings(): Promise<Array<String>> = buildPromise {
        arrayOf(t("enums.rarity"), t("applications.level"), t("applications.dc"), t("camping.cookingCost"), t("camping.purchaseCost"))
    }

    override fun onParsedSubmit(value: CrudData): Promise<Void> = buildPromise {
        actor.getCamping()?.let { camping ->
            val enabledRecipes = value.enabledIds + arrayOf("hearty-meal", "basic-meal")
            camping.cooking.knownRecipes = enabledRecipes
            camping.cooking.actorMeals.forEach {
                if (it.chosenMeal !in enabledRecipes) it.chosenMeal = "nothing"
                if (it.favoriteMeal !in enabledRecipes) it.favoriteMeal = null
            }
            camping.cooking.results = camping.cooking.results.asSequence()
                .filter { it.recipeId in enabledRecipes }
                .toTypedArray()
            actor.setCamping(camping)
        }
        undefined
    }
}
