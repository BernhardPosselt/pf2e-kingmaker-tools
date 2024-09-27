package com.foundryvtt.core.documents

import com.foundryvtt.core.*
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import com.foundryvtt.core.collections.EmbeddedCollection
import js.objects.jso
import kotlinx.js.JsPlainObject
import kotlin.js.Promise

@JsPlainObject
external interface RollInitiativeOptions {
    val formula: String?
    val updateTurn: Boolean?
    val messageOptions: AnyObject?
}

@JsPlainObject
external interface CombatHistoryData {
    val round: Int?
    val turn: Int?
    val tokenId: String?
    val combatantId: String?
}

// required to make instance of work, but since the classes are not registered here
// at page load, we can't use @file:JsQualifier
@JsName("CONFIG.Combat.documentClass")
@Suppress("NAME_CONTAINS_ILLEGAL_CHARS")
external class Combat : ClientDocument {
    companion object : DocumentStatic<Combat> {
        val CONFIG_SETTING: String
    }

    override fun delete(operation: DatabaseDeleteOperation): Promise<Combat>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<Combat?>

    var _id: String
    var type: String
    var scene: Scene
    var combatants: EmbeddedCollection<Combatant>
    var active: Boolean
    var round: Int
    var turn: Int
    var sort: Int

    val turns: Array<Combatant>
    val current: CombatHistoryData
    val previous: CombatHistoryData?
    val combatant: Combatant
    val nextCombatant: Combatant
    val settings: AnyObject
    val started: Boolean
    val isActive: Boolean

    fun activate(options: DatabaseUpdateOperation = definedExternally): Promise<Combatant>
    fun getCombatantsByToken(token: String): Array<Combatant>
    fun getCombatantsByToken(token: TokenDocument): Array<Combatant>
    fun getCombatantsByActor(actor: String): Array<Combatant>
    fun getCombatantsByActor(actor: Actor): Array<Combatant>
    fun startCombat(): Promise<Combat>
    fun nextRound(): Promise<Combat>
    fun previousRound(): Promise<Combat>
    fun nextTurn(): Promise<Combat>
    fun previousTurn(): Promise<Combat>
    fun endCombat(): Promise<Combat>
    fun toggleSceneLink(): Promise<Combat>
    fun resetAll(): Promise<Combat>
    fun rollInitiative(ids: String, options: RollInitiativeOptions = definedExternally): Promise<Combat>
    fun rollAll(options: AnyObject = definedExternally): Promise<Combat>
    fun rollNPC(options: AnyObject = definedExternally): Promise<Combat>
    fun setInitiative(id: String, value: Int): Promise<Unit>
    fun setupTurns(): Array<Combatant>
    fun updateCombatantActors()
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
fun Combat.update(data: Combat, operation: DatabaseUpdateOperation = jso()): Promise<Combat?> =
    update(data as AnyObject, operation)

fun <O> HooksEventListener.onPreCreateCombat(callback: PreCreateDocumentCallback<Combat, O>) =
    on("preCreateCombat", callback)

fun <O> HooksEventListener.onPreUpdateCombat(callback: PreUpdateDocumentCallback<Combat, O>): Unit =
    on("preUpdateCombat", callback)

fun <O> HooksEventListener.onPreDeleteCombat(callback: PreDeleteDocumentCallback<Combat, O>) =
    on("preDeleteCombat", callback)

fun <O> HooksEventListener.onCreateCombat(callback: CreateDocumentCallback<Combat, O>) =
    on("createCombat", callback)

fun <O> HooksEventListener.onUpdateCombat(callback: UpdateDocumentCallback<Combat, O>) =
    on("updateCombat", callback)

fun <O> HooksEventListener.onDeleteCombat(callback: DeleteDocumentCallback<Combat, O>) =
    on("deleteCombat", callback)