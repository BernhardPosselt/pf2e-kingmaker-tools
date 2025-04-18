@file:JsQualifier("foundry.documents.collections")
package com.foundryvtt.core.documents.collections

import com.foundryvtt.core.documents.Playlist
import com.foundryvtt.core.documents.abstract.WorldCollection
import kotlin.js.Promise

external class Playlists : WorldCollection<Playlist> {
    companion object : WorldCollectionStatic<Playlist>

    val playing: Boolean
    fun initialize(): Promise<Unit>
}