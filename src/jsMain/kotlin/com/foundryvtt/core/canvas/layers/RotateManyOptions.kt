package com.foundryvtt.core.canvas.layers

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface RotateManyOptions {
    val angle: Double?
    val delta: Double?
    val snap: Double?
    val ids: Array<String>?
    val includeLocked: Boolean?
}