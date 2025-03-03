package at.posselt.pfrpg2e.kingdom.modifiers.bonuses

import at.posselt.pfrpg2e.data.kingdom.KingdomAbility
import at.posselt.pfrpg2e.data.kingdom.KingdomAbilityScores
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.EqPredicate

fun createAbilityModifiers(abilities: KingdomAbilityScores) = listOf(
    Modifier(
        id = KingdomAbility.CULTURE.value,
        name = KingdomAbility.CULTURE.label,
        type = ModifierType.ABILITY,
        value = abilities.resolveModifier(KingdomAbility.CULTURE),
        predicates = listOf(
            EqPredicate(
                left = "@ability",
                right = KingdomAbility.CULTURE.value,
            )
        )
    ),
    Modifier(
        id = KingdomAbility.ECONOMY.value,
        name = KingdomAbility.ECONOMY.label,
        type = ModifierType.ABILITY,
        value = abilities.resolveModifier(KingdomAbility.ECONOMY),
        predicates = listOf(
            EqPredicate(
                left = "@ability",
                right = KingdomAbility.ECONOMY.value,
            )
        )
    ),
    Modifier(
        id = KingdomAbility.LOYALTY.value,
        name = KingdomAbility.LOYALTY.label,
        type = ModifierType.ABILITY,
        value = abilities.resolveModifier(KingdomAbility.LOYALTY),
        predicates = listOf(
            EqPredicate(
                left = "@ability",
                right = KingdomAbility.LOYALTY.value,
            )
        )
    ),
    Modifier(
        id = KingdomAbility.STABILITY.value,
        name = KingdomAbility.STABILITY.label,
        type = ModifierType.ABILITY,
        value = abilities.resolveModifier(KingdomAbility.STABILITY),
        predicates = listOf(
            EqPredicate(
                left = "@ability",
                right = KingdomAbility.STABILITY.value,
            )
        )
    )
)

fun createSkillAbilityModifiers(skill: KingdomSkill, abilities: KingdomAbilityScores) = Modifier(
    id = skill.ability.value,
    name = skill.ability.label,
    type = ModifierType.ABILITY,
    value = abilities.resolveModifier(skill.ability),
)