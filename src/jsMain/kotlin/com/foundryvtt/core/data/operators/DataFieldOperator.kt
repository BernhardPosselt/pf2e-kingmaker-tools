@file:JsQualifier("foundry.data.operators")

package com.foundryvtt.core.data.operators

import com.foundryvtt.core.AnyObject

abstract external class DataFieldOperator {
    companion object {
        @JsStatic
        fun get(value: dynamic = definedExternally): dynamic

        @JsStatic
        fun set(operator: DataFieldOperator, value: dynamic): dynamic

        @JsStatic
        fun create(value: dynamic = definedExternally): DataFieldOperator

        @JsStatic
        fun equals(a: DataFieldOperator, b: DataFieldOperator): Boolean
    }

    fun toJSON(): AnyObject
}