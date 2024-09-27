@file:JsQualifier("foundry.utils")

package com.foundryvtt.core.utils

import com.foundryvtt.core.AnyObject
import js.objects.ReadonlyRecord

external fun expandObject(value: AnyObject): AnyObject
external fun <T> deepClone(value: T): T
external fun mergeObject(original: AnyObject, other: AnyObject = definedExternally): AnyObject
external fun flattenObject(original: Any): ReadonlyRecord<String, Any>
external fun diffObject(
    original: AnyObject,
    other: AnyObject,
    options: DiffObjectOptions = definedExternally
): AnyObject

external fun getProperty(`object`: AnyObject, key: String): Any?
external fun setProperty(`object`: AnyObject, key: String, value: Any): Boolean
