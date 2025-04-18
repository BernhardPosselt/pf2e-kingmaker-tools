package com.foundryvtt.core.canvas

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface LoadTextureOptions {
    val expireCache: Boolean
    val additionalSources: Array<String>
}