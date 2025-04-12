package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.utils.t
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface RawHeartland {
    val id: String
    val name: String
    val description: String
    val boost: String
}

@JsModule("./heartlands.json")
private external val heartlands: Array<RawHeartland>

private fun RawHeartland.translate() =
    copy(
        name = t(name),
        description = t(description)
    )

private var translatedHeartlands = emptyArray<RawHeartland>()

fun translateHeartlands() {
    translatedHeartlands = heartlands
        .map { it.translate() }
        .toTypedArray()
}


fun KingdomData.getHeartlands(): Array<RawHeartland> {
    val overrides = homebrewHeartlands.map { it.id }.toSet()
    return homebrewHeartlands + translatedHeartlands.filter { it.id !in overrides }
}