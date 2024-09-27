package com.foundryvtt.pf2e.actor

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import js.objects.ReadonlyRecord
import js.objects.jso
import kotlinx.js.JsPlainObject
import kotlin.js.Promise

@JsPlainObject
external interface PF2EFamiliarData

// required to make instance of work, but since the classes are not registered here
// at page load, we can't use @file:JsQualifier
@JsName("CONFIG.PF2E.Actor.documentClasses.familiar")
@Suppress("NAME_CONTAINS_ILLEGAL_CHARS")
external class PF2EFamiliar : PF2EActor, PF2ECreature {
    companion object : DocumentStatic<PF2EFamiliar>

    override fun delete(operation: DatabaseDeleteOperation): Promise<PF2EFamiliar>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<PF2EFamiliar?>

    override val skills: ReadonlyRecord<String, PF2EAttribute>
    val system: PF2EFamiliarData
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
fun PF2EFamiliar.update(data: PF2EFamiliar, operation: DatabaseUpdateOperation = jso()): Promise<PF2EFamiliar?> =
    update(data as AnyObject, operation)