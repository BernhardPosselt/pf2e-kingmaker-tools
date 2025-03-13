package at.posselt.pfrpg2e.kingdom.data

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface RawResources {
    var now: Int
    var next: Int
}

fun RawResources.endTurn() = RawResources(
    now = next,
    next = 0,
)