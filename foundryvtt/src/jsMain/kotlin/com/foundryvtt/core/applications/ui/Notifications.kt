package com.foundryvtt.core.applications.ui

external class Notifications {
    fun error(message: String)
    fun info(message: String)
    fun warn(message: String)
}