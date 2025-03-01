package at.posselt.pfrpg2e.kingdom.modifiers

import at.posselt.pfrpg2e.data.kingdom.KingdomAbility
import at.posselt.pfrpg2e.data.kingdom.KingdomPhase
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Predicate
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.When

data class Modifier(
    val id: String,
    val type: ModifierType,
    val value: Int = 0,
    val name: String,
    val predicatedValue: When? = null,
    val phases: List<KingdomPhase> = emptyList(),
    val activities: List<String> = emptyList(),
    val skills: List<KingdomSkill> = emptyList(),
    val abilities: List<KingdomAbility> = emptyList(),
    val enabled: Boolean = true,
    val isConsumedAfterRoll: Boolean = false,
    val turns: Int? = null,
    val rollOptions: Set<String> = emptySet(),
    val predicates: List<Predicate> = emptyList(),
)