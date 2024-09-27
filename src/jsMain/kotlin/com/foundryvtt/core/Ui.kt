package com.foundryvtt.core

external val ui: Ui

external class Notifications {
    fun error(message: String)
    fun info(message: String)
    fun warn(message: String)
}

external object Ui {
    val notifications: Notifications
}