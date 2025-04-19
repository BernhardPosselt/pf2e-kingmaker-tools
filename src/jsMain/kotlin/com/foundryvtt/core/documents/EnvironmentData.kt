package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface EnvironmentData {
    val hue: Double
    val saturation: Double
    val intensity: Double
    val luminosity: Double
    val shadows: Double
}