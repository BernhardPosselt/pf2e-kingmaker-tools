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