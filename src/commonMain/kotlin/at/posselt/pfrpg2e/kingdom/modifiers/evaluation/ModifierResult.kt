package at.posselt.pfrpg2e.kingdom.modifiers.evaluation

import at.posselt.pfrpg2e.kingdom.modifiers.DowngradeResult
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.UpgradeResult


data class ModifierResult(
    val modifiers: List<Modifier>,
    val filteredModifiers: List<Modifier>,
    val total: Int,
    val bonuses: Map<ModifierType, Int>,
    val penalties: Map<ModifierType, Int>,
    val rollOptions: Set<String>,
    val fortune: Boolean,
    val rollTwiceKeepHighest: Boolean,
    val rollTwiceKeepLowest: Boolean,
    val upgradeResults: Set<UpgradeResult>,
    val downgradeResults: Set<DowngradeResult>,
) {
    val assurance = 10 + (bonuses[ModifierType.PROFICIENCY] ?: 0)
}