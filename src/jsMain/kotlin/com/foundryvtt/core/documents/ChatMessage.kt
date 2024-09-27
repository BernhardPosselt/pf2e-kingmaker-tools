package com.foundryvtt.core.documents

import com.foundryvtt.core.*
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import io.kvision.jquery.JQuery
import js.objects.ReadonlyRecord
import js.objects.jso
import kotlinx.js.JsPlainObject
import kotlin.js.Promise

@JsPlainObject
external interface ChatSpeakerData {
    val scene: String?
    val actor: String?
    val token: String?
    val alias: String?
}


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

@JsPlainObject
external interface GetSpeakerOptions {
    val scene: Scene?
    val actor: Actor?
    val token: TokenDocument?
    val alias: String?
}

@JsPlainObject
external interface SpeakerData {
    val scene: String?
    val actor: String?
    val token: String?
    val alias: String?
}

// required to make instance of work, but since the classes are not registered here
// at page load, we can't use @file:JsQualifier
@JsName("CONFIG.ChatMessage.documentClass")
@Suppress("NAME_CONTAINS_ILLEGAL_CHARS")
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
    fun getHTML(): Promise<JQuery>
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
fun ChatMessage.update(data: ChatMessage, operation: DatabaseUpdateOperation = jso()): Promise<ChatMessage?> =
    update(data as AnyObject, operation)

fun <O> HooksEventListener.onPreCreateChatMessage(callback: PreCreateDocumentCallback<ChatMessage, O>) =
    on("preCreateChatMessage", callback)

fun <O> HooksEventListener.onPreUpdateChatMessage(callback: PreUpdateDocumentCallback<ChatMessage, O>): Unit =
    on("preUpdateChatMessage", callback)

fun <O> HooksEventListener.onPreDeleteChatMessage(callback: PreDeleteDocumentCallback<ChatMessage, O>) =
    on("preDeleteChatMessage", callback)

fun <O> HooksEventListener.onCreateChatMessage(callback: CreateDocumentCallback<ChatMessage, O>) =
    on("createChatMessage", callback)

fun <O> HooksEventListener.onUpdateChatMessage(callback: UpdateDocumentCallback<ChatMessage, O>) =
    on("updateChatMessage", callback)

fun <O> HooksEventListener.onDeleteChatMessage(callback: DeleteDocumentCallback<ChatMessage, O>) =
    on("deleteChatMessage", callback)