package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface CombatHistoryData {
    val round: Int?
    val turn: Int?
    val tokenId: String?
    val combatantId: String?
}