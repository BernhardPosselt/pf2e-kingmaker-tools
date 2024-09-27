package com.foundryvtt.pf2e.actor

import com.foundryvtt.core.documents.ChatMessage
import com.foundryvtt.core.documents.TokenDocument
import com.foundryvtt.pf2e.actions.CheckDC
import com.foundryvtt.pf2e.actions.CheckRoll
import com.foundryvtt.pf2e.actions.ModifierPF2e
import com.foundryvtt.pf2e.actions.RollNoteSource
import com.foundryvtt.pf2e.item.PF2EItem
import kotlinx.html.org.w3c.dom.events.Event
import kotlinx.js.JsPlainObject
import kotlin.js.Promise

@JsPlainObject
external interface StatisticRollParameters {
    val identifier: String?
    val action: String?
    val token: TokenDocument?
    val attackNumber: Int?
    val target: PF2EActor?
    val origin: PF2EActor?
    val dc: CheckDC?
    val label: String?
    val slug: String?
    val title: String?
    val extraRollNotes: Array<RollNoteSource>?
    val extraRollOptions: Array<String>?
    val modifiers: Array<ModifierPF2e>?
    val item: PF2EItem?
    val rollMode: String?
    val skipDialog: Boolean?
    val rollTwice: Any? // "keep-higher" or "keep-lower" or false
    val traits: Array<String>?
    val damaging: Boolean?
    val melee: Boolean?
    val createMessage: Boolean?
    val callback: ((roll: CheckRoll, outcome: String?, message: ChatMessage, event: Event?) -> Promise<Unit>)?
}