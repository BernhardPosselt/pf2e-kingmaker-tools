package com.foundryvtt.core.documents

import com.foundryvtt.core.dice.Roll
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface RollTableRollOptions {
    val roll: Roll
    val recursive: Boolean?
    val _depth: Int?
}