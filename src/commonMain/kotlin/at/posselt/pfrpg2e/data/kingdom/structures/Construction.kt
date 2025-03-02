package at.posselt.pfrpg2e.data.kingdom.structures

import at.posselt.pfrpg2e.data.kingdom.KingdomSkill

data class Construction(
    val skills: List<KingdomSkill>,
    val lumber: Int,
    val luxuries: Int,
    val ore: Int,
    val stone: Int,
    val rp: Int,
    val dc: Int,
)