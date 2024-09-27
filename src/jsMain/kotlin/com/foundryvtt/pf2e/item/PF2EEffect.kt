package com.foundryvtt.pf2e.item

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import com.foundryvtt.pf2e.system.IntValue
import js.objects.jso
import kotlinx.js.JsPlainObject
import kotlin.js.Promise

@JsPlainObject
external interface PF2EEffectData

// required to make instance of work, but since the classes are not registered here
// at page load, we can't use @file:JsQualifier
@JsName("CONFIG.PF2E.Item.documentClasses.effect")
@Suppress("NAME_CONTAINS_ILLEGAL_CHARS")
external class PF2EEffect : PF2EItem {
    companion object : DocumentStatic<PF2EEffect>

    override fun delete(operation: DatabaseDeleteOperation): Promise<PF2EEffect>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<PF2EEffect?>

    val system: PF2EEffectData
    val isExpired: Boolean
    val badge: IntValue
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
fun PF2EEffect.update(data: PF2EEffect, operation: DatabaseUpdateOperation = jso()): Promise<PF2EEffect?> =
    update(data as AnyObject, operation)