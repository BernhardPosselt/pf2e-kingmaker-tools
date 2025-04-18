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
fun Macro.update(data: Macro, operation: DatabaseUpdateOperation = jso()): Promise<Macro?> =
    update(data as AnyObject, operation)

fun <O> HooksEventListener.onPreCreateMacro(callback: PreCreateDocumentCallback<Macro, O>) =
    on("preCreateMacro", callback)

fun <O> HooksEventListener.onPreUpdateMacro(callback: PreUpdateDocumentCallback<Macro, O>): Unit =
    on("preUpdateMacro", callback)

fun <O> HooksEventListener.onPreDeleteMacro(callback: PreDeleteDocumentCallback<Macro, O>) =
    on("preDeleteMacro", callback)

fun <O> HooksEventListener.onCreateMacro(callback: CreateDocumentCallback<Macro, O>) =
    on("createMacro", callback)

fun <O> HooksEventListener.onUpdateMacro(callback: UpdateDocumentCallback<Macro, O>) =
    on("updateMacro", callback)

fun <O> HooksEventListener.onDeleteMacro(callback: DeleteDocumentCallback<Macro, O>) =
    on("deleteMacro", callback)