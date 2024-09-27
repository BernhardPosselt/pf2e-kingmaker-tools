package com.foundryvtt.pf2e.actions

import com.foundryvtt.core.documents.ChatMessage
import com.foundryvtt.pf2e.actor.PF2EActor
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface CheckResultCallback {
    val actor: PF2EActor
    val message: ChatMessage?
    val outcome: String?
    val roll: CheckRoll
}