package com.foundryvtt.core.helpers

import com.foundryvtt.core.applications.api.ApplicationV2
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface SettingsMenuData<T : ApplicationV2> {
    val name: String
    val label: String
    val hint: String?
    val icon: String?
    val type: JsClass<out T>
    val restricted: Boolean
}