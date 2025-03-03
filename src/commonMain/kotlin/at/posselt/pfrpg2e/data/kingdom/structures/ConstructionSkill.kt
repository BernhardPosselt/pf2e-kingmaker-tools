package at.posselt.pfrpg2e.data.kingdom.structures

import at.posselt.pfrpg2e.data.kingdom.KingdomSkill

data class ConstructionSkill(
    val skill: KingdomSkill = KingdomSkill.ENGINEERING,
    val minRank: Int = 0,
)