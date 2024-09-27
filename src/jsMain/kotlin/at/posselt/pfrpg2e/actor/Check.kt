package at.posselt.pfrpg2e.actor

import at.posselt.pfrpg2e.data.actor.Attribute
import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.data.checks.RollMode
import at.posselt.pfrpg2e.fromOrdinal
import at.posselt.pfrpg2e.toCamelCase
import com.foundryvtt.pf2e.actions.CheckDC
import com.foundryvtt.pf2e.actor.PF2ECharacter
import com.foundryvtt.pf2e.actor.StatisticRollParameters
import js.array.toTypedArray
import kotlin.js.Promise

data class ParsedRollResult(
    val degreeOfSuccess: DegreeOfSuccess
)

fun PF2ECharacter.rollCheck(
    attribute: Attribute,
    dc: Int?,
    rollMode: RollMode = RollMode.GMROLL,
): Promise<ParsedRollResult>? = resolveAttribute(attribute)
    ?.let {
        it.roll(StatisticRollParameters(dc = dc?.let { CheckDC(value = dc) }, rollMode = rollMode.toCamelCase()))
            .then { result -> ParsedRollResult(fromOrdinal<DegreeOfSuccess>(result?.degreeOfSuccess ?: 0)!!) }
    }

fun Array<PF2ECharacter>.rollChecks(
    attribute: Attribute,
    dc: Int?,
    rollMode: RollMode = RollMode.GMROLL,
) = asSequence()
    .mapNotNull { it.rollCheck(attribute, dc, rollMode) }
    .toTypedArray()
