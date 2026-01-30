package com.foundryvtt.core

import com.foundryvtt.core.applications.ui.Notifications
import kotlinx.js.JsPlainObject



@JsPlainObject
external interface Ui {
    val notifications: Notifications
}