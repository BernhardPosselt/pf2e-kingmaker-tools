package at.posselt.pfrpg2e.kingdom.modifiers.constructors

import at.posselt.pfrpg2e.data.actor.Proficiency
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.ProficiencyMode

fun createProficiencyModifier(
    skill: KingdomSkill,
    proficiency: Proficiency,
    level: Int,
    mode: ProficiencyMode = ProficiencyMode.NONE,
) = Modifier(
    id = skill.value,
    name = "${skill.label} (${proficiency.label})",
    type = ModifierType.PROFICIENCY,
    value = when (proficiency) {
        Proficiency.UNTRAINED -> when (mode) {
            ProficiencyMode.NONE -> 0
            ProficiencyMode.HALF -> level / 2
            ProficiencyMode.FULL -> level
        }
        Proficiency.TRAINED -> 2 + level
        Proficiency.EXPERT -> 4 + level
        Proficiency.MASTER -> 6 + level
        Proficiency.LEGENDARY -> 8 + level
    },
)