@file:JsQualifier("foundry.documents")
package com.foundryvtt.core.documents

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import com.foundryvtt.core.dice.Roll
import kotlin.js.Promise

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
