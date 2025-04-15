package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.CrudApplication
import at.posselt.pfrpg2e.app.CrudColumn
import at.posselt.pfrpg2e.app.CrudData
import at.posselt.pfrpg2e.app.CrudItem
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.data.kingdom.KingdomAbility
import at.posselt.pfrpg2e.kingdom.KingdomActor
import at.posselt.pfrpg2e.kingdom.RawHeartland
import at.posselt.pfrpg2e.kingdom.getHeartlands
import at.posselt.pfrpg2e.kingdom.getKingdom
import at.posselt.pfrpg2e.kingdom.setKingdom
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.launch
import at.posselt.pfrpg2e.utils.t
import js.core.Void
import kotlin.js.Promise

class HeartlandManagement(
    private val kingdomActor: KingdomActor,
) : CrudApplication(
    title = t("kingdom.manageHeartlands"),
    debug = true,
    id = "kmManageHeartlands-${kingdomActor.uuid}"
) {
    override fun deleteEntry(id: String) = buildPromise {
        kingdomActor.getKingdom()?.let { kingdom ->
            kingdom.homebrewHeartlands = kingdom.homebrewHeartlands.filter { it.id != id }.toTypedArray()
            if (kingdom.heartland.type == id) {
                kingdom.heartland.type = null
            }
            kingdom.heartlandBlacklist = kingdom.heartlandBlacklist.filter { it == id }.toTypedArray()
            kingdomActor.setKingdom(kingdom)
            render()
        }
        undefined
    }

    override fun addEntry(): Promise<Void> = buildPromise {
        ModifyHeartland(
            afterSubmit = {
                kingdomActor.getKingdom()?.let { kingdom ->
                    kingdom.homebrewHeartlands = kingdom.homebrewHeartlands + it
                    kingdomActor.setKingdom(kingdom)
                }
                render()
            },
        ).launch()
        undefined
    }

    override fun editEntry(id: String) = buildPromise {
        ModifyHeartland(
            data = kingdomActor.getKingdom()?.homebrewHeartlands?.find { it.id == id },
            afterSubmit = { milestone ->
                kingdomActor.getKingdom()?.let { kingdom ->
                    kingdom.homebrewHeartlands = kingdom.homebrewHeartlands
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
            val disabledIds = kingdom.heartlandBlacklist.toSet()
            val homebrewIds = kingdom.homebrewHeartlands.map { it.id }.toSet()
            kingdom.getHeartlands()
                .sortedWith(compareBy(RawHeartland::name))
                .map { item ->
                    val canBeEdited = item.id in homebrewIds
                    CrudItem(
                        id = item.id,
                        name = item.name,
                        nameIsHtml = false,
                        additionalColumns = arrayOf(
                            CrudColumn(
                                escapeHtml = true,
                                value = KingdomAbility.fromString(item.boost)?.let { t(it) } ?: "",
                            )
                        ),
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
        arrayOf(t("kingdom.boost"))
    }

    override fun onParsedSubmit(value: CrudData): Promise<Void> = buildPromise {
        kingdomActor.getKingdom()?.let { kingdom ->
            val enabled = value.enabledIds.toSet()
            kingdom.heartlandBlacklist = kingdom.getHeartlands()
                .filter { it.id !in enabled }
                .map { it.id }
                .toTypedArray()
            if (kingdom.heartland.type !in enabled) {
                kingdom.heartland.type = null
            }
            kingdomActor.setKingdom(kingdom)
        }
        undefined
    }
}