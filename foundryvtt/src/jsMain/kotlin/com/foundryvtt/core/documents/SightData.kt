package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface SightData {
    val enabled: Boolean
    val range: Double
    val angle: Int
    val visionMode: String
    val color: String
    val attenuation: Double
    val brightness: Double
    val saturation: Double
    val contrast: Double
}