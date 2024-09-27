package com.foundryvtt.pf2e.item

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import js.objects.jso
import kotlinx.js.JsPlainObject
import kotlin.js.Promise


@JsPlainObject
external interface PF2EConditionValue {
    var isValued: Boolean
    var value: Int?
}

@JsPlainObject
external interface PF2EConditionData {
    var value: PF2EConditionValue
}

// required to make instance of work, but since the classes are not registered here
// at page load, we can't use @file:JsQualifier
@JsName("CONFIG.PF2E.Item.documentClasses.condition")
@Suppress("NAME_CONTAINS_ILLEGAL_CHARS")
external class PF2ECondition : PF2EItem {
    companion object : DocumentStatic<PF2ECondition>

    override fun delete(operation: DatabaseDeleteOperation): Promise<PF2ECondition>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<PF2ECondition?>

    val system: PF2EConditionData
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
fun PF2ECondition.update(data: PF2ECondition, operation: DatabaseUpdateOperation = jso()): Promise<PF2ECondition?> =
    update(data as AnyObject, operation)