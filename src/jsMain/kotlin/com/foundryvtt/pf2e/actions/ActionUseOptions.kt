package com.foundryvtt.pf2e.actions

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface ActionUseOptions : ActionVariantUseOptions {
    val variant: String?
}