package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface DetectionModeData {
    val id: String
    val enabled: Boolean
    val range: Double
}