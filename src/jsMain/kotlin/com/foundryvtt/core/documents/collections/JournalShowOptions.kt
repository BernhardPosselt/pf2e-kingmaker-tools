package com.foundryvtt.core.documents.collections

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface JournalShowOptions {
    val users: Array<String>?
    val force: Boolean?
}