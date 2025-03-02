package at.posselt.pfrpg2e.data.kingdom.structures

import at.posselt.pfrpg2e.data.kingdom.KingdomSkill

data class Construction(
    val skills: List<KingdomSkill> = listOf(KingdomSkill.ENGINEERING),
    val lumber: Int = 0,
    val luxuries: Int = 0,
    val ore: Int = 0,
    val stone: Int = 0,
    val rp: Int = 0,
    val dc: Int = 0,
)