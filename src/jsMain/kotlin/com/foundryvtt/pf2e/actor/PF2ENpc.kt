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
external interface PF2ENpcAttributesHp {
    var value: Int
    var max: Int
}

@JsPlainObject
external interface PF2ENpcAttributes {
    val hp: PF2ENpcAttributesHp
}

@JsPlainObject
external interface PF2ENpcData {
    val attributes: PF2ENpcAttributes
}

// required to make instance of work, but since the classes are not registered here
// at page load, we can't use @file:JsQualifier
@JsName("CONFIG.PF2E.Actor.documentClasses.npc")
@Suppress("NAME_CONTAINS_ILLEGAL_CHARS")
external class PF2ENpc : PF2EActor, PF2ECreature {
    companion object : DocumentStatic<PF2ENpc>

    override fun delete(operation: DatabaseDeleteOperation): Promise<PF2ENpc>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<PF2ENpc?>

    override val skills: ReadonlyRecord<String, PF2EAttribute>
    override val hitPoints: HitPoints
    val system: PF2ENpcData
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
fun PF2ENpc.update(data: PF2ENpc, operation: DatabaseUpdateOperation = unsafeJso()): Promise<PF2ENpc?> =
    update(data as AnyObject, operation)

@JsPlainObject
external interface PF2ENpcSheetData {
    val document: PF2ENpc
}

fun <O> HooksEventListener.onRenderPF2ENpcSheet(callback: RenderApplication<PF2ENpcSheetData, O>) =
    on("renderNPCSheetPF2e", callback)