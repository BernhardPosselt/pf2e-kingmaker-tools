package com.foundryvtt.core.canvas

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface BlurOptions {
    val enabled: Boolean
    val blurClass: JsClass<*>
    val strength: Int
    val passes: Int
    val kernels: Int
}