package at.posselt.pfrpg2e.utils

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.data.checks.RollMode
import at.posselt.pfrpg2e.data.checks.determineDegreeOfSuccess
import at.posselt.pfrpg2e.toCamelCase
import com.foundryvtt.core.dice.Roll
import com.foundryvtt.core.dice.RollMessageOptions
import com.foundryvtt.core.documents.Actor
import com.foundryvtt.core.documents.ChatMessage
import com.foundryvtt.core.documents.GetSpeakerOptions
import js.objects.Record
import js.objects.recordOf
import kotlinx.coroutines.await

data class DieValue(val value: Int) {
    @Suppress("unused")
    fun isNat1() = value == 1
    fun isNat20() = value == 20
}

data class D20CheckResult(
    val degreeOfSuccess: DegreeOfSuccess,
    val dieValue: DieValue,
    val roll: Roll,
    val rollMode: RollMode,
) {
    suspend fun toChat(flavor: String? = undefined, isHtml: Boolean) {
        roll.toMessage(
            recordOf("flavor" to if(!isHtml && flavor != undefined) escapeHtml(flavor) else flavor),
            RollMessageOptions(rollMode = rollMode.toCamelCase())
        ).await()
    }
}

suspend fun d20Check(
    dc: Int,
    modifier: Int = 0,
    flavor: String? = undefined,
    rollMode: RollMode? = null,
    toChat: Boolean = true,
    rollTwiceKeepHighest: Boolean = false,
    rollTwiceKeepLowest: Boolean = false,
    assurance: Boolean = false,
    isHtml: Boolean = false,
): D20CheckResult {
    val d20 = if (assurance) {
        "10"
    } else if (rollTwiceKeepHighest && !rollTwiceKeepLowest) {
        "2d20kh"
    } else if (rollTwiceKeepLowest && !rollTwiceKeepHighest) {
        "2d20kl"
    } else {
        "1d20"
    }
    val formula = if (modifier > 0) "$d20+$modifier" else d20
    val roll = Roll(formula).evaluate().await()
    val dieValue = roll.total - modifier
    val result = D20CheckResult(
        degreeOfSuccess = determineDegreeOfSuccess(dc, roll.total, dieValue),
        dieValue = DieValue(dieValue),
        roll = roll,
        rollMode = rollMode ?: RollMode.PUBLICROLL,
    )
    if (toChat) {
        result.toChat(flavor, isHtml)
    }
    return result
}

suspend fun roll(
    formula: String,
    flavor: String? = undefined,
    rollMode: RollMode = RollMode.PUBLICROLL,
    speaker: Actor? = null,
    toChat: Boolean = true,
    isHtml: Boolean = false,
): Int {
    val roll = Roll(formula).evaluate().await()
    if (toChat) {
        val data: Record<String, Any?> = recordOf(
            "flavor" to if (!isHtml && flavor != undefined) escapeHtml(flavor) else flavor,
        )
        if (speaker != null) {
            data["speaker"] = ChatMessage.getSpeaker(
                GetSpeakerOptions(
                    actor = speaker,
                )
            )
        }
        roll.toMessage(
            data,
            RollMessageOptions(rollMode = rollMode.toCamelCase())
        ).await()
    }
    return roll.total
}