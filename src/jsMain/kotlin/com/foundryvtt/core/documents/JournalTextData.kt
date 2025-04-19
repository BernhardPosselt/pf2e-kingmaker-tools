package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface JournalTextData {
    var content: String
    var markdown: String
    var format: Int
}