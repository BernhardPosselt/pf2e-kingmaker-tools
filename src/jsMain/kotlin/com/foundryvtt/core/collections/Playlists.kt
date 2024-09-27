package com.foundryvtt.core.collections

import com.foundryvtt.core.documents.Playlist
import kotlin.js.Promise

external class Playlists : WorldCollection<Playlist> {
    companion object : WorldCollectionStatic<Playlist>

    val playing: Boolean
    fun initialize(): Promise<Unit>
}