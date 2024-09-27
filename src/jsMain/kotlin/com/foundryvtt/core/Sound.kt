package com.foundryvtt.core

import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLAudioElement
import kotlin.js.Promise

@JsPlainObject
external interface SoundOptions {
    val context: AudioContext?
    val forceBuffer: Boolean?
}

@JsPlainObject
external interface AutoplayOptions {
    val autoplay: Boolean?
}

@JsPlainObject
external interface FadeOptions {
    val duration: Int?
    val from: Int?
    val type: String?
}


external class Sound(
    src: String,
    soundOptions: SoundOptions = definedExternally
) {
    val id: Int
    val src: String
    val context: AudioContext
    val element: HTMLAudioElement?
    val loaded: Boolean
    val failed: Boolean
    val playing: Boolean
    val isBuffering: Boolean
    var volume: Double
    val startTime: Double
    val pausedTime: Double
    val duration: Double
    val currentTime: Double
    var loop: Boolean

    fun load(options: AutoplayOptions = definedExternally): Promise<Sound>
    fun play(): Promise<Sound>
    fun pause()
    fun stop(): Promise<Sound>
    fun fade(volume: Double, options: FadeOptions = definedExternally): Promise<Unit>
    fun wait(duration: Double): Promise<Sound>
    fun <T> schedule(callback: (Sound) -> Promise<T>, playbackTime: Double): Promise<T>
}