package com.foundryvtt.pf2e.item

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import com.foundryvtt.pf2e.system.ItemTraits
import com.foundryvtt.pf2e.system.MaxValue
import js.objects.jso
import kotlinx.js.JsPlainObject
import kotlin.js.Promise

@JsPlainObject
external interface PF2EConsumableData : PhysicalItemData {
    var traits: ItemTraits
    var uses: MaxValue
}

// required to make instance of work, but since the classes are not registered here
// at page load, we can't use @file:JsQualifier
@JsName("CONFIG.PF2E.Item.documentClasses.consumable")
@Suppress("NAME_CONTAINS_ILLEGAL_CHARS")
external class PF2EConsumable : PF2EItem {
    companion object : DocumentStatic<PF2EConsumable>

    override fun delete(operation: DatabaseDeleteOperation): Promise<PF2EConsumable>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<PF2EConsumable?>

    val system: PF2EConsumableData
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
fun PF2EConsumable.update(data: PF2EConsumable, operation: DatabaseUpdateOperation = jso()): Promise<PF2EConsumable?> =
    update(data as AnyObject, operation)