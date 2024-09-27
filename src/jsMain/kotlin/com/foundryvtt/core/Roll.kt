package com.foundryvtt.core

import com.foundryvtt.core.documents.ChatMessage
import kotlinx.js.JsPlainObject
import kotlin.js.Promise

@JsPlainObject
external interface RollContext {
    val minimize: Boolean?
    val maximize: Boolean?
    val allowStrings: Boolean?
    val allowInteractive: Boolean?
}

@JsPlainObject
external interface RollMessageOptions {
    val rollMode: String?
    val create: Boolean?
}

open external class Roll(formula: String) {
    val formula: String
    val result: String
    val total: Int
    fun toMessage(
        data: AnyObject = definedExternally,
        options: RollMessageOptions = definedExternally
    ): Promise<ChatMessage>

    fun evaluate(context: RollContext = definedExternally): Promise<Roll>
}