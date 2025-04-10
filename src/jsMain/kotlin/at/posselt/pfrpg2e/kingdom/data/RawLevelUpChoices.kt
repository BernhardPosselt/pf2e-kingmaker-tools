package at.posselt.pfrpg2e.kingdom.data

import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.RawExplodedKingdomFeature
import at.posselt.pfrpg2e.kingdom.getCharters
import at.posselt.pfrpg2e.kingdom.getGovernments
import at.posselt.pfrpg2e.kingdom.getHeartlands
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface RawCharterChoices {
    var type: String?
    var abilityBoosts: RawAbilityBoostChoices
}

@JsPlainObject
external interface RawHeartlandChoices {
    var type: String?
}

@JsPlainObject
external interface RawGovernmentChoices {
    var type: String?
    var abilityBoosts: RawAbilityBoostChoices
    var featRuinThresholdIncreases: Array<RawRuinThresholdIncreasesContext>?
    var featSupportedLeader: String?
}

@JsPlainObject
external interface RawAbilityBoostChoices {
    var culture: Boolean
    var economy: Boolean
    var loyalty: Boolean
    var stability: Boolean
}

fun RawAbilityBoostChoices.getBoosts() =
    listOfNotNull(
        culture.takeIf { it == true }?.let { "culture" },
        economy.takeIf { it == true }?.let { "economy" },
        loyalty.takeIf { it == true }?.let { "loyalty" },
        stability.takeIf { it == true }?.let { "stability" },
    )

@JsPlainObject
external interface RawRuinThresholdIncreaseContext {
    val value: Int
    val increase: Boolean
}

@JsPlainObject
external interface RawRuinThresholdIncreasesContext {
    val crime: RawRuinThresholdIncreaseContext
    val corruption: RawRuinThresholdIncreaseContext
    val strife: RawRuinThresholdIncreaseContext
    val decay: RawRuinThresholdIncreaseContext
}

data class RuinThresholdIncreases(
    val crime: Int = 0,
    val corruption: Int = 0,
    val strife: Int = 0,
    val decay: Int = 0,
) {
    operator fun plus(other: RuinThresholdIncreases) =
        RuinThresholdIncreases(
            crime = crime + other.crime,
            corruption = corruption + other.corruption,
            strife = strife + other.strife,
            decay = decay + other.decay,
        )
}

fun RawRuinThresholdIncreasesContext.parse() = RuinThresholdIncreases(
    crime = if (crime.increase) crime.value else 0,
    corruption = if (corruption.increase) corruption.value else 0,
    strife = if (strife.increase) strife.value else 0,
    decay = if (decay.increase) decay.value else 0,
)

@JsPlainObject
external interface RawFeatureChoices {
    var id: String
    var skillIncrease: String?
    var abilityBoosts: RawAbilityBoostChoices?
    var featId: String?
    var ruinThresholdIncreases: RawRuinThresholdIncreasesContext?
    var featRuinThresholdIncreases: Array<RawRuinThresholdIncreasesContext>
    var supportedLeader: String?
}

@JsPlainObject
external interface RawBonusFeat {
    var id: String
    var ruinThresholdIncreases: Array<RawRuinThresholdIncreasesContext>
    var supportedLeader: String?
}


fun KingdomData.getChosenGovernment() =
    getGovernments()
        .find { it.id == government.type }

fun KingdomData.getChosenCharter() =
    getCharters()
        .find { it.id == charter.type }

fun KingdomData.getChosenHeartland() =
    getHeartlands()
        .find { it.id == heartland.type }

data class ChosenFeature(
    val feature: RawExplodedKingdomFeature,
    val choice: RawFeatureChoices
)

fun KingdomData.getChosenFeatures(
    allFeatures: List<RawExplodedKingdomFeature>
): List<ChosenFeature> {
    val choicesById = features.associateBy { it.id }
    return allFeatures
        .filter { it.level <= level }
        .mapNotNull { feature ->
            choicesById[feature.id]?.let { choice ->
                ChosenFeature(
                    choice = choice,
                    feature = feature
                )
            }
        }
}