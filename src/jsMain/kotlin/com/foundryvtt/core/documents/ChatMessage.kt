@file:JsQualifier("foundry.documents")
package com.foundryvtt.core.documents

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import com.foundryvtt.core.dice.Roll
import js.objects.ReadonlyRecord
import org.w3c.dom.HTMLElement
import kotlin.js.Promise


external class ChatMessage : ClientDocument {
    companion object : DocumentStatic<ChatMessage> {
        fun applyRollMode(data: Any, rollMode: String)
        fun getWhisperRecipients(name: String)
        fun getSpeaker(options: GetSpeakerOptions = definedExternally): SpeakerData
        fun getSpeakerActor(speaker: AnyObject): Actor?
    }

    override fun delete(operation: DatabaseDeleteOperation): Promise<ChatMessage>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<ChatMessage?>

    val alias: String
    val isAuthor: Boolean
    val isContentVisible: Boolean
    val isRoll: Boolean

    var _id: String
    var blind: Boolean
    var content: String
    var emote: Boolean
    var flavor: String
    var logged: Boolean
    var timestamp: Int
    var style: Int
    var type: String
    var system: AnyObject
    var author: User
    var speaker: SpeakerData
    var whisper: Array<String>
    var rolls: Array<Roll>
    var sound: String

    val _rollExpanded: Boolean

    fun prepareDerivedData()
    fun applyRollMode(rollMode: String)
    fun getRollData(): ReadonlyRecord<String, Any>
    fun renderHTML(): Promise<HTMLElement>
}
