package at.posselt.pfrpg2e.kingdom.data

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface MilestoneChoice {
    var id: String
    var completed: Boolean
    var enabled: Boolean
}