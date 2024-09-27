package com.foundryvtt.pf2e.actor

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import js.objects.jso
import kotlinx.js.JsPlainObject
import kotlin.js.Promise

@JsPlainObject
external interface PF2EVehicleData

// required to make instance of work, but since the classes are not registered here
// at page load, we can't use @file:JsQualifier
@JsName("CONFIG.PF2E.Actor.documentClasses.vehicle")
@Suppress("NAME_CONTAINS_ILLEGAL_CHARS")
external class PF2EVehicle : PF2EActor {
    companion object : DocumentStatic<PF2EVehicle>

    override fun delete(operation: DatabaseDeleteOperation): Promise<PF2EVehicle>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<PF2EVehicle?>

    val system: PF2EVehicleData
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
fun PF2EVehicle.update(data: PF2EVehicle, operation: DatabaseUpdateOperation = jso()): Promise<PF2EVehicle?> =
    update(data as AnyObject, operation)