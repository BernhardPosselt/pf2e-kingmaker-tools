package at.posselt.pfrpg2e.kingdom.modifiers.penalties

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.kingdom.modifiers.DowngradeResult
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Gte

fun createAnarchyPenalty() =
    Modifier(
        id = "anarchy",
        type = ModifierType.UNTYPED,
        value = 0,
        name = "modifiers.penalties.anarchy",
        applyIf = listOf(
            Gte("@unrest", "@anarchyAt")
        ),
        downgradeResults = listOf(
            DowngradeResult(
                downgrade = DegreeOfSuccess.CRITICAL_SUCCESS,
            ),
            DowngradeResult(
                downgrade = DegreeOfSuccess.SUCCESS,
            ),
            DowngradeResult(
                downgrade = DegreeOfSuccess.FAILURE,
            )
        )
    )