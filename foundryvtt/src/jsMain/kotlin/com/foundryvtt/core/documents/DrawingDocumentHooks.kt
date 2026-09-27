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
fun DrawingDocument.update(
    data: DrawingDocument,
    operation: DatabaseUpdateOperation = unsafeJso()
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