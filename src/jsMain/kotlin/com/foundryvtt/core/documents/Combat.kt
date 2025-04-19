@file:JsQualifier("foundry.documents")
package com.foundryvtt.core.documents

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import com.foundryvtt.core.documents.collections.EmbeddedCollection
import kotlin.js.Promise

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
