package com.foundryvtt.core.helpers

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface SettingsRange {
    val max: Int
    val min: Int
    val step: Int
}