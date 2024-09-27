@file:JsQualifier("foundry.data.fields")

package com.foundryvtt.core.data.fields

external class NumberField<T : Number>(
    options: NumberFieldOptions? = definedExternally,
    context: DataFieldContext<T>? = definedExternally,
) : DataField<T>