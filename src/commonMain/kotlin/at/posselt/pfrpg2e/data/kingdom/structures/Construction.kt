package at.posselt.pfrpg2e.data.kingdom.structures

import at.posselt.pfrpg2e.data.kingdom.KingdomSkillRank
import kotlin.math.max
import kotlin.math.min

data class Construction(
    val skills: Set<KingdomSkillRank> = emptySet(),
    val lumber: Int = 0,
    val luxuries: Int = 0,
    val ore: Int = 0,
    val stone: Int = 0,
    val rp: Int = 0,
    val dc: Int = 0,
) {
    fun free() = copy(
        lumber = 0,
        luxuries = 0,
        ore = 0,
        stone = 0,
        rp = 0,
    )

    fun halveCost() = copy(
        lumber = lumber / 2,
        luxuries = luxuries / 2,
        ore = ore / 2,
        stone = stone / 2,
        rp = rp / 2,
    )

    fun remainingCost(
        maxRpPerStructure: Int,
        constructedRp: Int,
        currentRp: Int
    ) = copy(
        lumber = 0,
        luxuries = 0,
        ore = 0,
        stone = 0,
        rp = min(max(0, constructedRp - currentRp), maxRpPerStructure)
    )

    fun upgradeFrom(other: Construction) = copy(
        lumber = max(lumber - other.lumber, 0),
        luxuries = max(luxuries - other.luxuries, 0),
        ore = max(ore - other.ore, 0),
        stone = max(stone - other.stone, 0),
        rp = max(rp - other.rp, 0),
    )

    fun hasFunds(
        existingLumber: Int,
        existingLuxuries: Int,
        existingOre: Int,
        existingStone: Int,
        existingRp: Int,
    ) = lumber <= existingLumber &&
            luxuries <= existingLuxuries &&
            ore <= existingOre &&
            stone <= existingStone &&
            rp <= existingRp

    fun hasFundsPartialConstruction(
        existingLumber: Int,
        existingLuxuries: Int,
        existingOre: Int,
        existingStone: Int,
        existingRp: Int,
        rpPerStructure: Int,
    ) = lumber <= existingLumber &&
            luxuries <= existingLuxuries &&
            ore <= existingOre &&
            stone <= existingStone &&
            min(rp, rpPerStructure) <= existingRp
}