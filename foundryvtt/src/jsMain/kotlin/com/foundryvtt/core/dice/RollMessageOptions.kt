@file:JsQualifier("foundry.dice")
package com.foundryvtt.core.dice

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface RollMessageOptions {
    val messageMode: String?
    val create: Boolean?
}