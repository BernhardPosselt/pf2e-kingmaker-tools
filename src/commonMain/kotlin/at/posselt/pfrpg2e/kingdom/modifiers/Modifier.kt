package at.posselt.pfrpg2e.kingdom.modifiers

import at.posselt.pfrpg2e.data.kingdom.KingdomPhase
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Predicate
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.WhenBranch

data class Modifier(
    val type: ModifierType,
    val value: Int = 0,
    val name: String,
    val predicatedValue: WhenBranch,
    val phases: List<KingdomPhase> = emptyList(),
    val activities: List<String> = emptyList(),
    val skills: List<KingdomSkill> = emptyList(),
    val enabled: Boolean = true,
    val consumed: Boolean = false,
    val turns: Int? = null,
    val rollOptions: List<String> = emptyList(),
    val predicate: List<Predicate>
)