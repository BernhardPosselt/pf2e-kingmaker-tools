package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.CrudApplication
import at.posselt.pfrpg2e.app.CrudColumn
import at.posselt.pfrpg2e.app.CrudData
import at.posselt.pfrpg2e.app.CrudItem
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.kingdom.RawGovernment
import at.posselt.pfrpg2e.kingdom.getFeats
import at.posselt.pfrpg2e.kingdom.getGovernments
import at.posselt.pfrpg2e.kingdom.getKingdom
import at.posselt.pfrpg2e.kingdom.setKingdom
import at.posselt.pfrpg2e.kingdom.sheet.resetAbilityBoosts
import at.posselt.pfrpg2e.toLabel
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.launch
import com.foundryvtt.pf2e.actor.PF2ENpc
import js.core.Void
import kotlin.js.Promise

class GovernmentManagement(
    private val kingdomActor: PF2ENpc,
) : CrudApplication(
    title = "Manage Governments",
    debug = true,
    id = "kmManageGovernments"
) {
    override fun deleteEntry(id: String) = buildPromise {
        kingdomActor.getKingdom()?.let { kingdom ->
            kingdom.homebrewGovernments = kingdom.homebrewGovernments.filter { it.id != id }.toTypedArray()
            if (kingdom.government.type == id) {
                kingdom.government.type = null
                resetAbilityBoosts(kingdom.government.abilityBoosts)
            }
            kingdomActor.setKingdom(kingdom)
            render()
        }
        undefined
    }

    override fun addEntry(): Promise<Void> = buildPromise {
        val feats = kingdomActor.getKingdom()?.getFeats() ?: emptyArray()
        ModifyGovernment(
            feats = feats,
            afterSubmit = {
                kingdomActor.getKingdom()?.let { kingdom ->
                    kingdom.homebrewGovernments = kingdom.homebrewGovernments + it
                    kingdomActor.setKingdom(kingdom)
                }
                render()
            },
        ).launch()
        undefined
    }

    override fun editEntry(id: String) = buildPromise {
        val feats = kingdomActor.getKingdom()?.getFeats() ?: emptyArray()
        ModifyGovernment(
            feats = feats,
            data = kingdomActor.getKingdom()?.homebrewGovernments?.find { it.id == id },
            afterSubmit = { government ->
                kingdomActor.getKingdom()?.let { kingdom ->
                    kingdom.homebrewGovernments = kingdom.homebrewGovernments
                        .filter { m -> m.id != government.id }
                        .toTypedArray() + government
                    if (kingdom.government.type == government.id) {
                        resetAbilityBoosts(kingdom.government.abilityBoosts)
                    }
                    kingdomActor.setKingdom(kingdom)
                }
                render()
            },
        ).launch()
        undefined
    }

    override fun getItems(): Promise<Array<CrudItem>> = buildPromise {
        kingdomActor.getKingdom()?.let { kingdom ->
            val disabledIds = kingdom.governmentBlacklist.toSet()
            val homebrewIds = kingdom.homebrewGovernments.map { it.id }.toSet()
            kingdom.getGovernments()
                .sortedWith(compareBy(RawGovernment::name))
                .map { item ->
                    val canBeEdited = item.id in homebrewIds
                    CrudItem(
                        id = item.id,
                        name = item.name,
                        nameIsHtml = false,
                        additionalColumns = arrayOf(
                            CrudColumn(
                                escapeHtml = true,
                                value = item.boosts.joinToString(", ") { it.toLabel() },
                            ),
                            CrudColumn(
                                escapeHtml = true,
                                value = item.skillProficiencies.joinToString(", ") { it.toLabel() },
                            ),
                        ),
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
        arrayOf("Boosts", "Skills")
    }

    override fun onParsedSubmit(value: CrudData): Promise<Void> = buildPromise {
        kingdomActor.getKingdom()?.let { kingdom ->
            val enabled = value.enabledIds.toSet()
            kingdom.governmentBlacklist = kingdom.getGovernments()
                .filter { it.id !in enabled }
                .map { it.id }
                .toTypedArray()
            if (kingdom.government.type !in enabled) {
                kingdom.government.type = null
            }
            kingdomActor.setKingdom(kingdom)
        }
        undefined
    }
}