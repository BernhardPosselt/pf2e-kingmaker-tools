package at.posselt.pfrpg2e.data.events

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier

data class KingdomEvent(
    val id: String,
    val name: String,
    val description: String,
    val special: String?,
    val modifiers: List<Modifier>,
    val resolution: String?,
    val resolvedOn: Set<DegreeOfSuccess>,
    val modifier: Int,
    val traits: Set<KingdomEventTrait>,
    val location: String?,
    val stages: List<KingdomEventStage>,
)