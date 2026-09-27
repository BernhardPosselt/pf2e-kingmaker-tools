package com.foundryvtt.pf2e.actions

import com.foundryvtt.pf2e.actor.PF2EActor
import kotlinx.js.JsPlainObject
import org.w3c.dom.events.Event

@JsPlainObject
external interface ActionVariantUseOptionsMessage {
    val create: Boolean?
}

@JsPlainObject
external interface ActionVariantUseOptions {
    val actors: Array<out PF2EActor>?
    val event: Event?
    val message: ActionVariantUseOptionsMessage?
    val traits: Array<String>?
    val target: Any?  // Actor or Token
}