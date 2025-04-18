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
fun Actor.update(data: Actor, operation: DatabaseUpdateOperation = jso()): Promise<Actor?> =
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