package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.CrudApplication
import at.posselt.pfrpg2e.app.CrudData
import at.posselt.pfrpg2e.app.CrudItem
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.kingdom.RawFeat
import at.posselt.pfrpg2e.kingdom.getFeats
import at.posselt.pfrpg2e.kingdom.getKingdom
import at.posselt.pfrpg2e.kingdom.setKingdom
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.launch
import com.foundryvtt.pf2e.actor.PF2ENpc
import js.core.Void
import kotlin.js.Promise

class FeatManagement(
    private val kingdomActor: PF2ENpc,
) : CrudApplication(
    title = "Manage Feats",
    debug = true,
    id = "kmManageFeats-${kingdomActor.uuid}"
) {
    override fun deleteEntry(id: String) = buildPromise {
        kingdomActor.getKingdom()?.let { kingdom ->
            kingdom.homebrewFeats = kingdom.homebrewFeats.filter { it.id != id }.toTypedArray()
            kingdom.features = kingdom.features.map {
                if (it.featId == id) {
                    it.copy(featId = null, featRuinThresholdIncreases = emptyArray())
                } else {
                    it
                }
            }.toTypedArray()
            kingdom.featBlacklist = kingdom.featBlacklist.filter { it == id }.toTypedArray()
            kingdomActor.setKingdom(kingdom)
            render()
        }
        undefined
    }

    override fun addEntry(): Promise<Void> = buildPromise {
        ModifyFeat(
            afterSubmit = {
                kingdomActor.getKingdom()?.let { kingdom ->
                    kingdom.homebrewFeats = kingdom.homebrewFeats + it
                    kingdomActor.setKingdom(kingdom)
                }
                render()
            },
        ).launch()
        undefined
    }

    override fun editEntry(id: String) = buildPromise {
        ModifyFeat(
            data = kingdomActor.getKingdom()?.homebrewFeats?.find { it.id == id },
            afterSubmit = { milestone ->
                kingdomActor.getKingdom()?.let { kingdom ->
                    kingdom.homebrewFeats = kingdom.homebrewFeats
                        .filter { m -> m.id != milestone.id }
                        .toTypedArray() + milestone
                    kingdomActor.setKingdom(kingdom)
                }
                render()
            },
        ).launch()
        undefined
    }

    override fun getItems(): Promise<Array<CrudItem>> = buildPromise {
        kingdomActor.getKingdom()?.let { kingdom ->
            val disabledIds = kingdom.featBlacklist.toSet()
            val homebrewIds = kingdom.homebrewFeats.map { it.id }.toSet()
            kingdom.getFeats()
                .sortedWith(compareBy(RawFeat::name))
                .map { item ->
                    val canBeEdited = item.id in homebrewIds
                    CrudItem(
                        id = item.id,
                        name = item.name,
                        nameIsHtml = false,
                        additionalColumns = arrayOf(),
                        enable = CheckboxInput(
                            value = item.id !in disabledIds,
                            label = "Enable",
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
            kingdom.featBlacklist = kingdom.getFeats()
                .filter { it.id !in enabled }
                .map { it.id }
                .toTypedArray()
            kingdom.features = kingdom.features.map {
                if (it.featId !in enabled) {
                    it.copy(featId = null, featRuinThresholdIncreases = emptyArray())
                } else {
                    it
                }
            }.toTypedArray()
            kingdomActor.setKingdom(kingdom)
        }
        undefined
    }
}