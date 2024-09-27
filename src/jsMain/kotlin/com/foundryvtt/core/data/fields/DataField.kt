@file:JsQualifier("foundry.data.fields")

package com.foundryvtt.core.data.fields

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.FormInputConfig
import com.foundryvtt.core.abstract.DataModel


abstract external class DataField<T>(
    options: DataFieldOptions/*<T>*/ = definedExternally,
    context: DataFieldContext<T> = definedExternally
) {
    @OptIn(ExperimentalStdlibApi::class)
    @JsExternalInheritorsOnly
    open class DataFieldStatic<T> {
        var hierarchical: Boolean
        var recursive: Boolean
        var _defaults: DataFieldOptions/*<T>*/
        val hasFormSupport: Boolean
    }

    companion object : DataFieldStatic<Any>

    var options: DataFieldOptions/*<T>*/

    val fieldPath: String
    fun <O> apply(fn: Function<O>, value: T, options: AnyObject = definedExternally): O
    fun <O> apply(fn: String, value: T, options: AnyObject = definedExternally): O
    fun clean(value: Any, options: CleanOptions = definedExternally): T
    fun getInitialValue(data: Any): T
    fun validate(value: Any, options: DataFieldValidationOptions = definedExternally): DataModelValidationFailure
    fun initialize(value: Any, model: AnyObject, options: AnyObject = definedExternally): Any
    fun toObject(value: Any): Any
    fun toInput(config: FormInputConfig<T>): Any // HTMLElement or HTMLCollection
    fun toFormGroup(groupConfig: FormGroupConfig<T> = definedExternally, inputConfig: FormInputConfig<T>)
    fun applyChange(value: Any, model: DataModel, change: EffectChangeData): Any
}