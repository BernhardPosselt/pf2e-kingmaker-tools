package at.posselt.pfrpg2e.camping

import at.posselt.pfrpg2e.divideRoundingUp
import kotlin.math.max

fun calculateRestoredHp(
    currentHp: Int,
    maxHp: Int,
    conModifier: Int,
    level: Int,
): Int {
    val maxRestored = max(conModifier, 1) * level
    val hpLost = maxHp - currentHp
    return if (hpLost >= maxRestored) maxRestored else hpLost
}


fun calculateRestDurationSeconds(restSecondsPerPc: List<Int>): Int {
    val partySize = restSecondsPerPc.size
    val restDurationSeconds = restSecondsPerPc.average().toInt()
    return if (partySize < 2) {
        restDurationSeconds
    } else {
        restDurationSeconds + (restDurationSeconds / (partySize - 1))
    }
}

fun calculateDailyPreparationSeconds(gunsToClean: Int): Int =
    if (gunsToClean == 0) 3600 else gunsToClean.divideRoundingUp(4) * 3600
