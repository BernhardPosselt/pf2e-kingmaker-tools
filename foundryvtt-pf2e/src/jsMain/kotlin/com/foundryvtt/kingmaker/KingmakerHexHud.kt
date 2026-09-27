package com.foundryvtt.kingmaker

import kotlin.js.Promise

external class KingmakerHexHud {
    val hex: KingmakerHex
    val enabled: Boolean

    fun toggle(enabled: Boolean? = definedExternally)
    fun activate(hex: KingmakerHex): Promise<Any>
    fun clear()
}