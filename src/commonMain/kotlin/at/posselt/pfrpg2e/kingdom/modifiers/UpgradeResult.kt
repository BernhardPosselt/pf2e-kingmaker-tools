package at.posselt.pfrpg2e.kingdom.modifiers

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess

data class UpgradeResult(
    val upgrade: DegreeOfSuccess,
    val times: Int,
)
