package com.foundryvtt.core.applications.api

import js.objects.ReadonlyRecord
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface HandlebarsTemplatePart {
    val template: String
    val id: String?
    val classes: Array<String>?
    val templates: Array<String>?
    val scrollable: Array<String>?
    val forms: ReadonlyRecord<String, ApplicationFormConfiguration>?
}

@JsPlainObject
external interface HandlebarsRenderOptions {
    val parts: Array<String>
}