package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface GetSpeakerOptions {
    val scene: Scene?
    val actor: Actor?
    val token: TokenDocument?
    val alias: String?
}