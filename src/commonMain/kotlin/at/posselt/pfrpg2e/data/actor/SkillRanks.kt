package at.posselt.pfrpg2e.data.actor

data class SkillRanks(
    val acrobatics: Int = 0,
    val arcana: Int = 0,
    val athletics: Int = 0,
    val crafting: Int = 0,
    val deception: Int = 0,
    val diplomacy: Int = 0,
    val intimidation: Int = 0,
    val medicine: Int = 0,
    val nature: Int = 0,
    val occultism: Int = 0,
    val performance: Int = 0,
    val religion: Int = 0,
    val society: Int = 0,
    val stealth: Int = 0,
    val survival: Int = 0,
    val thievery: Int = 0,
    val perception: Int = 0,
    val lores: List<LoreRank> = emptyList(),
) {
    private val loresByName = lores.associateBy { it.name }
    data class LoreRank(
        val name: String,
        val rank: Int,
    )
    fun resolveRank(attribute: Attribute): Int =
        when (attribute) {
            is Lore -> loresByName[attribute.value]?.rank ?: 0
            Perception -> perception
            Skill.ACROBATICS -> acrobatics
            Skill.ARCANA -> arcana
            Skill.ATHLETICS -> athletics
            Skill.CRAFTING -> crafting
            Skill.DECEPTION -> deception
            Skill.DIPLOMACY -> diplomacy
            Skill.INTIMIDATION -> intimidation
            Skill.MEDICINE -> medicine
            Skill.NATURE -> nature
            Skill.OCCULTISM -> occultism
            Skill.PERFORMANCE -> performance
            Skill.RELIGION -> religion
            Skill.SOCIETY -> society
            Skill.STEALTH -> stealth
            Skill.SURVIVAL -> survival
            Skill.THIEVERY -> thievery
        }
}