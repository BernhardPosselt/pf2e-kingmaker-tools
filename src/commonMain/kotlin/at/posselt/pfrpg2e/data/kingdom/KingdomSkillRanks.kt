package at.posselt.pfrpg2e.data.kingdom

import at.posselt.pfrpg2e.data.actor.Proficiency

data class KingdomSkillRanks(
    val agriculture: Int = 0,
    val arts: Int = 0,
    val boating: Int = 0,
    val defense: Int = 0,
    val engineering: Int = 0,
    val exploration: Int = 0,
    val folklore: Int = 0,
    val industry: Int = 0,
    val intrigue: Int = 0,
    val magic: Int = 0,
    val politics: Int = 0,
    val scholarship: Int = 0,
    val statecraft: Int = 0,
    val trade: Int = 0,
    val warfare: Int = 0,
    val wilderness: Int = 0,
) {
    fun resolveProficiency(skill: KingdomSkill) =
        Proficiency.fromRank(resolve(skill))

    fun resolve(skill: KingdomSkill) =
        when (skill) {
            KingdomSkill.AGRICULTURE -> agriculture
            KingdomSkill.ARTS -> arts
            KingdomSkill.BOATING -> boating
            KingdomSkill.DEFENSE -> defense
            KingdomSkill.ENGINEERING -> engineering
            KingdomSkill.EXPLORATION -> exploration
            KingdomSkill.FOLKLORE -> folklore
            KingdomSkill.INDUSTRY -> industry
            KingdomSkill.INTRIGUE -> intrigue
            KingdomSkill.MAGIC -> magic
            KingdomSkill.POLITICS -> politics
            KingdomSkill.SCHOLARSHIP -> scholarship
            KingdomSkill.STATECRAFT -> statecraft
            KingdomSkill.TRADE -> trade
            KingdomSkill.WARFARE -> warfare
            KingdomSkill.WILDERNESS -> wilderness
        }
}