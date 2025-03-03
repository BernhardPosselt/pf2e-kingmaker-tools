package at.posselt.pfrpg2e.kingdom

import js.objects.JsPlainObject
import kotlinx.serialization.json.JsonElement


@JsPlainObject
external interface KingdomFeature {
    val levels: Array<Int>
    val name: String
    val description: String
    val flags: Array<String>?
    val modifiers: Array<RawModifier>?
    val freeBoosts: Int?
    val skillProficiencies: Int?
}

fun KingdomFeature.explodeLevels(): List<ExplodedKingdomFeature> =
    levels.map {
        ExplodedKingdomFeature(
            level = it,
            levels = levels,
            name = name,
            description = description,
            flags = flags,
            modifiers = modifiers,
            freeBoosts = freeBoosts,
            skillProficiencies = skillProficiencies,
        )
    }

@JsPlainObject
external interface ExplodedKingdomFeature : KingdomFeature {
    val level: Int
}


@JsModule("./features.json")
external val kingdomFeatures: Array<KingdomFeature>

@JsModule("./schemas/feature.json")
external val kingdomFeatureSchema: JsonElement