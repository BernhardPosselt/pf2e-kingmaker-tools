package at.posselt.pfrpg2e.data.kingdom.structures

import at.posselt.pfrpg2e.data.kingdom.KingdomSkill

data class GroupedStructureBonus(
    val structureNames: Set<String>,
    val skill: KingdomSkill?,
    val activity: String?,
    val value: Int,
    val locatedIn: String,
)