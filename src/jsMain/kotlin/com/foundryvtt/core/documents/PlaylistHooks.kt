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
fun Playlist.update(data: Playlist, operation: DatabaseUpdateOperation = jso()): Promise<Playlist?> =
    update(data as AnyObject, operation)


fun <O> HooksEventListener.onPreCreatePlaylist(callback: PreCreateDocumentCallback<Playlist, O>) =
    on("preCreatePlaylist", callback)

fun <O> HooksEventListener.onPreUpdatePlaylist(callback: PreUpdateDocumentCallback<Playlist, O>): Unit =
    on("preUpdatePlaylist", callback)

fun <O> HooksEventListener.onPreDeletePlaylist(callback: PreDeleteDocumentCallback<Playlist, O>) =
    on("preDeletePlaylist", callback)

fun <O> HooksEventListener.onCreatePlaylist(callback: CreateDocumentCallback<Playlist, O>) =
    on("createPlaylist", callback)

fun <O> HooksEventListener.onUpdatePlaylist(callback: UpdateDocumentCallback<Playlist, O>) =
    on("updatePlaylist", callback)

fun <O> HooksEventListener.onDeletePlaylist(callback: DeleteDocumentCallback<Playlist, O>) =
    on("deletePlaylist", callback)