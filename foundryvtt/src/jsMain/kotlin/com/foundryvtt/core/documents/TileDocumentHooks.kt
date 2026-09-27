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
import js.objects.unsafeJso
import kotlin.js.Promise


@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
fun TileDocument.update(data: TileDocument, operation: DatabaseUpdateOperation = unsafeJso()): Promise<TileDocument?> =
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