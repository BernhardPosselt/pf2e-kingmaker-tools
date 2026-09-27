package com.foundryvtt.core.canvas.placeables

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface OffsetOptions {
    val offsetX: Int?
    val offsetY: Int?
}