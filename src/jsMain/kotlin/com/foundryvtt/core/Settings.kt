package com.foundryvtt.core

import com.foundryvtt.core.applications.api.ApplicationV2
import com.foundryvtt.core.data.fields.DataField
import js.objects.ReadonlyRecord
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import kotlin.js.Promise

typealias CustomFormInput<T> = (field: DataField<T>, config: FormInputConfig<T>) -> HTMLElement

@JsPlainObject
external interface SettingsRange {
    val max: Int
    val min: Int
    val step: Int
}

@JsPlainObject
external interface FormInputConfig<T> {
    val name: String
    val value: T
    val required: Boolean?
    val disabled: Boolean?
    val readonly: Boolean?
    val localize: Boolean?
    val dataset: ReadonlyRecord<String, String>?
    val placeholder: String?
    val input: CustomFormInput<T>?
}

@JsPlainObject
external interface SettingsData<T> {
    val name: String?
    val hint: String?
    val scope: String // world or client
    val config: Boolean?
    val default: T?
    val requiresReload: Boolean?
    val type: Any  // Number, Object, Array, Boolean class or data field or data model
    val choices: ReadonlyRecord<String, T>?
    val onChange: ((value: T) -> Unit)?
    val range: SettingsRange?
    val input: CustomFormInput<T>?
}

@JsPlainObject
external interface SettingsMenuData<T : ApplicationV2> {
    val name: String
    val label: String
    val hint: String?
    val icon: String?
    val type: JsClass<out T>
    val restricted: Boolean
}

external class Settings {
    fun <T> register(namespace: String, key: String, data: SettingsData<T>)
    fun <T : ApplicationV2> registerMenu(namespace: String, key: String, data: SettingsMenuData<T>)
    fun <T> set(namespace: String, key: String, data: T): Promise<T>
    fun <T> get(namespace: String, key: String): T
}