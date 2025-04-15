package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.CrudApplication
import at.posselt.pfrpg2e.app.CrudData
import at.posselt.pfrpg2e.app.CrudItem
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.kingdom.KingdomActor
import at.posselt.pfrpg2e.kingdom.RawKingdomEvent
import at.posselt.pfrpg2e.kingdom.getEvents
import at.posselt.pfrpg2e.kingdom.getKingdom
import at.posselt.pfrpg2e.kingdom.setKingdom
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.launch
import at.posselt.pfrpg2e.utils.t
import js.core.Void
import kotlin.js.Promise

class KingdomEventManagement(
    private val kingdomActor: KingdomActor,
) : CrudApplication(
    title = t("kingdom.manageKingdomEvents"),
    debug = true,
    id = "kmManageKingdomEvents-${kingdomActor.uuid}"
) {
    override fun deleteEntry(id: String) = buildPromise {
        kingdomActor.getKingdom()?.let { kingdom ->
            kingdom.homebrewKingdomEvents = kingdom.homebrewKingdomEvents.filter { it.id != id }.toTypedArray()
            kingdom.ongoingEvents = kingdom.ongoingEvents.filter { it.id != id }.toTypedArray()
            kingdom.kingdomEventBlacklist = kingdom.kingdomEventBlacklist.filter { it == id }.toTypedArray()
            kingdomActor.setKingdom(kingdom)
            render()
        }
        undefined
    }

    override fun addEntry(): Promise<Void> = buildPromise {
        ModifyKingdomEvent(
            afterSubmit = {
                kingdomActor.getKingdom()?.let { kingdom ->
                    kingdom.homebrewKingdomEvents = kingdom.homebrewKingdomEvents + it
                    kingdomActor.setKingdom(kingdom)
                }
                render()
            },
        ).launch()
        undefined
    }

    override fun editEntry(id: String) = buildPromise {
        ModifyKingdomEvent(
            data = kingdomActor.getKingdom()?.homebrewKingdomEvents?.find { it.id == id },
            afterSubmit = { event ->
                kingdomActor.getKingdom()?.let { kingdom ->
                    kingdom.homebrewKingdomEvents = kingdom.homebrewKingdomEvents
                        .filter { m -> m.id != event.id }
                        .toTypedArray() + event
                    kingdomActor.setKingdom(kingdom)
                }
                render()
            },
        ).launch()
        undefined
    }

    override fun getItems(): Promise<Array<CrudItem>> = buildPromise {
        kingdomActor.getKingdom()?.let { kingdom ->
            val disabledIds = kingdom.kingdomEventBlacklist.toSet()
            val homebrewIds = kingdom.homebrewKingdomEvents.map { it.id }.toSet()
            kingdom.getEvents()
                .sortedWith(compareBy(RawKingdomEvent::name))
                .map { item ->
                    val canBeEdited = item.id in homebrewIds
                    CrudItem(
                        id = item.id,
                        name = item.name,
                        nameIsHtml = false,
                        additionalColumns = arrayOf(),
                        enable = CheckboxInput(
                            value = item.id !in disabledIds,
                            label = t("applications.enable"),
                            hideLabel = true,
                            name = "enabledIds.${item.id}",
                        ).toContext(),
                        canBeEdited = canBeEdited,
                        canBeDeleted = canBeEdited,
                    )
                }.toTypedArray()
        } ?: emptyArray()
    }

    override fun getHeadings(): Promise<Array<String>> = buildPromise {
        arrayOf()
    }

    override fun onParsedSubmit(value: CrudData): Promise<Void> = buildPromise {
        kingdomActor.getKingdom()?.let { kingdom ->
            val enabled = value.enabledIds.toSet()
            kingdom.kingdomEventBlacklist = kingdom.getEvents()
                .filter { it.id !in enabled }
                .map { it.id }
                .toTypedArray()
            kingdomActor.setKingdom(kingdom)
        }
        undefined
    }
}