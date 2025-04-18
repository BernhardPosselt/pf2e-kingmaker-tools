package com.foundryvtt.core.documents

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.CreateDocumentCallback
import com.foundryvtt.core.DeleteDocumentCallback
import com.foundryvtt.core.HooksEventListener
import com.foundryvtt.core.PreCreateDocumentCallback
import com.foundryvtt.core.PreDeleteDocumentCallback
import com.foundryvtt.core.PreUpdateDocumentCallback
import com.foundryvtt.core.UpdateDocumentCallback
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import js.objects.jso
import kotlin.js.Promise


@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
fun TokenDocument.update(data: TokenDocument, operation: DatabaseUpdateOperation = jso()): Promise<TokenDocument?> =
    update(data as AnyObject, operation)


fun <O> HooksEventListener.onPreCreateToken(callback: PreCreateDocumentCallback<TokenDocument, O>) =
    on("preCreateToken", callback)

fun <O> HooksEventListener.onPreUpdateToken(callback: PreUpdateDocumentCallback<TokenDocument, O>): Unit =
    on("preUpdateToken", callback)

fun <O> HooksEventListener.onPreDeleteToken(callback: PreDeleteDocumentCallback<TokenDocument, O>) =
    on("preDeleteToken", callback)

fun <O> HooksEventListener.onCreateToken(callback: CreateDocumentCallback<TokenDocument, O>) =
    on("createToken", callback)

fun <O> HooksEventListener.onUpdateToken(callback: UpdateDocumentCallback<TokenDocument, O>) =
    on("updateToken", callback)

fun <O> HooksEventListener.onDeleteToken(callback: DeleteDocumentCallback<TokenDocument, O>) =
    on("deleteToken", callback)