@file:JsQualifier("foundry.documents")
package com.foundryvtt.core.documents

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.AudioContext
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import com.foundryvtt.core.audio.Sound
import kotlin.js.Promise

open external class PlaylistSound : ClientDocument {
    companion object : DocumentStatic<PlaylistSound>

    override fun delete(operation: DatabaseDeleteOperation): Promise<PlaylistSound>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<PlaylistSound?>

    var _id: String
    val sound: Sound?
    val fadeDuration: Int
    val context: AudioContext
    val effectiveVolume: Int
    var name: String
    var description: String
    var path: String
    var channel: String
    var playing: Boolean
    var pausedTime: Int
    var repeat: Boolean
    var volume: Int
    var fade: Int
    var sort: Int

    fun sync()
    fun load(): Promise<Unit>
}
