package at.posselt.pfrpg2e.data.kingdom.structures

import at.posselt.pfrpg2e.data.kingdom.KingdomSkillRank

data class Construction(
    val skills: Set<KingdomSkillRank> = emptySet(),
    val lumber: Int = 0,
    val luxuries: Int = 0,
    val ore: Int = 0,
    val stone: Int = 0,
    val rp: Int = 0,
    val dc: Int = 0,
)