package com.foundryvtt.kingmaker

import com.foundryvtt.core.HooksEventListener
import org.w3c.dom.HTMLElement

typealias KingmakerHexEditApp = Any

fun <O> HooksEventListener.onCloseKingmakerHexEdit(callback: (KingmakerHexEditApp, HTMLElement) -> O) =
    on("closeKingmakerHexEdit", callback)