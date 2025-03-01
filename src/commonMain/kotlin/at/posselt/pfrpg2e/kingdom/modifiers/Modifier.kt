package at.posselt.pfrpg2e.kingdom.modifiers

import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Predicate
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.When

data class Modifier(
    val id: String,
    val type: ModifierType,
    val value: Int = 0,
    val name: String,
    val predicatedValue: When? = null,
    val enabled: Boolean = true,
    val isConsumedAfterRoll: Boolean = false,
    val turns: Int? = null,
    val rollOptions: Set<String> = emptySet(),
    val predicates: List<Predicate> = emptyList(),
)