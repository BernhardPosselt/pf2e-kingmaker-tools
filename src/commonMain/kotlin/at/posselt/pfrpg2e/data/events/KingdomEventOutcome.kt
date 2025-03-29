package at.posselt.pfrpg2e.data.events

import at.posselt.pfrpg2e.kingdom.modifiers.Modifier

data class KingdomEventOutcome(
    val msg: String,
    val modifiers: List<Modifier>,
)