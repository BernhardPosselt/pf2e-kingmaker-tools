package com.foundryvtt.core.dice

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface RollContext {
    val minimize: Boolean?
    val maximize: Boolean?
    val allowStrings: Boolean?
    val allowInteractive: Boolean?
}