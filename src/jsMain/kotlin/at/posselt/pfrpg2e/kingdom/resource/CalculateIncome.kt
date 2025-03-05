package at.posselt.pfrpg2e.kingdom.resource

import at.posselt.pfrpg2e.data.kingdom.RealmData
import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.RawKingdomFeat
import at.posselt.pfrpg2e.kingdom.resources.Income
import at.posselt.pfrpg2e.kingdom.resources.calculateIncome

fun KingdomData.income(
    realmData: RealmData,
    allFeats: List<RawKingdomFeat>,
): Income {
    return calculateIncome(
        kingdomLevel = level,
        realmData = realmData,
        additionalResourceDice = allFeats.sumOf { it.resourceDice ?: 0 } + resourceDice.now,
        additionalResourcePoints = resourcePoints.now,
    )
}