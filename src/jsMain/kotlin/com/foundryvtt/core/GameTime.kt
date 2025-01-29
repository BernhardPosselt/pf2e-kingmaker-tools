package com.foundryvtt.core

import io.socket.Socket
import kotlin.js.Promise

external class GameTime(socket: Socket) {
    val serverTime: Double
    val worldTime: Double
    fun advance(seconds: Double, options: AnyObject = definedExternally): Promise<Double>
    fun advance(seconds: Int, options: AnyObject = definedExternally): Promise<Double>
    fun sync(socket: Socket): Promise<GameTime>
    fun onUpdateWorldTime(worldTime: Double, options: AnyObject, userId: String)
}