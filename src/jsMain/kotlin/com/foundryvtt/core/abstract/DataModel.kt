@file:JsQualifier("foundry.abstract")

package com.foundryvtt.core.abstract

import com.foundryvtt.core.AnyObject


abstract external class DataModel(
    data: AnyObject? = definedExternally,
    options: DocumentConstructionContext? = definedExternally
) {
    open fun toObject(source: Boolean = definedExternally): AnyObject
    open fun toJSON(): AnyObject
    open fun reset()
    open val parent: DataModel?
    val _source: AnyObject
}