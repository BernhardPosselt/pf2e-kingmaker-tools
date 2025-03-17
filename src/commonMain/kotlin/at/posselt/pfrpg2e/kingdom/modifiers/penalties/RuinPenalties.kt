package at.posselt.pfrpg2e.kingdom.modifiers.penalties

import at.posselt.pfrpg2e.data.kingdom.Ruin
import at.posselt.pfrpg2e.data.kingdom.RuinValues
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Eq

fun createRuinModifiers(values: RuinValues): List<Modifier> =
    Ruin.entries.mapNotNull {
        val value = values.resolve(it).penalty
        if (value == 0) {
            null
        } else {
            Modifier(
                id = "ruin-${it.value}",
                name = "Ruin (${it.label})",
                type = ModifierType.ITEM,
                value = -value,
                applyIf = listOf(
                    Eq("@ability", it.ability.value)
                )
            )
        }
    }