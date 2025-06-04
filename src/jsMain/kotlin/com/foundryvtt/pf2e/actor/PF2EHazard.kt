package com.foundryvtt.pf2e.actor

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import com.foundryvtt.core.helpers.HooksEventListener
import com.foundryvtt.core.helpers.RenderApplication
import js.objects.ReadonlyRecord
import js.objects.unsafeJso
import kotlinx.js.JsPlainObject
import kotlin.js.Promise

@JsPlainObject
external interface PF2EHazardData

// required to make instance of work, but since the classes are not registered here
// at page load, we can't use @file:JsQualifier
@JsName("CONFIG.PF2E.Actor.documentClasses.hazard")
@Suppress("NAME_CONTAINS_ILLEGAL_CHARS")
external class PF2EHazard : PF2EActor, PF2ECreature {
    companion object : DocumentStatic<PF2EHazard>

    override fun delete(operation: DatabaseDeleteOperation): Promise<PF2EHazard>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<PF2EHazard?>

    override val skills: ReadonlyRecord<String, PF2EAttribute>
    val system: PF2EHazardData
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
fun PF2EHazard.update(data: PF2EHazard, operation: DatabaseUpdateOperation = unsafeJso()): Promise<PF2EHazard?> =
    update(data as AnyObject, operation)

@JsPlainObject
external interface PF2EHazardSheetData {
    val document: PF2EHazard
}

fun <O> HooksEventListener.onRenderPF2EHazardSheet(callback: RenderApplication<PF2EHazardSheetData, O>) =
    on("renderHazardSheetPF2e", callback)