package at.posselt.pfrpg2e.kingdom

import kotlinx.js.JsPlainObject
import kotlinx.serialization.json.JsonElement

@JsPlainObject
external interface RawHeartland {
    val name: String
    val description: String
    val boost: String
}

@JsModule("./heartlands.json")
external val heartlands: Array<RawHeartland>

@JsModule("./schemas/heartland.json")
external val heartlandSchema: JsonElement