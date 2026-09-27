@file:JsQualifier("foundry.applications.handlebars")
package com.foundryvtt.core.applications.handlebars

import js.objects.ReadonlyRecord
import kotlin.js.Promise

external fun renderTemplate(path: String, data: Any? = definedExternally): Promise<String>
external fun loadTemplates(paths: Array<String>): Promise<Array<HandlebarsTemplateDelegate>>
external fun loadTemplates(paths: ReadonlyRecord<String, String>): Promise<Array<HandlebarsTemplateDelegate>>