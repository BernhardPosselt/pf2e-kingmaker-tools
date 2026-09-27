package com.foundryvtt.core.documents

import kotlinx.html.org.w3c.dom.events.Event
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface MacroScope {
    val speaker: ChatSpeakerData
    val actor: Actor
    val token: TokenDocument
    val event: Event
}