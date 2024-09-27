package com.foundryvtt.pf2e.actions

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface SingleCheckActionUseOptions : ActionUseOptions {
    val difficultyClass: CheckDC?
    val modifiers: Array<ModifierPF2e>?
    val multipleAttackPenalty: Int?
    val notes: Array<RollNoteSource>?
    val rollOptions: Array<String>?
    val statistic: String?
}