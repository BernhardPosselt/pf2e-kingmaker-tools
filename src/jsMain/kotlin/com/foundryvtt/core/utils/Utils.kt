@file:JsQualifier("foundry.utils")

package com.foundryvtt.core.utils

import com.foundryvtt.core.AnyObject
import js.objects.ReadonlyRecord
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface MergeOptions {
    val insertKeys: Boolean?
    val insertValues: Boolean?
    val overwrite: Boolean?
    val recursive: Boolean?
    val inplace: Boolean?
    val enforceTypes: Boolean?
    val performDeletions: Boolean?
}

external fun expandObject(value: AnyObject): AnyObject
external fun <T> deepClone(value: T): T
external fun mergeObject(original: AnyObject, other: AnyObject = definedExternally, options: MergeOptions = definedExternally): AnyObject
external fun flattenObject(original: Any): ReadonlyRecord<String, Any>
external fun diffObject(
    original: AnyObject,
    other: AnyObject,
    options: DiffObjectOptions = definedExternally
): AnyObject
external fun isEmpty(value: Any?): Boolean
external fun objectsEqual(a: AnyObject, b: AnyObject): Boolean
external fun getProperty(`object`: AnyObject, key: String): Any?
external fun hasProperty(`object`: AnyObject, key: String): Boolean
external fun setProperty(`object`: AnyObject, key: String, value: Any): Boolean
