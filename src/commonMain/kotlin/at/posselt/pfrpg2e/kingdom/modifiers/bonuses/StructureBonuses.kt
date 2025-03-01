package at.posselt.pfrpg2e.kingdom.modifiers.bonuses

import at.posselt.pfrpg2e.data.kingdom.structures.EvaluatedStructureBonuses
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.EqPredicate

fun structureBonuses(structureBonuses: EvaluatedStructureBonuses): List<Modifier> {
    val skillBonuses = structureBonuses.skillBonuses.map {
        Modifier(
            value = it.value,
            id = "structure-${it.skill}",
            name = it.structureNames.joinToString(", "),
            predicates = listOf(
                EqPredicate("@skill", it.skill.value)
            ),
            type = ModifierType.ITEM,
        )
    }
    val activityBonuses = structureBonuses.activityBonuses.map {
        Modifier(
            value = it.value,
            id = "structure-${it.skill}-${it.activity}",
            name = it.structureNames.joinToString(", "),
            predicates = listOf(
                EqPredicate("@skill", it.skill.value),
                EqPredicate("@activity", it.activity),
            ),
            type = ModifierType.ITEM,
        )
    }
    return activityBonuses + skillBonuses
}