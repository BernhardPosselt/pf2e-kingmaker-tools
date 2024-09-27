@file:JsQualifier("foundry.data.fields")

package com.foundryvtt.core.data.fields


external class ObjectField<T>(
    options: DataFieldOptions/*<T>*/ = definedExternally,
    context: DataFieldContext<T> = definedExternally,
) : DataField<T>
