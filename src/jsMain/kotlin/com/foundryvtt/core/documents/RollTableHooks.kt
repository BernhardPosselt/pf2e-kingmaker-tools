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
fun RollTable.update(data: RollTable, operation: DatabaseUpdateOperation = jso()): Promise<RollTable?> =
    update(data as AnyObject, operation)

fun <O> HooksEventListener.onPreCreateRollTable(callback: PreCreateDocumentCallback<RollTable, O>) =
    on("preCreateRollTable", callback)

fun <O> HooksEventListener.onPreUpdateRollTable(callback: PreUpdateDocumentCallback<RollTable, O>): Unit =
    on("preUpdateRollTable", callback)

fun <O> HooksEventListener.onPreDeleteRollTable(callback: PreDeleteDocumentCallback<RollTable, O>) =
    on("preDeleteRollTable", callback)

fun <O> HooksEventListener.onCreateRollTable(callback: CreateDocumentCallback<RollTable, O>) =
    on("createRollTable", callback)

fun <O> HooksEventListener.onUpdateRollTable(callback: UpdateDocumentCallback<RollTable, O>) =
    on("updateRollTable", callback)

fun <O> HooksEventListener.onDeleteRollTable(callback: DeleteDocumentCallback<RollTable, O>) =
    on("deleteRollTable", callback)