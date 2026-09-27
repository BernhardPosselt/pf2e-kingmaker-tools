package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface ActivityData {
    val sceneId: String?
    val cursor: CursorData
    val ruler: dynamic
    val targets: Array<String>
    val active: Boolean
    val ping: PingData
    val av: AVSettingsData
}