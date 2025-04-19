package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface TileRestrictions {
    val light: Boolean
    val weather: Boolean
}