package at.posselt.pfrpg2e.kingdom.data

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface RawAbilityScores {
    val economy: Int
    val stability: Int
    val loyalty: Int
    val culture: Int
}