package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface PingData {
    val pull: Boolean?
    val style: String
    val scene: String
    val zoom: Int
}