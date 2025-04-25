package com.foundryvtt.core.data.fields

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.abstract.DocumentConstructionContext
import org.w3c.dom.HTMLElement
import kotlin.js.Promise

open external class TypeDataModel(
    data: AnyObject? = definedExternally,
    options: DocumentConstructionContext? = definedExternally
): DataModel {
    open fun prepareBaseData()
    open fun prepareDerivedData()
    fun toEmbed(): Promise<HTMLElement?>
    // TODO
}