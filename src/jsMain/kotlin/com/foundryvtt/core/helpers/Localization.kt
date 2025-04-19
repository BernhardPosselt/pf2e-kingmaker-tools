@file:JsQualifier("foundry.helpers")
package com.foundryvtt.core.helpers

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DataModel
import js.core.Void
import js.intl.ListFormat
import kotlin.js.Promise

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