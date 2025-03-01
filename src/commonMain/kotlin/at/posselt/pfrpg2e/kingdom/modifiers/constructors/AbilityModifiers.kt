package at.posselt.pfrpg2e.kingdom.modifiers.constructors

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