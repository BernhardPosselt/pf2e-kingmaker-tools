package at.posselt.pfrpg2e.data.kingdom.structures

// TODO
data class EvaluatedStructureBonuses(
    val eventBonus: Int, // added by watchtowers
    val leaderBonus: Int, // added by Palace & co
    val skillBonuses: List<StructureSkillBonus>,
    val activityBonuses: List<StructureActivityBonus>,
)
