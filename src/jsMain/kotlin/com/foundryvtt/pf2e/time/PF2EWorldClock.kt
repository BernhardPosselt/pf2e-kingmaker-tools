package com.foundryvtt.pf2e.time

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface PF2EWorldClock {
    val month: String
}