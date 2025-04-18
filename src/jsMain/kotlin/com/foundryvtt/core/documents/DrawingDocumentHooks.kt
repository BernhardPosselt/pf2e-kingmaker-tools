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
fun DrawingDocument.update(
    data: DrawingDocument,
    operation: DatabaseUpdateOperation = jso()
): Promise<DrawingDocument?> =
    update(data as AnyObject, operation)

fun <O> HooksEventListener.onPreCreateDrawing(callback: PreCreateDocumentCallback<DrawingDocument, O>) =
    on("preCreateDrawing", callback)

fun <O> HooksEventListener.onPreUpdateDrawing(callback: PreUpdateDocumentCallback<DrawingDocument, O>): Unit =
    on("preUpdateDrawing", callback)

fun <O> HooksEventListener.onPreDeleteDrawing(callback: PreDeleteDocumentCallback<DrawingDocument, O>) =
    on("preDeleteDrawing", callback)

fun <O> HooksEventListener.onCreateDrawing(callback: CreateDocumentCallback<DrawingDocument, O>) =
    on("createDrawing", callback)

fun <O> HooksEventListener.onUpdateDrawing(callback: UpdateDocumentCallback<DrawingDocument, O>) =
    on("updateDrawing", callback)

fun <O> HooksEventListener.onDeleteDrawing(callback: DeleteDocumentCallback<DrawingDocument, O>) =
    on("deleteDrawing", callback)