package com.foundryvtt.core.helpers

import com.foundryvtt.core.AnyMutableObject
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseCreateOperation
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import com.foundryvtt.core.applications.api.ApplicationV2
import com.foundryvtt.core.applications.api.ContextMenuEntry
import com.foundryvtt.core.applications.ui.Hotbar
import com.foundryvtt.core.canvas.Canvas
import com.foundryvtt.core.canvas.groups.CanvasVisibility
import com.foundryvtt.core.documents.ChatMessage
import com.foundryvtt.core.documents.TokenDocument
import org.w3c.dom.HTMLElement


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
    html: HTMLElement,
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

fun <O> HooksEventListener.onRenderChatMessage(callback: (message: ChatMessage, html: HTMLElement, messageData: AnyObject) -> O) =
    on("renderChatMessageHTML", callback)

fun <O> HooksEventListener.onGetChatMessageContextOptions(callback: ApplicationEntryContext<O>) =
    on("getChatMessageContextOptions", callback)

fun <O> HooksEventListener.onSightRefresh(callback: (CanvasVisibility) -> O) =
    on("sightRefresh", callback)

fun <O> HooksEventListener.onApplyTokenStatusEffect(callback: (TokenDocument, String, Boolean) -> O) =
    on("applyTokenStatusEffect", callback)

fun <O> HooksEventListener.onHotBarDrop(callback: (bar: Hotbar, data: AnyObject, slot: Int) -> O) =
    on("hotbarDrop", callback)

fun <O> HooksEventListener.onI18NInit(callback: () -> O) = on("i18nInit", callback)

object TypedHooks: HooksEventListener {
    override fun <T> on(key: String, callback: Function<T>) {
        Hooks.on(key, callback)
    }
    fun <T> off(key: String, callback: Function<T>) {
        Hooks.off(key, callback)
    }
}