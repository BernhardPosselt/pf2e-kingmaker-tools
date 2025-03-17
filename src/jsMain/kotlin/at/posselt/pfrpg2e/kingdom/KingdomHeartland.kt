package at.posselt.pfrpg2e.kingdom

import kotlinx.js.JsPlainObject
import kotlinx.serialization.json.JsonElement

@JsPlainObject
external interface RawHeartland {
    val id: String
    val name: String
    val description: String
    val boost: String
}

@JsModule("./heartlands.json")
external val heartlands: Array<RawHeartland>

@Suppress("unused")
@JsModule("./schemas/heartland.json")
external val heartlandSchema: JsonElement

fun KingdomData.getHeartlands(): Array<RawHeartland> {
    val overrides = homebrewHeartlands.map { it.id }.toSet()
    return homebrewHeartlands + heartlands.filter { it.id !in overrides }
}