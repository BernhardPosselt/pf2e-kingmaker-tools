package com.foundryvtt.pf2e.actor

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import com.foundryvtt.pf2e.system.IntValue
import js.objects.unsafeJso
import kotlinx.js.JsPlainObject
import kotlin.js.Promise

@JsPlainObject
external interface PF2EArmyTraits {
    val type: String // 'skirmisher' | 'cavalry' | 'siege' | 'infantry'
    val rarity: String // 'common' | 'uncommon' | 'rare' | 'unique'
}

@JsPlainObject
external interface PF2EArmyDetails {
    var level: IntValue
    var alliance: String // party or ...
}

@JsPlainObject
external interface PF2EArmyData {
    val recruitmentDC: Int
    val consumption: Int
    val scouting: Int
    val traits: PF2EArmyTraits
    val details: PF2EArmyDetails
}

// required to make instance of work, but since the classes are not registered here
// at page load, we can't use @file:JsQualifier
@JsName("CONFIG.PF2E.Actor.documentClasses.army")
@Suppress("NAME_CONTAINS_ILLEGAL_CHARS")
external class PF2EArmy : PF2EActor {
    companion object : DocumentStatic<PF2EArmy>

    override fun delete(operation: DatabaseDeleteOperation): Promise<PF2EArmy>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<PF2EArmy?>

    val system: PF2EArmyData
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
fun PF2EArmy.update(data: PF2EArmy, operation: DatabaseUpdateOperation = unsafeJso()): Promise<PF2EArmy?> =
    update(data as AnyObject, operation)