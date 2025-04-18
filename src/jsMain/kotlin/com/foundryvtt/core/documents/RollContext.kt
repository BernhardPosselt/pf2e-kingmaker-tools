package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface RollContext {
    val minimize: Boolean?
    val maximize: Boolean?
    val allowStrings: Boolean?
    val allowInteractive: Boolean?
}