package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface HotbarMacros {
    val slot: Int
    val macro: Macro?
}