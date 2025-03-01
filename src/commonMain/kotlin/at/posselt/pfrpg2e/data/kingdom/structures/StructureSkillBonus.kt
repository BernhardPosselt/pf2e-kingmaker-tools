package at.posselt.pfrpg2e.data.kingdom.structures

import at.posselt.pfrpg2e.data.kingdom.KingdomSkill

data class StructureSkillBonus(
    val structureNames: List<String>,
    val skill: KingdomSkill,
    val value: Int,
)