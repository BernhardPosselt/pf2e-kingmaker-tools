package com.foundryvtt.core.documents

import com.foundryvtt.core.dice.Roll
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface RollTableDraw {
    val roll: Roll
    val results: Array<TableResult>
}