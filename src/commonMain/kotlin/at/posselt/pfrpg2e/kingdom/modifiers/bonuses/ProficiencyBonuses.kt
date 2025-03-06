package at.posselt.pfrpg2e.kingdom.modifiers.bonuses

import at.posselt.pfrpg2e.data.actor.Proficiency
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.KingdomSkillRanks
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.UntrainedProficiencyMode
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Eq

fun createAllProficiencyModifiers(
    ranks: KingdomSkillRanks,
    level: Int,
    mode: UntrainedProficiencyMode
) = KingdomSkill.entries.map {
    createProficiencyModifier(
        skill = it,
        proficiency = ranks.resolveProficiency(it),
        level = level,
        mode = mode
    )
}

fun createProficiencyModifier(
    skill: KingdomSkill,
    proficiency: Proficiency,
    level: Int,
    mode: UntrainedProficiencyMode = UntrainedProficiencyMode.NONE,
) = Modifier(
    id = skill.value,
    name = "${skill.label} (${proficiency.label})",
    type = ModifierType.PROFICIENCY,
    value = when (proficiency) {
        Proficiency.UNTRAINED -> when (mode) {
            UntrainedProficiencyMode.NONE -> 0
            UntrainedProficiencyMode.HALF -> level / 2
            UntrainedProficiencyMode.FULL -> level
        }

        Proficiency.TRAINED -> 2 + level
        Proficiency.EXPERT -> 4 + level
        Proficiency.MASTER -> 6 + level
        Proficiency.LEGENDARY -> 8 + level
    },
    applyIf = listOf(Eq("@skill", skill.value)),
)