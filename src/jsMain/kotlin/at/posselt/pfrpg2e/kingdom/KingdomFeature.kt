package at.posselt.pfrpg2e.kingdom

import js.objects.JsPlainObject
import kotlinx.serialization.json.JsonElement

@JsPlainObject
external interface RawCharter {
    val name: String
    val description: String
    val flaw: String
    val freeBoosts: Int
    val boost: String
}

@JsPlainObject
external interface RawGovernment {
    val name: String
    val description: String
    val boosts: Array<String>
    val freeBoosts: Int
    val bonusFeat: String
    val skillProficiencies: Array<String>
}

@JsPlainObject
external interface RawHeartland {
    val name: String
    val description: String
    val boost: String
}


@JsPlainObject
external interface KingdomFeature {
    val levels: Array<Int>
    val name: String
    val description: String
    val flags: Array<String>?
    val modifiers: Array<RawModifier>?
    val freeBoosts: Int?
    val skillProficiencies: Int?
    val charters: Array<RawCharter>
    val heartlands: Array<RawHeartland>
    val governments: Array<RawGovernment>
}


@JsModule("./features.json")
external val features: Array<KingdomFeature>

@JsModule("./schemas/feature.json")
external val featureSchema: JsonElement