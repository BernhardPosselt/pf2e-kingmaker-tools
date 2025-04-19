package com.foundryvtt.core.documents

import com.foundryvtt.core.AnyObject
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface ToggleCombatantOptions {
    val active: Boolean?
    val options: AnyObject?
}