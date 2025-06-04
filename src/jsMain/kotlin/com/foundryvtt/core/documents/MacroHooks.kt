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
fun Macro.update(data: Macro, operation: DatabaseUpdateOperation = unsafeJso()): Promise<Macro?> =
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