package com.i18next

import com.foundryvtt.core.AnyObject
import js.objects.JsPlainObject
import kotlin.js.Promise

@JsPlainObject
external interface I18NextInterpolationOptions {
    val escapeValue: Boolean?
    val prefix: String?
    val suffix: String?
    val escape: ((value: String) -> String)?
    val useRawValueToEscape: Boolean?
    // TODO
}

@JsPlainObject
external interface I18NextBackendOptions {
    val loadPath: String
    // TODO
}

@JsPlainObject
external interface I18NextInitOptions {
    val fallbackLng: String
    val load: String
    val lng: String
    val debug: Boolean
    val resources: AnyObject?
    val backend: I18NextBackendOptions
    val interpolation: I18NextInterpolationOptions
}

external interface I18Next {
    fun init(options: I18NextInitOptions): Promise<Unit>
    fun <T : Any> use(plugin: JsClass<T>): I18Next
    fun t(key: String, data: AnyObject): String
    fun t(key: String, default: String): String
    fun t(key: Array<String>): String
    fun t(key: String): String
    fun exists(key: String): Boolean
}

@JsNonModule
@JsModule("i18next")
external val i18next: I18Next