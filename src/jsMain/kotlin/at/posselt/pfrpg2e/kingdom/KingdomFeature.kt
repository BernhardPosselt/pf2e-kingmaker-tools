package at.posselt.pfrpg2e.kingdom

import js.objects.JsPlainObject
import kotlinx.serialization.json.JsonElement

@JsPlainObject
external interface KingdomFeature {
    val levels: Array<Int>
    val name: String
    val description: String
    val flags: Array<String>?
    val modifiers: Array<Modifier>?
}


@JsModule("./features.json")
external val features: Array<KingdomFeature>

@JsModule("./schemas/feature.json")
external val featureSchema: JsonElement