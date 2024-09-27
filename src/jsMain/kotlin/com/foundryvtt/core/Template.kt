package com.foundryvtt.core

import js.objects.ReadonlyRecord
import kotlinx.js.JsPlainObject
import kotlin.js.Promise

external fun renderTemplate(path: String, data: ReadonlyRecord<String, Any?>): Promise<String>

@JsPlainObject
external interface HandlebarOptions {
    // if needed, check handlebars RuntimeOptions
}

typealias HandlebarsTemplateDelegate = (AnyObject, HandlebarOptions) -> Promise<String>

external fun loadTemplates(paths: Array<String>): Promise<Array<HandlebarsTemplateDelegate>>
external fun loadTemplates(paths: ReadonlyRecord<String, String>): Promise<Array<HandlebarsTemplateDelegate>>