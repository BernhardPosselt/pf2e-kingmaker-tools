@file:JsQualifier("foundry.data.operators")

package com.foundryvtt.core.data.operators

external class ForcedDeletion(
    @Suppress("LocalVariableName")
    _value: dynamic = definedExternally,
): DataFieldOperator {
    companion object {
        @JsStatic
        fun get(value: dynamic = definedExternally): dynamic

        @JsStatic
        fun set(operator: DataFieldOperator, value: dynamic): dynamic

        @JsStatic
        fun create(value: dynamic = definedExternally): ForcedDeletion

        @JsStatic
        fun equals(a: DataFieldOperator, b: DataFieldOperator): Boolean
    }
}