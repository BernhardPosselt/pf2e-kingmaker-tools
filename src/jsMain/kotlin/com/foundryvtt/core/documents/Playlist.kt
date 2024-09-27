package com.foundryvtt.core.documents

import com.foundryvtt.core.*
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import com.foundryvtt.core.collections.EmbeddedCollection
import js.objects.jso
import kotlinx.js.JsPlainObject
import kotlin.js.Promise

@JsPlainObject
external interface PlayNextOptions {
    val direction: Int?
}

// required to make instance of work, but since the classes are not registered here
// at page load, we can't use @file:JsQualifier
@JsName("CONFIG.Playlist.documentClass")
@Suppress("NAME_CONTAINS_ILLEGAL_CHARS")
external class Playlist : ClientDocument {
    companion object : DocumentStatic<Playlist>

    override fun delete(operation: DatabaseDeleteOperation): Promise<Playlist>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<Playlist?>

    var _id: String
    var name: String
    var description: String
    var sounds: EmbeddedCollection<PlaylistSound>
    var channel: String
    var mode: Int
    var playing: Boolean
    var fade: Int
    var folder: Folder
    var sorting: String
    var seed: Int
    var sort: Int
    val playbackOrder: Array<String>
    fun playAll(): Promise<Playlist>
    fun playNext(soundId: String, options: PlayNextOptions = definedExternally)
    fun playSound(sound: PlaylistSound): Promise<Playlist>
    fun stopSound(sound: PlaylistSound): Promise<Playlist>
    fun stopAll(): Promise<Playlist>
    fun cycleMode(): Promise<Playlist>
}

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