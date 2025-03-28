package at.posselt.pfrpg2e.kingdom.modifiers

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess

data class Note(
    val note: String,
    val degree: DegreeOfSuccess? = null,
)