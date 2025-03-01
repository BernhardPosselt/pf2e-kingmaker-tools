package at.posselt.pfrpg2e.kingdom.modifiers.evaluation

import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType

data class ModifierResult(
    val modifiers: List<Modifier>,
    val total: Int,
    val bonuses: Map<ModifierType, Int>,
    val penalties: Map<ModifierType, Int>,
    val rollOptions: Set<String>,
) {
    val assurance = 10 +
            (bonuses[ModifierType.PROFICIENCY] ?: 0) +
            (penalties[ModifierType.PROFICIENCY] ?: 0)
}