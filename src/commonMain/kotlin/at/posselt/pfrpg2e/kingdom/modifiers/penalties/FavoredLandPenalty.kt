package at.posselt.pfrpg2e.kingdom.modifiers.penalties

import at.posselt.pfrpg2e.data.kingdom.KingdomPhase
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.EqPredicate

fun favoredLandPenalty() =
    Modifier(
        value = -2,
        type = ModifierType.UNTYPED,
        id = "favored-land",
        enabled = false,
        name = "Favored Land",
        predicates = listOf(
            EqPredicate("@phase", KingdomPhase.REGION.value)
        )
    )