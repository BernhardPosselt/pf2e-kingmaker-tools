package at.posselt.pfrpg2e.kingdom

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface RawHeartland {
    val id: String
    val name: String
    val description: String
    val boost: String
}

@JsModule("./heartlands.json")
external val heartlands: Array<RawHeartland>


fun KingdomData.getHeartlands(): Array<RawHeartland> {
    val overrides = homebrewHeartlands.map { it.id }.toSet()
    return homebrewHeartlands + heartlands.filter { it.id !in overrides }
}