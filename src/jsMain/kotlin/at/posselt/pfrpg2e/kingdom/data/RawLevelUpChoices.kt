package at.posselt.pfrpg2e.kingdom.data

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface RawCharterChoices {
    val type: String?
    val abilityBoosts: RawAbilityBoostChoices
}

@JsPlainObject
external interface RawHeartlandChoices {
    val type: String?
}

@JsPlainObject
external interface RawGovernmentChoices {
    val type: String?
    val abilityBoosts: RawAbilityBoostChoices
}

@JsPlainObject
external interface RawAbilityBoostChoices {
    var culture: Boolean
    var economy: Boolean
    var loyalty: Boolean
    var stability: Boolean
}

@JsPlainObject
external interface RawRuinThresholdIncrease {
    val value: Int
    val increase: Boolean
}

@JsPlainObject
external interface RawRuinThresholdIncreases {
    val crime: RawRuinThresholdIncrease
    val corruption: RawRuinThresholdIncrease
    val strife: RawRuinThresholdIncrease
    val stability: RawRuinThresholdIncrease
}

@JsPlainObject
external interface RawFeatureChoices {
    var id: String
    var skillIncrease: String?
    var abilityBoosts: RawAbilityBoostChoices?
    var featId: String?
    var ruinThresholdIncreases: RawRuinThresholdIncreases?
    var featRuinThresholdIncreases: Array<RawRuinThresholdIncrease>
}


