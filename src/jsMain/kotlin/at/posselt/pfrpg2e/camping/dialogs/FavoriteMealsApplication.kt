package at.posselt.pfrpg2e.camping.dialogs

import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.ValidatedHandlebarsContext
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.HiddenInput
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.camping.CampingActor
import at.posselt.pfrpg2e.camping.canBeFavoriteMeal
import at.posselt.pfrpg2e.camping.getActorsInCamp
import at.posselt.pfrpg2e.camping.getAllRecipes
import at.posselt.pfrpg2e.camping.getCamping
import at.posselt.pfrpg2e.camping.setCamping
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.t
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.Game
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.abstract.DocumentConstructionContext
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.data.dsl.buildSchema
import js.core.Void
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.js.Promise

@JsExport
class FavoriteMealDataModel(
    value: AnyObject,
    options: DocumentConstructionContext?
) : DataModel(value, options) {
    companion object {
        @JsStatic
        fun defineSchema() = buildSchema {
            array("meals") {
                schema {
                    string("actorUuid")
                    string("favoriteMeal", nullable = true)
                }
            }
        }
    }
}

@JsPlainObject
external interface FavoriteMealChoice {
    val actorUuid: String
    val favoriteMeal: String?
}

@JsPlainObject
external interface FavoriteMealContext : ValidatedHandlebarsContext {
    val formRows: Array<FormElementContext>
}

@JsPlainObject
external interface FavoriteMealSubmitData {
    val meals: Array<FavoriteMealChoice>
}


@JsExport
class FavoriteMealsApplication(
    private val game: Game,
    private val actor: CampingActor,
) : FormApp<FavoriteMealContext, FavoriteMealSubmitData>(
    title = t("camping.favoriteMeals"),
    template = "components/forms/application-form.hbs",
    debug = true,
    dataModel = FavoriteMealDataModel::class.js,
    id = "kmFavoriteMeals-${actor.uuid}"
) {
    private var meals: List<FavoriteMealChoice> = actor.getCamping()?.let {
        it.cooking.actorMeals.map { actorMeal ->
            FavoriteMealChoice(actorUuid = actorMeal.actorUuid, favoriteMeal = actorMeal.favoriteMeal)
        }
    } ?: emptyList()

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (val action = target.dataset["action"]) {
            "km-save" -> {
                buildPromise {
                    actor.getCamping()?.let { camping ->
                        val allowedActorUuids = camping.getActorsInCamp()
                            .filter { game.user.isGM || it.isOwner }
                            .map { it.uuid }
                            .toSet()
                        val mealsByActorUuid = meals.associateBy { it.actorUuid }
                        camping.cooking.actorMeals
                            .filter { it.actorUuid in allowedActorUuids }
                            .forEach {
                                it.favoriteMeal = mealsByActorUuid[it.actorUuid]?.favoriteMeal
                            }
                        actor.setCamping(camping)
                    }
                    close()
                }
            }

            else -> console.log(action)
        }
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<FavoriteMealContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        val camping = actor.getCamping()
        val actors = camping
            ?.getActorsInCamp()
            ?.filter { game.user.isGM || it.isOwner }
            ?.associateBy { it.uuid } ?: emptyMap()
        val mealChoices = camping
            ?.getAllRecipes()
            ?.filter { it.canBeFavoriteMeal() }
            ?.sortedBy { it.name }
            ?.map { SelectOption(label = it.name, value = it.id) }
            ?: emptyList()

        FavoriteMealContext(
            partId = parent.partId,
            isFormValid = isFormValid,
            formRows = meals
                .filter { it.actorUuid in actors }
                .flatMapIndexed { index, meal ->
                    val name = actors[meal.actorUuid]?.name
                    listOf(
                        HiddenInput(
                            name = "meals.$index.actorUuid",
                            value = meal.actorUuid,
                        ).toContext(),
                        Select(
                            label = name ?: "",
                            name = "meals.$index.favoriteMeal",
                            value = meal.favoriteMeal,
                            options = mealChoices,
                            stacked = false,
                            required = false,
                        ).toContext(),
                    )
                }
                .toTypedArray()
        )
    }

    override fun onParsedSubmit(value: FavoriteMealSubmitData): Promise<Void> = buildPromise {
        meals = value.meals.toList()
        undefined
    }

}