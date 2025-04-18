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
fun Combat.update(data: Combat, operation: DatabaseUpdateOperation = jso()): Promise<Combat?> =
    update(data as AnyObject, operation)

fun <O> HooksEventListener.onPreCreateCombat(callback: PreCreateDocumentCallback<Combat, O>) =
    on("preCreateCombat", callback)

fun <O> HooksEventListener.onPreUpdateCombat(callback: PreUpdateDocumentCallback<Combat, O>): Unit =
    on("preUpdateCombat", callback)

fun <O> HooksEventListener.onPreDeleteCombat(callback: PreDeleteDocumentCallback<Combat, O>) =
    on("preDeleteCombat", callback)

fun <O> HooksEventListener.onCreateCombat(callback: CreateDocumentCallback<Combat, O>) =
    on("createCombat", callback)

fun <O> HooksEventListener.onUpdateCombat(callback: UpdateDocumentCallback<Combat, O>) =
    on("updateCombat", callback)

fun <O> HooksEventListener.onDeleteCombat(callback: DeleteDocumentCallback<Combat, O>) =
    on("deleteCombat", callback)