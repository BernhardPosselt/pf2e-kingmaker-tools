package at.posselt.pfrpg2e.kingdom.modifiers.bonuses

import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.MergedSettlement
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Eq

fun createStructureBonuses(mergedSettlement: MergedSettlement) =
    mergedSettlement.settlement.bonuses.map {
        Modifier(
            value = it.value,
            id = "structure-${listOfNotNull(it.skill, it.activity).joinToString("-")}",
            name = "${it.locatedIn} (${it.structureNames.joinToString(", ")})",
            applyIf = listOfNotNull(
                it.skill?.let { skill -> Eq("@skill", skill.value) },
                it.activity?.let { activity -> Eq("@activity", activity) },
            ),
            type = ModifierType.ITEM,
        )
    }
