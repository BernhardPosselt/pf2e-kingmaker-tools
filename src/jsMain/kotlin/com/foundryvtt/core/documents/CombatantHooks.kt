package com.foundryvtt.core.documents

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.CreateDocumentCallback
import com.foundryvtt.core.DeleteDocumentCallback
import com.foundryvtt.core.HooksEventListener
import com.foundryvtt.core.PreCreateDocumentCallback
import com.foundryvtt.core.PreDeleteDocumentCallback
import com.foundryvtt.core.PreUpdateDocumentCallback
import com.foundryvtt.core.UpdateDocumentCallback
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import js.objects.jso
import kotlin.js.Promise


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