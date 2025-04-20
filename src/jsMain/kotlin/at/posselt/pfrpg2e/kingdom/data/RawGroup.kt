package at.posselt.pfrpg2e.kingdom.data

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface RawGroup {
    var name: String
    var negotiationDC: Int
    var atWar: Boolean
    var preventPledgeOfFealty: Boolean
    var relations: String  // none, diplomatic-relations, trade-agreement
}