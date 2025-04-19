package com.foundryvtt.core.canvas.layers

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface MoveManyOptions {
    val dx: Int?
    val dy: Int?
    val rotate: Boolean?
    val ids: Array<String>?
    val includeLocked: Boolean?
}