package com.foundryvtt.pf2e

import com.foundryvtt.core.Game
import com.foundryvtt.pf2e.actions.PF2EActionMacros
import com.foundryvtt.pf2e.time.PF2EWorldClock
import kotlinx.js.JsPlainObject


@JsPlainObject
external interface PF2EGame {
    val actions: PF2EActionMacros
    val worldClock: PF2EWorldClock
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
inline val Game.pf2e: PF2EGame
    get() = asDynamic().pf2e as PF2EGame