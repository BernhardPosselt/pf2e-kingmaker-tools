package com.foundryvtt.core.documents

import com.foundryvtt.core.AnyObject
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface RollInitiativeOptions {
    val formula: String?
    val updateTurn: Boolean?
    val messageOptions: AnyObject?
}