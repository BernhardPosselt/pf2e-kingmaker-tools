package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface SceneEnvironmentData {
    val darknessLevel: Double
    val darknessLevelLock: Boolean
    val globalLight: GlobalLightData
    val cycle: Boolean
    val base: EnvironmentData
    val dark: EnvironmentData
}