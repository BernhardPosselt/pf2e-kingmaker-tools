package com.foundryvtt.core.dice

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.documents.ChatMessage
import kotlin.js.Promise

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