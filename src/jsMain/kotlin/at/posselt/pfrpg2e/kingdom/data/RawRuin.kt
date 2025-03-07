package at.posselt.pfrpg2e.kingdom.data

import at.posselt.pfrpg2e.data.kingdom.Ruins
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface RawRuinValues {
    var value: Int
    var penalty: Int
    var threshold: Int
}

@JsPlainObject
external interface RawRuin {
    var corruption: RawRuinValues
    var crime: RawRuinValues
    var decay: RawRuinValues
    var strife: RawRuinValues
}

fun RawRuin.parse() = Ruins(
    decayPenalty = decay.penalty,
    strifePenalty = strife.penalty,
    corruptionPenalty = corruption.penalty,
    crimePenalty = crime.penalty,
)