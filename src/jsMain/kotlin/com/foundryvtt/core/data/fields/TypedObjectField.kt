@file:JsQualifier("foundry.data.fields")

package com.foundryvtt.core.data.fields

external class TypedObjectField<T, D>(
    element: DataField<D>,
    options: DataFieldOptions? = definedExternally,
    context: DataFieldContext<T>? = definedExternally,
) : DataField<T> {
}