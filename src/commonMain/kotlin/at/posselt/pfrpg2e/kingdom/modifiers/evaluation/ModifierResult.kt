package at.posselt.pfrpg2e.kingdom.modifiers.evaluation

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType

data class ModifierResult(
    val modifiers: List<Modifier>,
    val total: Int,
    val bonuses: Map<ModifierType, Int>,
    val penalties: Map<ModifierType, Int>,
    val rollOptions: Set<String>,
    val fortune: Boolean,
    val rollTwice: Boolean,
    val upgradeResults: Set<DegreeOfSuccess>,
    val downgradeResults: Set<DegreeOfSuccess>,
) {
    val assurance = 10 + (bonuses[ModifierType.PROFICIENCY] ?: 0)
}