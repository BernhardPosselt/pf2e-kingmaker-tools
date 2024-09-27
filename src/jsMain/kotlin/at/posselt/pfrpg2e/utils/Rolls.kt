package at.posselt.pfrpg2e.utils

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.data.checks.RollMode
import at.posselt.pfrpg2e.data.checks.determineDegreeOfSuccess
import at.posselt.pfrpg2e.toCamelCase
import com.foundryvtt.core.Actor
import com.foundryvtt.core.Roll
import com.foundryvtt.core.RollMessageOptions
import com.foundryvtt.core.documents.ChatMessage
import com.foundryvtt.core.documents.GetSpeakerOptions
import js.objects.Record
import js.objects.recordOf
import kotlinx.coroutines.await

data class DieValue(val value: Int) {
    fun isNat1() = value == 1
    fun isNat20() = value == 20
}

data class D20CheckResult(
    val degreeOfSuccess: DegreeOfSuccess,
    val dieValue: DieValue,
)

suspend fun d20Check(
    dc: Int,
    modifier: Int = 0,
    flavor: String? = undefined,
    rollMode: RollMode = RollMode.PUBLICROLL,
    toChat: Boolean = true,
): D20CheckResult {
    val d20 = "1d20"
    val formula = if (modifier > 0) "$d20+$modifier" else d20
    val roll = Roll(formula).evaluate().await()
    if (toChat) {
        roll.toMessage(
            recordOf("flavor" to flavor),
            RollMessageOptions(rollMode = rollMode.toCamelCase())
        ).await()
    }
    val dieValue = roll.total - modifier
    return D20CheckResult(
        degreeOfSuccess = determineDegreeOfSuccess(dc, roll.total, dieValue),
        dieValue = DieValue(dieValue),
    )
}

suspend fun roll(
    formula: String,
    flavor: String? = undefined,
    rollMode: RollMode = RollMode.PUBLICROLL,
    speaker: Actor? = null,
    toChat: Boolean = true,
): Int {
    val roll = Roll(formula).evaluate().await()
    if (toChat) {
        val data: Record<String, Any?> = recordOf(
            "flavor" to flavor,
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