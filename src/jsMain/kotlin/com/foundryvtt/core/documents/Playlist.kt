@file:JsQualifier("foundry.documents")
package com.foundryvtt.core.documents

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import com.foundryvtt.core.documents.collections.EmbeddedCollection
import kotlinx.js.JsPlainObject
import kotlin.js.Promise

@JsPlainObject
external interface PlayNextOptions {
    val direction: Int?
}

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
