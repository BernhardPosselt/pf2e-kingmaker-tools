package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface TokenConstraintOptions {
    val ignoreCost: Boolean
    val ignoreWalls: Boolean
}