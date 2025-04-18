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
fun TileDocument.update(data: TileDocument, operation: DatabaseUpdateOperation = jso()): Promise<TileDocument?> =
    update(data as AnyObject, operation)

fun <O> HooksEventListener.onPreCreateTile(callback: PreCreateDocumentCallback<TileDocument, O>) =
    on("preCreateTile", callback)

fun <O> HooksEventListener.onPreUpdateTile(callback: PreUpdateDocumentCallback<TileDocument, O>): Unit =
    on("preUpdateTile", callback)

fun <O> HooksEventListener.onPreDeleteTile(callback: PreDeleteDocumentCallback<TileDocument, O>) =
    on("preDeleteTile", callback)

fun <O> HooksEventListener.onCreateTile(callback: CreateDocumentCallback<TileDocument, O>) =
    on("createTile", callback)

fun <O> HooksEventListener.onUpdateTile(callback: UpdateDocumentCallback<TileDocument, O>) =
    on("updateTile", callback)

fun <O> HooksEventListener.onDeleteTile(callback: DeleteDocumentCallback<TileDocument, O>) =
    on("deleteTile", callback)