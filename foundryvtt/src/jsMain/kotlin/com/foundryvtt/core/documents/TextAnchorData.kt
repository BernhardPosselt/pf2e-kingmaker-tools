package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface TextAnchorData {
    val scale: Double?
    val duration: Int?
}