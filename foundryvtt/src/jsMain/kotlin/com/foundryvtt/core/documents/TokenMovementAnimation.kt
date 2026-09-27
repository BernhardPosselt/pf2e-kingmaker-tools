package com.foundryvtt.core.documents

import js.core.Void
import kotlinx.js.JsPlainObject
import kotlin.js.Promise

@JsPlainObject
external interface TokenMovementAnimation {
    val started: Promise<Void>
    val ended: Promise<Void>
    val duration: Int
}