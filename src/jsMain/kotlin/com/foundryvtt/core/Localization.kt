package com.foundryvtt.core

import com.foundryvtt.core.abstract.DataModel
import js.core.Void
import js.intl.ListFormat
import js.intl.ListFormatStyle
import js.intl.ListFormatType
import js.objects.JsPlainObject
import kotlin.js.Promise

@JsPlainObject
external interface GetListFormatterOptions {
    val style: ListFormatStyle?
    val type: ListFormatType?
}

@JsPlainObject
external interface LocalizeDataModelOptions {
    val prefixes: Array<String>?
    val prefixPath: String?
}

external class Localization {
    val lang: String
    val defaultModule: String
    val translations: AnyObject
    fun initialize(): Promise<Void>
    fun setLanguage(lang: String): Promise<Void>
    fun has(stringId: String, fallback: Boolean = definedExternally): Boolean
    fun localize(stringId: String): String
    fun format(stringId: String, data: AnyObject = definedExternally): String
    fun getListFormatter(options: GetListFormatterOptions = definedExternally): ListFormat
    fun <T: AnyObject> sortObjects(objects: Array<T>, key: String): Array<T>

    companion object {
        @JsStatic
        fun <T: DataModel> localizeDataModel(model: T, options: LocalizeDataModelOptions = definedExternally)
    }
}