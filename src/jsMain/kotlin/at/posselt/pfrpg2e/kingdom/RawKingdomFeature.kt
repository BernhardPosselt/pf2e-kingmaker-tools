package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.utils.t
import js.objects.JsPlainObject


@JsPlainObject
external interface RawKingdomFeature {
    val id: String
    val levels: Array<Int>
    val name: String
    val automationNotes: String?
    val description: String
    val rollOptions: Array<String>?
    val modifiers: Array<RawModifier>?
    val freeBoosts: Int?
    val skillProficiencies: Int?
    val abilityBoosts: Int?
    val ruinThresholdIncreases: RawRuinThresholdIncreases?
    val skillIncrease: Boolean?
    val kingdomFeat: Boolean?
    val claimHexAttempts: Int?
}

fun KingdomData.getFeatures(): Array<RawKingdomFeature> =
    if (settings.kingdomSkillIncreaseEveryLevel) {
        translatedKingdomFeatures.map {
            if (it.id == "skill-increase") {
                it.copy(levels = (2..20).toList().toTypedArray())
            } else {
                it
            }
        }.toTypedArray()
    } else {
        translatedKingdomFeatures
    }

fun KingdomData.getExplodedFeatures() =
    getFeatures().flatMap { it.explodeLevels() }

fun RawKingdomFeature.explodeLevels(): List<RawExplodedKingdomFeature> =
    levels.map {
        RawExplodedKingdomFeature(
            id = "$id-level-$it",
            level = it,
            levels = levels,
            name = name,
            description = description,
            rollOptions = rollOptions,
            modifiers = modifiers,
            freeBoosts = freeBoosts,
            skillProficiencies = skillProficiencies,
            abilityBoosts = abilityBoosts,
            ruinThresholdIncreases = ruinThresholdIncreases,
            skillIncrease = skillIncrease,
            kingdomFeat = kingdomFeat,
            claimHexAttempts = claimHexAttempts,
            automationNotes = automationNotes,
        )
    }

@JsPlainObject
external interface RawExplodedKingdomFeature : RawKingdomFeature {
    val level: Int
}


@JsModule("./features.json")
private external val kingdomFeatures: Array<RawKingdomFeature>

private fun RawKingdomFeature.translate() =
    copy(
        name = t(name),
        description = t(description),
        automationNotes = automationNotes?.let { t(it) },
    )

private var translatedKingdomFeatures = emptyArray<RawKingdomFeature>()

fun translateKingdomFeatures() {
    translatedKingdomFeatures = kingdomFeatures
        .map { it.translate() }
        .toTypedArray()
}