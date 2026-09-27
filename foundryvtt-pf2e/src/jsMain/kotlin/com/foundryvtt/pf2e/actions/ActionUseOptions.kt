package com.foundryvtt.pf2e.actions

import kotlinx.js.JsPlainObject

@JsPlainObject
@Suppress("unused")
external interface ActionUseOptions : ActionVariantUseOptions {
    val variant: String?
}