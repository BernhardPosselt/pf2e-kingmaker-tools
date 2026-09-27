package com.foundryvtt.core.documents

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.dice.Roll
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface TableMessageOptions {
    val roll: Roll?
    val messageData: AnyObject?
    val messageOptions: AnyObject?
}