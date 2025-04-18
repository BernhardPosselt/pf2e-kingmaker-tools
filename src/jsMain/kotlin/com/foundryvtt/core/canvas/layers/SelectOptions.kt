package com.foundryvtt.core.canvas.layers

import com.foundryvtt.core.AnyObject
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface SelectOptions {
    val x: Double?
    val y: Double?
    val width: Double?
    val height: Double?
    val releaseOptions: AnyObject?
    val controlOptions: AnyObject?
}