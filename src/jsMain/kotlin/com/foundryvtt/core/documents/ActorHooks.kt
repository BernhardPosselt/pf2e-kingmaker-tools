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
fun Actor.update(data: Actor, operation: DatabaseUpdateOperation = unsafeJso()): Promise<Actor?> =
    update(data as AnyObject, operation)

@Suppress("unused")
fun <O> HooksEventListener.onPreCreateActor(callback: PreCreateDocumentCallback<Actor, O>) =
    on("preCreateActor", callback)

fun <O> HooksEventListener.onPreUpdateActor(callback: PreUpdateDocumentCallback<Actor, O>): Unit =
    on("preUpdateActor", callback)

@Suppress("unused")
fun <O> HooksEventListener.onPreDeleteActor(callback: PreDeleteDocumentCallback<Actor, O>) =
    on("preDeleteActor", callback)

@Suppress("unused")
fun <O> HooksEventListener.onCreateActor(callback: CreateDocumentCallback<Actor, O>) =
    on("createActor", callback)

fun <O> HooksEventListener.onUpdateActor(callback: UpdateDocumentCallback<Actor, O>) =
    on("updateActor", callback)

fun <O> HooksEventListener.onDeleteActor(callback: DeleteDocumentCallback<Actor, O>) =
    on("deleteActor", callback)