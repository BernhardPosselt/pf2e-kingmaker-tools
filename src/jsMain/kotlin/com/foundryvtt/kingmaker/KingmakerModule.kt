package com.foundryvtt.kingmaker

import com.foundryvtt.core.packages.Module
import com.foundryvtt.core.utils.Collection
import js.objects.ReadonlyRecord
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface HexFeature {
    val type: String?
}

@JsPlainObject
external interface HexState {
    val commodity: String?
    val camp: String?
    val features: Array<HexFeature>?
    val claimed: Boolean?
}


@JsPlainObject
external interface KingmakerState {
    val hexes: ReadonlyRecord<String, HexState>
}

@JsPlainObject
external interface KingmakerRegion {
    val hexes: Collection<KingmakerHex>
}

external class KingmakerModule : Module {
    val state: KingmakerState
    val region: KingmakerRegion
}

external val kingmaker: KingmakerModule