package com.foundryvtt.pf2e.item

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import com.foundryvtt.pf2e.system.ItemTraits
import js.objects.jso
import kotlinx.js.JsPlainObject
import kotlin.js.Promise

@JsPlainObject
external interface PF2EEquipmentData : PhysicalItemData {
    var traits: ItemTraits
}

// required to make instance of work, but since the classes are not registered here
// at page load, we can't use @file:JsQualifier
@JsName("CONFIG.PF2E.Item.documentClasses.equipment")
@Suppress("NAME_CONTAINS_ILLEGAL_CHARS")
external class PF2EEquipment : PF2EItem {
    companion object : DocumentStatic<PF2EEquipment>

    override fun delete(operation: DatabaseDeleteOperation): Promise<PF2EEquipment>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<PF2EEquipment?>

    val system: PF2EEquipmentData
    val isInvested: Boolean
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
fun PF2EEquipment.update(data: PF2EEquipment, operation: DatabaseUpdateOperation = jso()): Promise<PF2EEquipment?> =
    update(data as AnyObject, operation)