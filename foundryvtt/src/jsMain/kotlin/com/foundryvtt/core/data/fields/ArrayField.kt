@file:JsQualifier("foundry.data.fields")

package com.foundryvtt.core.data.fields

import com.foundryvtt.core.AnyObject

external class ArrayField<T, D>(
    element: DataField<D>,
    options: ArrayFieldOptions<T>? = definedExternally,
    context: DataFieldContext<Array<T>>? = definedExternally,
) : DataField<Array<T>> {
    fun migrateSource(sourceData: AnyObject, fieldData: Any)
}