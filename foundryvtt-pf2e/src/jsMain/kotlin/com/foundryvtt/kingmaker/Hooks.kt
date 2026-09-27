package com.foundryvtt.kingmaker

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.helpers.HooksEventListener
import org.w3c.dom.HTMLElement

typealias KingmakerHexEditApp = Any

fun <O> HooksEventListener.onCloseKingmakerHexEdit(callback: (KingmakerHexEditApp, HTMLElement) -> O) =
    on("closeKingmakerHexEdit", callback)

fun <O> HooksEventListener.onRenderKingmakerHexHud(callback: (app: KingmakerHexHud, html: HTMLElement, messageData: AnyObject) -> O) =
    on("renderKingmakerHexHUD", callback)