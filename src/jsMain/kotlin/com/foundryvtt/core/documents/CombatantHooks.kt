package com.foundryvtt.core.documents

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import com.foundryvtt.core.helpers.CreateDocumentCallback
import com.foundryvtt.core.helpers.DeleteDocumentCallback
import com.foundryvtt.core.helpers.HooksEventListener
import com.foundryvtt.core.helpers.PreCreateDocumentCallback
import com.foundryvtt.core.helpers.PreDeleteDocumentCallback
import com.foundryvtt.core.helpers.PreUpdateDocumentCallback
import com.foundryvtt.core.helpers.UpdateDocumentCallback
import js.objects.unsafeJso
import kotlin.js.Promise


@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
fun Combatant.update(data: Combatant, operation: DatabaseUpdateOperation = unsafeJso()): Promise<Combatant?> =
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