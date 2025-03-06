package at.posselt.pfrpg2e.kingdom.modifiers

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Expression

data class UpgradeResult(
    val upgrade: DegreeOfSuccess,
    val applyIf: List<Expression<Boolean>> = emptyList(),
)

