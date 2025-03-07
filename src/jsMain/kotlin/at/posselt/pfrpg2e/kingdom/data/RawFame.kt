package at.posselt.pfrpg2e.kingdom.data

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface RawFame {
    var now: Int
    var next: Int
    var type: String
}