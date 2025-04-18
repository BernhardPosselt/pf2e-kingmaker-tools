@file:JsQualifier("foundry.helpers")
package com.foundryvtt.core.helpers


external class Hooks {
    companion object {
        @JsStatic
        fun <T> on(key: String, callback: Function<T>)
        @JsStatic
        fun <T> once(key: String, callback: Function<T>)
        @JsStatic
        fun <T> off(key: String, callback: Function<T>)
        @JsStatic
        fun callAll(key: String, args: Array<Any>)
        @JsStatic
        fun call(key: String, args: Array<Any>)
        @JsStatic
        fun onError(location: String, error: Throwable, options: OnErrorOptions = definedExternally)
    }
}

