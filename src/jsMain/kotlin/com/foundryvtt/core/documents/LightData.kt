package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface LightData {
    val negative: Boolean
    val priority: Int
    var alpha: Boolean
    var angle: Int
}