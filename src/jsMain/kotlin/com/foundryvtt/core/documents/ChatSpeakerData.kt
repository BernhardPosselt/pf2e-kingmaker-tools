package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface ChatSpeakerData {
    val scene: String?
    val actor: String?
    val token: String?
    val alias: String?
}