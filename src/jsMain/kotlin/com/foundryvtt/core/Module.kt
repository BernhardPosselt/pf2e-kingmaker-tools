package com.foundryvtt.core

import kotlin.js.Promise

open external class Module {
    val active: Boolean
    fun updateSource(data: Any): Promise<Unit>
}