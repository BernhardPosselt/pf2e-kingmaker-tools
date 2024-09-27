package com.foundryvtt.pf2e.item

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import com.foundryvtt.pf2e.system.ItemTraits
import js.objects.jso
import kotlinx.js.JsPlainObject
import kotlin.js.Promise

@JsPlainObject
external interface PF2EFeatData {
    var traits: ItemTraits
}

// required to make instance of work, but since the classes are not registered here
// at page load, we can't use @file:JsQualifier
@JsName("CONFIG.PF2E.Item.documentClasses.feat")
@Suppress("NAME_CONTAINS_ILLEGAL_CHARS")
external class PF2EFeat : PF2EItem {
    companion object : DocumentStatic<PF2EFeat>

    override fun delete(operation: DatabaseDeleteOperation): Promise<PF2EFeat>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<PF2EFeat?>

    val system: PF2EFeatData
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
fun PF2EFeat.update(data: PF2EFeat, operation: DatabaseUpdateOperation = jso()): Promise<PF2EFeat?> =
    update(data as AnyObject, operation)