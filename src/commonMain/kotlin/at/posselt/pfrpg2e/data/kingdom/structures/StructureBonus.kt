package at.posselt.pfrpg2e.data.kingdom.structures

import at.posselt.pfrpg2e.data.kingdom.KingdomSkill

data class StructureBonus(
    val skill: KingdomSkill?,
    val activity: String?,
    val value: Int,
)