package com.foundryvtt.core

import com.foundryvtt.core.applications.ui.Notifications
import js.objects.JsPlainObject



@JsPlainObject
external interface Ui {
    val notifications: Notifications
}