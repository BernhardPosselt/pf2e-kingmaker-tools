package at.posselt.pfrpg2e.data.kingdom

import kotlin.math.min

fun calculateHexXP(
    hexes: Int,
    xpPerClaimedHex: Int,
    kingdomSize: Int,
    useVK: Boolean
) = if (useVK) {
    when {
        kingdomSize < 10 -> hexes * 100
        kingdomSize < 25 -> hexes * 50
        kingdomSize < 50 -> hexes * 25
        kingdomSize < 100 -> hexes * 10
        else -> hexes * 5
    }
} else {
    hexes * xpPerClaimedHex;
}

fun calculateRpXP(
    rp: Int,
    kingdomLevel: Int,
    rpToXpConversionRate: Int,
    rpToXpConversionLimit: Int,
    useVK: Boolean,
): Int {
    val xp = if (useVK) {
        when {
            kingdomLevel < 5 -> rp * 10
            kingdomLevel < 9 -> rp * 7
            kingdomLevel < 13 -> rp * 5
            kingdomLevel < 17 -> rp * 2
            else -> rp
        }
    } else {
        rp * rpToXpConversionRate
    }
    return min(rpToXpConversionLimit, xp)
}

fun calculateEventXP(modifier: Int) =
    when (modifier) {
        -4 -> 10
        -3 -> 15
        -2 -> 20
        -1 -> 30
        0 -> 40
        1 -> 60
        2 -> 80
        3 -> 120
        4 -> 160
        else -> 0
    }
