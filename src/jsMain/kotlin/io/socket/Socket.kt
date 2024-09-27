package io.socket

external class Socket {
    fun emit(event: String, data: Any)
    fun on(event: String, callback: (Any) -> Unit)
}