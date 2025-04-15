package at.posselt.pfrpg2e.kingdom.sheet

import at.posselt.pfrpg2e.kingdom.KingdomActor
import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.RawMilestone
import at.posselt.pfrpg2e.kingdom.data.MilestoneChoice
import at.posselt.pfrpg2e.kingdom.getKingdom
import at.posselt.pfrpg2e.kingdom.setKingdom
import at.posselt.pfrpg2e.utils.postChatMessage
import at.posselt.pfrpg2e.utils.t
import js.objects.recordOf
import kotlin.math.abs
import kotlin.math.max

data class XpChange(
    val addXp: Int = 0,
    val gain: Boolean = true,
    val addLevel: Int = 0,
    val losingLevel: Boolean = false,
) {
    suspend fun toChat() {
        if (addXp > 0) {
            if (gain) {
                postChatMessage(t("kingdom.gainingXp", recordOf("xp" to addXp)))
            } else {
                postChatMessage(t("kingdom.losingXp", recordOf("xp" to addXp)))
            }
        }
        if (losingLevel) {
            postChatMessage(t("kingdom.losingLevel"))
        }
    }
}

suspend fun KingdomActor.gainXp(amount: Int) {
    getKingdom()?.let {
        val change = it.calculateXpChange(amount)
        change.toChat()
        it.level += change.addLevel
        it.xp += change.addXp
        setKingdom(it)
    }
}

fun KingdomData.calculateXpChange(amount: Int) =
    if (amount > 0 && level < 20) {
        XpChange(amount)
    } else if (amount < 0) {
        if (abs(amount) < xp) {
            XpChange(amount, gain = false)
        } else if (level > 1) {
            val newTarget = xp + amount + 1000
            XpChange(newTarget - xp, losingLevel = true, gain = false, addLevel = -1)
        } else {
            XpChange()
        }
    } else {
        XpChange()
    }

fun calculateMilestoneXp(
    milestones: Array<RawMilestone>,
    previous: Array<MilestoneChoice>,
    current: Array<MilestoneChoice>,
): Int {
    val milestonesById = milestones.associateBy { it.id }
    val previousById = previous.associateBy { it.id }
    return current.sumOf { curr ->
        val milestone = milestonesById[curr.id]
        val prev = previousById[curr.id]
        if (milestone != null && prev != null && prev.completed != curr.completed) {
            if (curr.completed == false) {
                -milestone.xp
            } else {
                milestone.xp
            }
        } else {
            0
        }
    }
}

suspend fun KingdomActor.levelUp() {
    getKingdom()?.let {
        it.level += 1
        it.xp = if (it.level >= 20) 0 else max(0, it.xp - it.xpThreshold)
        postChatMessage(t("kingdom.leveledUpTo", recordOf("level" to it.level)))
        setKingdom(it)
    }
}

fun KingdomData.canLevelUp() =
    xp >= xpThreshold && level < 20