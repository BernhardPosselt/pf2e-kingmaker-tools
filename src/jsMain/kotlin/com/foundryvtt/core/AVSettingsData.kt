package com.foundryvtt.core

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface AVSettingsData {
    val muted: Boolean
    val hidden: Boolean
    val speaking: Boolean
}