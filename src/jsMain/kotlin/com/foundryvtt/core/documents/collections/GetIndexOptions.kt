package com.foundryvtt.core.documents.collections

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface GetIndexOptions {
    val fields: Array<String>?
}