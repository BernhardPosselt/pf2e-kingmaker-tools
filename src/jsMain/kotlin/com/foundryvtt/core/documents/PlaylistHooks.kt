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