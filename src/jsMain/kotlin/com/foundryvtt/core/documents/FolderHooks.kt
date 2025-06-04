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
fun Folder.update(data: Folder, operation: DatabaseUpdateOperation = unsafeJso()): Promise<Folder?> =
    update(data as AnyObject, operation)

fun <O> HooksEventListener.onPreCreateFolder(callback: PreCreateDocumentCallback<Folder, O>) =
    on("preCreateFolder", callback)

fun <O> HooksEventListener.onPreUpdateFolder(callback: PreUpdateDocumentCallback<Folder, O>): Unit =
    on("preUpdateFolder", callback)

fun <O> HooksEventListener.onPreDeleteFolder(callback: PreDeleteDocumentCallback<Folder, O>) =
    on("preDeleteFolder", callback)

fun <O> HooksEventListener.onCreateFolder(callback: CreateDocumentCallback<Folder, O>) =
    on("createFolder", callback)

fun <O> HooksEventListener.onUpdateFolder(callback: UpdateDocumentCallback<Folder, O>) =
    on("updateFolder", callback)

fun <O> HooksEventListener.onDeleteFolder(callback: DeleteDocumentCallback<Folder, O>) =
    on("deleteFolder", callback)