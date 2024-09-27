package com.foundryvtt.pf2e.actor

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import js.objects.jso
import kotlinx.js.JsPlainObject
import kotlin.js.Promise

@JsPlainObject
external interface PF2EPartySpeed {
    val total: Int
}

@JsPlainObject
external interface PF2EPartyAttributes {
    val speed: PF2EPartySpeed
}

@JsPlainObject
external interface PF2EPartyData {
    val attributes: PF2EPartyAttributes
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
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
fun PF2EParty.update(data: PF2EParty, operation: DatabaseUpdateOperation = jso()): Promise<PF2EParty?> =
    update(data as AnyObject, operation)