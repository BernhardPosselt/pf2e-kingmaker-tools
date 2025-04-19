package com.foundryvtt.core.documents

import com.foundryvtt.core.dice.Roll
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface DrawOptions {
    val roll: Roll?
    val recursive: Boolean?
    val results: Array<TableResult>?
    val displayChat: Boolean?
    val rollMode: String?
}