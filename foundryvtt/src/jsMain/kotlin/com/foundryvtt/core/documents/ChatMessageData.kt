package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface ChatMessageData {
    val _id: String
    val type: String?
    val user: String
    val timestamp: Int
    val flavor: String
    val content: String
    val speaker: ChatSpeakerData
    val whisper: Array<String>
    val blind: Boolean?
    val rolls: Array<String>
    val sound: String
    val emote: Boolean
}