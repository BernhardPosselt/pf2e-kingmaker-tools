package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface RollMessageOptions {
    val rollMode: String?
    val create: Boolean?
}