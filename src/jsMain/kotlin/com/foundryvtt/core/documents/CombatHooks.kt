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
fun Combat.update(data: Combat, operation: DatabaseUpdateOperation = unsafeJso()): Promise<Combat?> =
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