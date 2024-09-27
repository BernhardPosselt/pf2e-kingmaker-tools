package com.foundryvtt.core

import com.foundryvtt.core.abstract.DatabaseCreateOperation
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import com.foundryvtt.core.applications.api.ApplicationV2
import com.foundryvtt.core.applications.api.ContextMenuEntry
import com.foundryvtt.core.documents.ChatMessage
import com.foundryvtt.core.documents.TokenDocument
import io.kvision.jquery.JQuery
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface OnErrorOptions {
    val msg: String?
    val log: String?
    val notify: String?
    val data: Any
}

external interface HooksEventListener {
    fun <T> on(key: String, callback: Function<T>)
}

typealias PreCreateDocumentCallback<T, O> = (
    document: T,
    data: AnyMutableObject,
    options: DatabaseCreateOperation,
    userId: String
) -> O

typealias PreUpdateDocumentCallback<T, O> = (
    document: T,
    changed: AnyMutableObject,
    options: DatabaseUpdateOperation,
    userId: String
) -> O

typealias PreDeleteDocumentCallback<T, O> = (
    document: T,
    options: DatabaseDeleteOperation,
    userId: String
) -> O

typealias CreateDocumentCallback<T, O> = (
    document: T,
    data: AnyMutableObject,
    options: DatabaseCreateOperation,
    userId: String
) -> O

typealias UpdateDocumentCallback<T, O> = (
    document: T,
    changed: AnyMutableObject,
    options: DatabaseUpdateOperation,
    userId: String
) -> O

typealias DeleteDocumentCallback<T, O> = (
    document: T,
    options: DatabaseDeleteOperation,
    userId: String
) -> O

typealias RenderApplication<D, O> = (
    application: ApplicationV2,
    html: JQuery,
    data: D,
) -> O

typealias ApplicationEntryContext<O> = (
    application: ApplicationV2,
    entryOptions: Array<ContextMenuEntry>
) -> O

fun <O> HooksEventListener.onReady(callback: (Any) -> O) =
    on("ready", callback)

fun <O> HooksEventListener.onInit(callback: () -> O) =
    on("init", callback)

fun <O> HooksEventListener.onUpdateWorldTime(callback: (worldTime: Int, deltaInSeconds: Int, options: Any, userId: String) -> O) =
    on("updateWorldTime", callback)

fun <O> HooksEventListener.onCanvasReady(callback: (Canvas) -> O) =
    on("canvasReady", callback)

fun <O> HooksEventListener.onRenderChatLog(callback: RenderApplication<AnyObject, O>) =
    on("renderChatLog", callback)

fun <O> HooksEventListener.onRenderChatMessage(callback: (message: ChatMessage, html: JQuery, messageData: AnyObject) -> O) =
    on("renderChatMessage", callback)

fun <O> HooksEventListener.onGetChatLogEntryContext(callback: ApplicationEntryContext<O>) =
    on("getChatLogEntryContext", callback)

fun <O> HooksEventListener.onSightRefresh(callback: (CanvasVisibility) -> O) =
    on("sightRefresh", callback)

fun <O> HooksEventListener.onApplyTokenStatusEffect(callback: (TokenDocument, String, Boolean) -> O) =
    on("applyTokenStatusEffect", callback)


external object Hooks : HooksEventListener {
    override fun <T> on(key: String, callback: Function<T>)
    fun <T> once(key: String, callback: Function<T>)
    fun <T> off(key: String, callback: Function<T>)
    fun callAll(key: String, args: Array<Any>)
    fun call(key: String, args: Array<Any>)
    fun onError(location: String, error: Throwable, options: OnErrorOptions = definedExternally)
}

