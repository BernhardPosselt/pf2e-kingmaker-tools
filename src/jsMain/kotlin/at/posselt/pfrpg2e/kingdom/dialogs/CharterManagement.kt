package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.CrudApplication
import at.posselt.pfrpg2e.app.CrudColumn
import at.posselt.pfrpg2e.app.CrudData
import at.posselt.pfrpg2e.app.CrudItem
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.data.kingdom.KingdomAbility
import at.posselt.pfrpg2e.kingdom.KingdomActor
import at.posselt.pfrpg2e.kingdom.RawCharter
import at.posselt.pfrpg2e.kingdom.getCharters
import at.posselt.pfrpg2e.kingdom.getKingdom
import at.posselt.pfrpg2e.kingdom.setKingdom
import at.posselt.pfrpg2e.kingdom.sheet.resetAbilityBoosts
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.launch
import at.posselt.pfrpg2e.utils.t
import js.core.Void
import kotlin.js.Promise

class CharterManagement(
    private val kingdomActor: KingdomActor,
) : CrudApplication(
    title = "Manage Charters",
    debug = true,
    id = "kmManageCharters-${kingdomActor.uuid}"
) {
    override fun deleteEntry(id: String) = buildPromise {
        kingdomActor.getKingdom()?.let { kingdom ->
            kingdom.homebrewCharters = kingdom.homebrewCharters.filter { it.id != id }.toTypedArray()
            if (kingdom.charter.type == id) {
                kingdom.charter.type = null
                resetAbilityBoosts(kingdom.charter.abilityBoosts)
            }
            kingdom.charterBlacklist = kingdom.charterBlacklist.filter { it == id }.toTypedArray()
            kingdomActor.setKingdom(kingdom)
            render()
        }
        undefined
    }

    override fun addEntry(): Promise<Void> = buildPromise {
        ModifyCharter(
            afterSubmit = {
                kingdomActor.getKingdom()?.let { kingdom ->
                    kingdom.homebrewCharters = kingdom.homebrewCharters + it
                    kingdomActor.setKingdom(kingdom)
                }
                render()
            },
        ).launch()
        undefined
    }

    override fun editEntry(id: String) = buildPromise {
        ModifyCharter(
            data = kingdomActor.getKingdom()?.homebrewCharters?.find { it.id == id },
            afterSubmit = { charter ->
                kingdomActor.getKingdom()?.let { kingdom ->
                    kingdom.homebrewCharters = kingdom.homebrewCharters
                        .filter { m -> m.id != charter.id }
                        .toTypedArray() + charter
                    if (kingdom.charter.type == charter.id) {
                        resetAbilityBoosts(kingdom.charter.abilityBoosts)
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
            val disabledIds = kingdom.charterBlacklist.toSet()
            val homebrewIds = kingdom.homebrewCharters.map { it.id }.toSet()
            kingdom.getCharters()
                .sortedWith(compareBy(RawCharter::name))
                .map { item ->
                    val canBeEdited = item.id in homebrewIds
                    CrudItem(
                        id = item.id,
                        name = item.name,
                        nameIsHtml = false,
                        additionalColumns = arrayOf(
                            CrudColumn(
                                escapeHtml = true,
                                value = item.boost?.let { KingdomAbility.fromString(it) }?.let { t(it) } ?: "",
                            ),
                            CrudColumn(
                                escapeHtml = true,
                                value = item.flaw?.let { KingdomAbility.fromString(it) }?.let { t(it) } ?: "",
                            )
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
        arrayOf("Boost", "Flaw")
    }

    override fun onParsedSubmit(value: CrudData): Promise<Void> = buildPromise {
        kingdomActor.getKingdom()?.let { kingdom ->
            val enabled = value.enabledIds.toSet()
            kingdom.charterBlacklist = kingdom.getCharters()
                .filter { it.id !in enabled }
                .map { it.id }
                .toTypedArray()
            if (kingdom.charter.type !in enabled) {
                kingdom.charter.type = null
            }
            kingdomActor.setKingdom(kingdom)
        }
        undefined
    }
}