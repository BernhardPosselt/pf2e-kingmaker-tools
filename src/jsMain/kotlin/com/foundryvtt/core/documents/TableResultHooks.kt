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
fun TableResult.update(data: TableResult, operation: DatabaseUpdateOperation = jso()): Promise<TableResult?> =
    update(data as AnyObject, operation)

fun <O> HooksEventListener.onPreCreateTableResult(callback: PreCreateDocumentCallback<TableResult, O>) =
    on("preCreateTableResult", callback)

fun <O> HooksEventListener.onPreUpdateTableResult(callback: PreUpdateDocumentCallback<TableResult, O>): Unit =
    on("preUpdateTableResult", callback)

fun <O> HooksEventListener.onPreDeleteTableResult(callback: PreDeleteDocumentCallback<TableResult, O>) =
    on("preDeleteTableResult", callback)

fun <O> HooksEventListener.onCreateTableResult(callback: CreateDocumentCallback<TableResult, O>) =
    on("createTableResult", callback)

fun <O> HooksEventListener.onUpdateTableResult(callback: UpdateDocumentCallback<TableResult, O>) =
    on("updateTableResult", callback)

fun <O> HooksEventListener.onDeleteTableResult(callback: DeleteDocumentCallback<TableResult, O>) =
    on("deleteTableResult", callback)