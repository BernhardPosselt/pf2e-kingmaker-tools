package at.posselt.pfrpg2e.kingdom.modifiers.evaluation

import at.posselt.pfrpg2e.data.actor.Proficiency
import at.posselt.pfrpg2e.data.kingdom.KingdomAbilityScores
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType

fun createAbilityModifiers(skill: KingdomSkill, abilities: KingdomAbilityScores) = Modifier(
    id = skill.ability.value,
    name = skill.ability.label,
    type = ModifierType.ABILITY,
    value = abilities.resolveModifier(skill.ability),
)

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