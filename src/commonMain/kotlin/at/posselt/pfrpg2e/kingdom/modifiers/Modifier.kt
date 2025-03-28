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
    val fortune: Boolean = false,
    val rollTwiceKeepHighest: Boolean = false,
    val rollTwiceKeepLowest: Boolean = false,
    val upgradeResults: List<UpgradeResult> = emptyList(),
    val downgradeResults: List<DowngradeResult> = emptyList(),
    val notes: Set<Note> = emptySet(),
)