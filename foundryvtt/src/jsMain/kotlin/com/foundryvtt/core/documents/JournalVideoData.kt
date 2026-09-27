package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface JournalVideoData {
    var controls: Boolean
    var loop: Boolean
    var autoplay: Boolean
    var voluem: Double
    var timestamp: Int
    var width: Int
    var height: Int
}