package com.foundryvtt.core.documents

import com.foundryvtt.core.*
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import js.objects.jso
import kotlin.js.Promise

@JsName("CONFIG.TableResult.documentClass")
@Suppress("NAME_CONTAINS_ILLEGAL_CHARS")
external class TableResult : ClientDocument {
    companion object : DocumentStatic<TableResult>

    override fun delete(operation: DatabaseDeleteOperation): Promise<TableResult>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<TableResult?>

    val icon: String
    fun getChatText(): String

    var _id: String
    var type: String
    var text: String
    var img: String
    var documentCollection: String
    var documentId: String
    var weight: Int
    var range: Array<Int>
    var drawn: Boolean
}

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