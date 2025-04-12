package at.posselt.pfrpg2e.kingdom.modifiers.bonuses

import at.posselt.pfrpg2e.data.kingdom.KingdomAbility
import at.posselt.pfrpg2e.data.kingdom.KingdomAbilityScores
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Eq

fun createAbilityModifiers(abilities: KingdomAbilityScores) = listOf(
    Modifier(
        id = KingdomAbility.CULTURE.value,
        name = KingdomAbility.CULTURE.i18nKey,
        type = ModifierType.ABILITY,
        value = abilities.resolveModifier(KingdomAbility.CULTURE),
        applyIf = listOf(
            Eq(
                left = "@ability",
                right = KingdomAbility.CULTURE.value,
            )
        )
    ),
    Modifier(
        id = KingdomAbility.ECONOMY.value,
        name = KingdomAbility.ECONOMY.i18nKey,
        type = ModifierType.ABILITY,
        value = abilities.resolveModifier(KingdomAbility.ECONOMY),
        applyIf = listOf(
            Eq(
                left = "@ability",
                right = KingdomAbility.ECONOMY.value,
            )
        )
    ),
    Modifier(
        id = KingdomAbility.LOYALTY.value,
        name = KingdomAbility.LOYALTY.i18nKey,
        type = ModifierType.ABILITY,
        value = abilities.resolveModifier(KingdomAbility.LOYALTY),
        applyIf = listOf(
            Eq(
                left = "@ability",
                right = KingdomAbility.LOYALTY.value,
            )
        )
    ),
    Modifier(
        id = KingdomAbility.STABILITY.value,
        name = KingdomAbility.STABILITY.i18nKey,
        type = ModifierType.ABILITY,
        value = abilities.resolveModifier(KingdomAbility.STABILITY),
        applyIf = listOf(
            Eq(
                left = "@ability",
                right = KingdomAbility.STABILITY.value,
            )
        )
    )
)