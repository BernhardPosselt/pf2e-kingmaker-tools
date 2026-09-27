package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface TileVideo {
    val loop: Boolean
    val autoplay: Boolean
    val alpha: Double
}