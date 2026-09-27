@file:JsQualifier("foundry.helpers")
package com.foundryvtt.core.helpers

import com.foundryvtt.core.applications.api.ApplicationV2
import kotlin.js.Promise


external class ClientSettings {
    fun <T> register(namespace: String, key: String, data: SettingsData<T>)
    fun <T : ApplicationV2> registerMenu(namespace: String, key: String, data: SettingsMenuData<T>)
    fun <T> set(namespace: String, key: String, data: T): Promise<T>
    fun <T> get(namespace: String, key: String): T
}