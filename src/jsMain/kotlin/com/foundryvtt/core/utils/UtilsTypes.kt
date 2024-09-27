package com.foundryvtt.core.utils

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface DiffObjectOptions {
    val inner: Boolean?
    val deletionKeys: Boolean?
}