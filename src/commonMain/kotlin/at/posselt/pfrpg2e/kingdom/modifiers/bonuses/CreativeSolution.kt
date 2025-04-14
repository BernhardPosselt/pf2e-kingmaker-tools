package at.posselt.pfrpg2e.kingdom.modifiers.bonuses

import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.All
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.HasRollOption

fun createSupernaturalSolutionModifier() =
    Modifier(
        id = "creative-solution",
        type = ModifierType.CIRCUMSTANCE,
        value = 2,
        name = "modifiers.bonuses.creativeSolution",
        enabled = true,
        applyIf = listOf(
            All(expressions = listOf(HasRollOption(
                option = "creative-solution"
            )))
        )
    )