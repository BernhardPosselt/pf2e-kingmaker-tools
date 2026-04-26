@file:JsQualifier("foundry.data.operators")

package com.foundryvtt.core.data.operators

external class ForcedReplacement(
    value: dynamic = definedExternally,
): DataFieldOperator {
    companion object {
        @JsStatic
        fun get(value: dynamic = definedExternally): dynamic

        @JsStatic
        fun set(operator: DataFieldOperator, value: dynamic): dynamic

        @JsStatic
        fun create(value: dynamic = definedExternally): ForcedReplacement

        @JsStatic
        fun equals(a: DataFieldOperator, b: DataFieldOperator): Boolean
    }
}