package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface Darkness {
    val min: Double
    val max: Double
}

@JsPlainObject
external interface GlobalLightData {
    val enabled: Boolean
    val bright: Boolean
    val alpha: Double
    val color: String?
    val coloration: Double
    val contrast: Double
    val darkness: Darkness
    val luminosity: Double
    val shadows: Double
    val saturation: Double
}