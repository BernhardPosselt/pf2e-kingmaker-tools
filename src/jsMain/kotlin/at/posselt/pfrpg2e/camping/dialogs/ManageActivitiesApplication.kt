package at.posselt.pfrpg2e.camping.dialogs

import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.CrudApplication
import at.posselt.pfrpg2e.app.CrudData
import at.posselt.pfrpg2e.app.CrudItem
import at.posselt.pfrpg2e.camping.*
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.launch
import com.foundryvtt.core.Game
import com.foundryvtt.pf2e.actor.PF2ENpc
import js.core.Void
import kotlin.js.Promise

@JsExport
class ManageActivitiesApplication(
    private val game: Game,
    private val actor: PF2ENpc,
) : CrudApplication(
    title = "Manage Activities",
    debug = true,
    id = "kmManageActivities"
) {
    override fun deleteEntry(id: String) = buildPromise {
        actor.getCamping()?.let { camping ->
            camping.homebrewCampingActivities =
                camping.homebrewCampingActivities.filter { it.name != id }.toTypedArray()
            camping.campingActivities = camping.campingActivities.filter { it.activity != id }.toTypedArray()
            camping.lockedActivities = camping.lockedActivities.filter { it != id }.toTypedArray()
            actor.setCamping(camping)
            render()
        }
        undefined
    }

    override fun addEntry(): Promise<Void> = buildPromise {
        ActivityApplication(
            game,
            actor,
            afterSubmit = { render() },
        ).launch()
        undefined
    }

    override fun editEntry(id: String) = buildPromise {
        ActivityApplication(
            game,
            actor,
            actor.getCamping()?.homebrewCampingActivities?.find { it.name == id },
            afterSubmit = { render() },
        ).launch()
        undefined
    }

    override fun getItems(): Promise<Array<CrudItem>> = buildPromise {
        actor.getCamping()?.let { camping ->
            val locked = camping.lockedActivities.toSet()
            camping.getAllActivities()
                .sortedWith(compareBy(CampingActivityData::name))
                .map { activity ->
                    val name = activity.name
                    val canBeEdited = activity.isHomebrew
                    val enabled = !locked.contains(name)
                    CrudItem(
                        id = name,
                        name = name,
                        nameIsHtml = false,
                        additionalColumns = emptyArray(),
                        enable = CheckboxInput(
                            value = enabled,
                            label = "Enable",
                            hideLabel = true,
                            disabled = activity.isPrepareCampsite() || activity.isCookMeal(),
                            name = "enabledIds.$name",
                        ).toContext(),
                        canBeEdited = canBeEdited,
                        canBeDeleted = canBeEdited,
                    )
                }.toTypedArray()
        } ?: emptyArray()
    }

    override fun getHeadings(): Promise<Array<String>> = buildPromise {
        emptyArray()
    }

    override fun onParsedSubmit(value: CrudData): Promise<Void> = buildPromise {
        val enabled = value.enabledIds.toSet() + setOf("Prepare Campsite", "Cook Meal")
        actor.getCamping()?.let { camping ->
            camping.lockedActivities = camping.getAllActivities()
                .filter { !enabled.contains(it.name) }
                .map { it.name }
                .toTypedArray()
            actor.setCamping(camping)
        }
        undefined
    }
}