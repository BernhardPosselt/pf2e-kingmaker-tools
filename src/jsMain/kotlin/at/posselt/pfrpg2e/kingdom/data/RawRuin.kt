package at.posselt.pfrpg2e.kingdom.data

import at.posselt.pfrpg2e.data.kingdom.RuinValue
import at.posselt.pfrpg2e.data.kingdom.RuinValues
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

fun RawRuinValues.parse() =
    RuinValue(
        value = value,
        threshold = threshold,
        penalty = penalty,
    )

fun RawRuin.parse() = RuinValues(
    decay = decay.parse(),
    strife = strife.parse(),
    corruption = corruption.parse(),
    crime = crime.parse(),
)