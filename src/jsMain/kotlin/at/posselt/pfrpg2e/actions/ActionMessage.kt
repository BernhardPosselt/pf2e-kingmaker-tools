package at.posselt.pfrpg2e.actions

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface ActionMessage {
    val action: String
    val data: Any?
}
