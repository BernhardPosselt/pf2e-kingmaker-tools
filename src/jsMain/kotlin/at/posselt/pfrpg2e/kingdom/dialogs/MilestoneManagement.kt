package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.CrudApplication
import at.posselt.pfrpg2e.app.CrudColumn
import at.posselt.pfrpg2e.app.CrudData
import at.posselt.pfrpg2e.app.CrudItem
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.kingdom.KingdomActor
import at.posselt.pfrpg2e.kingdom.RawMilestone
import at.posselt.pfrpg2e.kingdom.data.MilestoneChoice
import at.posselt.pfrpg2e.kingdom.getKingdom
import at.posselt.pfrpg2e.kingdom.getMilestones
import at.posselt.pfrpg2e.kingdom.setKingdom
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.launch
import js.core.Void
import kotlin.js.Promise

class MilestoneManagement(
    private val kingdomActor: KingdomActor,
) : CrudApplication(
    title = "Manage Milestones",
    debug = true,
    id = "kmManageMilestones-${kingdomActor.uuid}"
) {
    override fun deleteEntry(id: String) = buildPromise {
        kingdomActor.getKingdom()?.let { kingdom ->
            kingdom.homebrewMilestones = kingdom.homebrewMilestones.filter { it.id != id }.toTypedArray()
            kingdom.milestones = kingdom.milestones.filter { it.id != id }.toTypedArray()
            kingdomActor.setKingdom(kingdom)
            render()
        }
        undefined
    }

    override fun addEntry(): Promise<Void> = buildPromise {
        ModifyMilestone(
            afterSubmit = {
                kingdomActor.getKingdom()?.let { kingdom ->
                    kingdom.homebrewMilestones = kingdom.homebrewMilestones + it
                    kingdom.milestones = kingdom.milestones + MilestoneChoice(
                        id = it.id,
                        completed = false,
                        enabled = true,
                    )
                    kingdomActor.setKingdom(kingdom)
                }
                render()
            },
        ).launch()
        undefined
    }

    override fun editEntry(id: String) = buildPromise {
        ModifyMilestone(
            data = kingdomActor.getKingdom()?.homebrewMilestones?.find { it.id == id },
            afterSubmit = { milestone ->
                kingdomActor.getKingdom()?.let { kingdom ->
                    kingdom.homebrewMilestones = kingdom.homebrewMilestones
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
            val enabled = kingdom.milestones.associateBy { it.id }
            val homebrewIds = kingdom.homebrewMilestones.map { it.id }.toSet()
            kingdom.getMilestones()
                .sortedWith(compareBy(RawMilestone::name))
                .map { item ->
                    val canBeEdited = item.id in homebrewIds
                    CrudItem(
                        id = item.id,
                        name = item.name,
                        nameIsHtml = false,
                        additionalColumns = arrayOf(
                            CrudColumn(
                                escapeHtml = false,
                                value = if (enabled[item.id]?.completed == true) {
                                    "<i class=\"fa-solid fa-circle-check\"></i>"
                                } else {
                                    ""
                                }
                            ), CrudColumn(
                                escapeHtml = true,
                                value = item.xp.toString(),
                            )
                        ),
                        enable = CheckboxInput(
                            value = enabled[item.id]?.enabled == true,
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
        arrayOf("Completed", "XP")
    }

    override fun onParsedSubmit(value: CrudData): Promise<Void> = buildPromise {
        val enabled = value.enabledIds.toSet()
        kingdomActor.getKingdom()?.let { kingdom ->
            kingdom.milestones = kingdom.milestones.map { it.copy(enabled = it.id in enabled) }.toTypedArray()
            kingdomActor.setKingdom(kingdom)
        }
        undefined
    }
}