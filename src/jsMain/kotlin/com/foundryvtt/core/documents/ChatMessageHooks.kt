package com.foundryvtt.core.documents

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import com.foundryvtt.core.helpers.CreateDocumentCallback
import com.foundryvtt.core.helpers.DeleteDocumentCallback
import com.foundryvtt.core.helpers.HooksEventListener
import com.foundryvtt.core.helpers.PreCreateDocumentCallback
import com.foundryvtt.core.helpers.PreDeleteDocumentCallback
import com.foundryvtt.core.helpers.PreUpdateDocumentCallback
import com.foundryvtt.core.helpers.UpdateDocumentCallback
import js.objects.jso
import kotlin.js.Promise


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