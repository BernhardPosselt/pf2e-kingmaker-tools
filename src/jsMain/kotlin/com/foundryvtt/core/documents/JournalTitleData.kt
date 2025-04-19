package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface JournalTitleData {
    var show: Boolean
    var level: Int
}