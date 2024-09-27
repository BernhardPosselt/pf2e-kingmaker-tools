package com.foundryvtt.core

import io.socket.Socket
import kotlin.js.Promise

external class GameTime(socket: Socket) {
    val serverTime: Int
    val worldTime: Int
    fun advance(seconds: Int, options: AnyObject = definedExternally): Promise<Int>
    fun sync(socket: Socket): Promise<GameTime>
    fun onUpdateWorldTime(worldTime: Int, options: AnyObject, userId: String)
}