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
    val culture: Boolean
    val economy: Boolean
    val loyalty: Boolean
    val stability: Boolean
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
    val id: String
    val skillIncrease: String?
    val abilityBoosts: RawAbilityBoostChoices?
    val featId: String?
    val ruinThresholdIncreases: RawRuinThresholdIncreases?
    val featRuinThresholdIncreases: Array<RawRuinThresholdIncrease>
}


