package at.posselt.pfrpg2e.kingdom.modifiers

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Expression

data class DowngradeResult(
    val downgrade: DegreeOfSuccess,
    val applyIf: List<Expression<Boolean>> = emptyList(),
)