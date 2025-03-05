package com.foundryvtt.kingmaker

import com.foundryvtt.core.Module
import js.objects.ReadonlyRecord
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface HexState {
    val commodity: String?
    val camp: String?
    val features: Array<String>?
    val claimed: Boolean?
}


@JsPlainObject
external interface KingmakerState {
    val hexes: ReadonlyRecord<String, HexState>
}

external class KingmakerModule : Module {
    val state: KingmakerState
}

external val kingmaker: KingmakerModule