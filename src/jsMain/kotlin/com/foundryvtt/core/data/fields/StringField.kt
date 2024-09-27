@file:JsQualifier("foundry.data.fields")

package com.foundryvtt.core.data.fields

external class StringField(
    options: StringFieldOptions? = definedExternally,
    context: DataFieldContext<String>? = definedExternally,
) : DataField<String> {
    var blank: Boolean
    var trim: Boolean
    var choices: Any  // Array<String> | Object | function
    var textSearch: Boolean
}