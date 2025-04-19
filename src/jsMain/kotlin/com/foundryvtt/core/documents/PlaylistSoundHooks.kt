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
fun PlaylistSound.update(data: PlaylistSound, operation: DatabaseUpdateOperation = jso()): Promise<PlaylistSound?> =
    update(data as AnyObject, operation)

fun <O> HooksEventListener.onPreCreatePlaylistSound(callback: PreCreateDocumentCallback<PlaylistSound, O>) =
    on("preCreatePlaylistSound", callback)

fun <O> HooksEventListener.onPreUpdatePlaylistSound(callback: PreUpdateDocumentCallback<PlaylistSound, O>): Unit =
    on("preUpdatePlaylistSound", callback)

fun <O> HooksEventListener.onPreDeletePlaylistSound(callback: PreDeleteDocumentCallback<PlaylistSound, O>) =
    on("preDeletePlaylistSound", callback)

fun <O> HooksEventListener.onCreatePlaylistSound(callback: CreateDocumentCallback<PlaylistSound, O>) =
    on("createPlaylistSound", callback)

fun <O> HooksEventListener.onUpdatePlaylistSound(callback: UpdateDocumentCallback<PlaylistSound, O>) =
    on("updatePlaylistSound", callback)

fun <O> HooksEventListener.onDeletePlaylistSound(callback: DeleteDocumentCallback<PlaylistSound, O>) =
    on("deletePlaylistSound", callback)