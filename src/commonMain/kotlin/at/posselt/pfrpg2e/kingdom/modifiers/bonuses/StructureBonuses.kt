package at.posselt.pfrpg2e.kingdom.modifiers.bonuses

import at.posselt.pfrpg2e.data.kingdom.settlements.Settlement
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.EqPredicate

fun structureBonuses(settlement: Settlement)=
    settlement.bonuses.map {
        Modifier(
            value = it.value,
            id = "structure-${listOfNotNull(it.skill, it.activity).joinToString("-")}",
            name = it.structureNames.joinToString(", "),
            predicates = listOfNotNull(
                it.skill?.let { skill -> EqPredicate("@skill", skill.value) },
                it.activity?.let { activity -> EqPredicate("@activity", activity) },
            ),
            type = ModifierType.ITEM,
        )
    }
