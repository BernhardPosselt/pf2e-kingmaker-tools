package at.posselt.pfrpg2e.kingdom.modifiers.constructors

import at.posselt.pfrpg2e.data.kingdom.Ruin
import at.posselt.pfrpg2e.data.kingdom.Ruins
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.EqPredicate

fun createRuinModifiers(values: Ruins): List<Modifier> {
    return Ruin.entries.mapNotNull {
        val value = values.resolvePenalty(it)
        if (value != 0) {
            Modifier(
                id = "ruin-${it.value}",
                name = "Ruin (${it.label})",
                type = ModifierType.ITEM,
                value = value,
                predicates = listOf(
                    EqPredicate("@ability", it.ability.value)
                )
            )
        } else {
            null
        }
    }
}