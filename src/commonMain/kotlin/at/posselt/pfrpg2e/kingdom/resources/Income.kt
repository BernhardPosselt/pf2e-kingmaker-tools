package at.posselt.pfrpg2e.kingdom.resources

import at.posselt.pfrpg2e.data.kingdom.RealmData
import at.posselt.pfrpg2e.data.kingdom.ResourceDieSize
import at.posselt.pfrpg2e.data.kingdom.findKingdomSize

data class Income(
    val stone: Int,
    val ore: Int,
    val lumber: Int,
    val resourceDice: Int,
    val resourceDiceSize: ResourceDieSize,
    val rp: Int,
) {
    val resourcePointsFormula = "$resourceDice${resourceDiceSize.value}"
}

fun calculateIncome(
    kingdomLevel: Int,
    realmData: RealmData,
    additionalResourceDice: Int,
    additionalResourcePoints: Int,
): Income {
    val worksites = realmData.worksites
    val size = findKingdomSize(realmData.size)
    return Income(
        stone = worksites.quarries.income,
        ore = worksites.mines.income,
        lumber = worksites.lumberCamps.income,
        resourceDice = 4 + kingdomLevel + additionalResourceDice,
        resourceDiceSize = size.resourceDieSize,
        rp = additionalResourceDice
    )
}