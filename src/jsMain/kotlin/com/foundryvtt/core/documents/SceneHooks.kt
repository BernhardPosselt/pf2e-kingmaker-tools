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
fun Scene.update(data: Scene, operation: DatabaseUpdateOperation = jso()): Promise<Scene?> =
    update(data as AnyObject, operation)

fun <O> HooksEventListener.onPreCreateScene(callback: PreCreateDocumentCallback<Scene, O>) =
    on("preCreateScene", callback)

fun <O> HooksEventListener.onPreUpdateScene(callback: PreUpdateDocumentCallback<Scene, O>): Unit =
    on("preUpdateScene", callback)

fun <O> HooksEventListener.onPreDeleteScene(callback: PreDeleteDocumentCallback<Scene, O>) =
    on("preDeleteScene", callback)

fun <O> HooksEventListener.onCreateScene(callback: CreateDocumentCallback<Scene, O>) =
    on("createScene", callback)

fun <O> HooksEventListener.onUpdateScene(callback: UpdateDocumentCallback<Scene, O>) =
    on("updateScene", callback)

fun <O> HooksEventListener.onDeleteScene(callback: DeleteDocumentCallback<Scene, O>) =
    on("deleteScene", callback)