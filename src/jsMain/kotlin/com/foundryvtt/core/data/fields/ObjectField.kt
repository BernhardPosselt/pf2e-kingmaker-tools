@file:JsQualifier("foundry.data.fields")

package com.foundryvtt.core.data.fields

import com.foundryvtt.core.AnyObject


external class ObjectField(
    options: DataFieldOptions/*<AnyObject>*/ = definedExternally,
    context: DataFieldContext<AnyObject>? = definedExternally,
) : DataField<AnyObject>
