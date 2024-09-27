package com.foundryvtt.core.documents

import com.foundryvtt.core.*
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import js.objects.jso
import kotlin.js.Promise

// required to make instance of work, but since the classes are not registered here
// at page load, we can't use @file:JsQualifier
@JsName("CONFIG.Combatant.documentClass")
@Suppress("NAME_CONTAINS_ILLEGAL_CHARS")
external class Combatant : ClientDocument {
    companion object : DocumentStatic<Combatant>;

    override fun delete(operation: DatabaseDeleteOperation): Promise<Combatant>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<Combatant?>

    var _id: String
    var type: String
    var actorId: String
    var tokenId: String
    var sceneId: String
    var name: String
    var img: String
    var initiative: Int
    var hidden: Boolean
    var defeated: Boolean

    var resource: AnyObject?
    val combat: Combat?
    val isNPC: Boolean
    val actor: Actor?
    val token: TokenDocument?
    val players: Array<User>
    val isDefeated: Boolean
    fun getInitiativeRoll(formula: String): Roll
    fun rollInitiative(formula: String): Promise<Combatant>
    fun updateResource(): AnyObject?
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
fun Combatant.update(data: Combatant, operation: DatabaseUpdateOperation = jso()): Promise<Combatant?> =
    update(data as AnyObject, operation)

fun <O> HooksEventListener.onPreCreateCombatant(callback: PreCreateDocumentCallback<Combatant, O>) =
    on("preCreateCombatant", callback)

fun <O> HooksEventListener.onPreUpdateCombatant(callback: PreUpdateDocumentCallback<Combatant, O>): Unit =
    on("preUpdateCombatant", callback)

fun <O> HooksEventListener.onPreDeleteCombatant(callback: PreDeleteDocumentCallback<Combatant, O>) =
    on("preDeleteCombatant", callback)

fun <O> HooksEventListener.onCreateCombatant(callback: CreateDocumentCallback<Combatant, O>) =
    on("createCombatant", callback)

fun <O> HooksEventListener.onUpdateCombatant(callback: UpdateDocumentCallback<Combatant, O>) =
    on("updateCombatant", callback)

fun <O> HooksEventListener.onDeleteCombatant(callback: DeleteDocumentCallback<Combatant, O>) =
    on("deleteCombatant", callback)