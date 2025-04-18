package com.foundryvtt.core

import com.foundryvtt.core.applications.ui.Notifications
import js.objects.JsPlainObject

external val ui: Ui

@JsPlainObject
external interface Ui {
    val notifications: Notifications
}