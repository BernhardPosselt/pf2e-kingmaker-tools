package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface Permissions {
    val create: Function<Boolean>
    val update: Function<Boolean>
    val delete: Function<Boolean>
}