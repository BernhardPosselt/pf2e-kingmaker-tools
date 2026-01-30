package at.posselt.pfrpg2e.camping.dialogs

import at.posselt.pfrpg2e.app.CrudApplication
import at.posselt.pfrpg2e.app.CrudColumn
import at.posselt.pfrpg2e.app.CrudData
import at.posselt.pfrpg2e.app.CrudItem
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.camping.ActorMeal
import at.posselt.pfrpg2e.camping.CampingActor
import at.posselt.pfrpg2e.camping.RecipeData
import at.posselt.pfrpg2e.camping.buildFoodCost
import at.posselt.pfrpg2e.camping.cookingCost
import at.posselt.pfrpg2e.camping.format
import at.posselt.pfrpg2e.camping.getAllRecipes
import at.posselt.pfrpg2e.camping.getCamping
import at.posselt.pfrpg2e.camping.getCompendiumFoodItems
import at.posselt.pfrpg2e.camping.getTotalCarriedFood
import at.posselt.pfrpg2e.camping.typedCampingUpdate
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.buildUuid
import at.posselt.pfrpg2e.utils.launch
import at.posselt.pfrpg2e.utils.t
import at.posselt.pfrpg2e.utils.tpl
import com.foundryvtt.core.AnyMutableObject
import com.foundryvtt.core.Game
import com.foundryvtt.core.applications.ux.TextEditor.TextEditor
import js.core.Void
import js.objects.Object
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
        actor.typedCampingUpdate { camping ->
            cooking.knownRecipes.set(camping.cooking.knownRecipes.filter { it != id }.toTypedArray())
            cooking.homebrewMeals.set(camping.cooking.homebrewMeals.filter { it.id != id }.toTypedArray())
            cooking.actorMeals.set(camping.cooking.actorMeals.map {
                ActorMeal(
                    actorUuid = it.actorUuid,
                    favoriteMeal = if (it.favoriteMeal == id) null else it.favoriteMeal,
                    chosenMeal = if (it.chosenMeal == id) "nothing" else it.chosenMeal
                )
            }.toTypedArray())
            cooking.results.deleteEntry(id)
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
        val enabledRecipes = value.enabledIds + arrayOf("hearty-meal", "basic-meal")
        actor.typedCampingUpdate { camping ->
            cooking.knownRecipes.set(enabledRecipes)
            cooking.actorMeals.set(camping.cooking.actorMeals.map {
                if (it.chosenMeal !in enabledRecipes) it.chosenMeal = "nothing"
                if (it.favoriteMeal !in enabledRecipes) it.favoriteMeal = null
                it
            }.toTypedArray())
            val idsToRemove = Object.keys(camping.cooking.results)
                .filter { it !in enabledRecipes }
                .toSet()
            cooking.results.deleteEntries(idsToRemove)
        }
        undefined
    }
}
