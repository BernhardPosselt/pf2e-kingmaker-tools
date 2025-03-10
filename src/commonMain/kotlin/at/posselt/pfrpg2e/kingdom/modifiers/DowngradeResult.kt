package at.posselt.pfrpg2e.kingdom.modifiers

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess

data class DowngradeResult(
    val downgrade: DegreeOfSuccess,
    val times: Int = 1,
)