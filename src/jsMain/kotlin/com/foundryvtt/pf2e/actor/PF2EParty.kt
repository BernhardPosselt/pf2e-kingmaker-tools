package com.foundryvtt.pf2e.actor

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import js.objects.unsafeJso
import kotlinx.js.JsPlainObject
import kotlin.js.Promise

@JsPlainObject
external interface PF2ETravelSpeed {
    val value: Int
}

@JsPlainObject
external interface PF2EPartySpeeds {
    val travel: PF2ETravelSpeed
}

@JsPlainObject
external interface PF2EPartyMovement {
    val speeds: PF2EPartySpeeds
}

@JsPlainObject
external interface PF2EPartyData {
    val movement: PF2EPartyMovement
}

// required to make instance of work, but since the classes are not registered here
// at page load, we can't use @file:JsQualifier
@JsName("CONFIG.PF2E.Actor.documentClasses.party")
@Suppress("NAME_CONTAINS_ILLEGAL_CHARS")
external class PF2EParty : PF2EActor {
    companion object : DocumentStatic<PF2EParty>

    override fun delete(operation: DatabaseDeleteOperation): Promise<PF2EParty>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<PF2EParty?>

    val members: Array<PF2EActor>
    val system: PF2EPartyData
    val active: Boolean
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
fun PF2EParty.update(data: PF2EParty, operation: DatabaseUpdateOperation = unsafeJso()): Promise<PF2EParty?> =
    update(data as AnyObject, operation)