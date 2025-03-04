package at.posselt.pfrpg2e.kingdom.modifiers

import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Expression
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.When

data class Modifier(
    val id: String,
    val type: ModifierType,
    val value: Int = 0,
    val name: String,
    val valueExpression: When? = null,
    val enabled: Boolean = true,
    val isConsumedAfterRoll: Boolean = false,
    val turns: Int? = null,
    val rollOptions: Set<String> = emptySet(),
    val applyIf: List<Expression<Boolean>> = emptyList(),
)