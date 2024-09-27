package at.posselt.pfrpg2e.app

import com.foundryvtt.core.applications.api.ApplicationConfiguration
import com.foundryvtt.core.applications.api.HandlebarsTemplatePart
import js.objects.ReadonlyRecord
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface HandlebarsFormApplicationOptions : ApplicationConfiguration {
    val parts: ReadonlyRecord<String, HandlebarsTemplatePart>?
}

@JsPlainObject
external interface HandlebarsRenderContext {
    val partId: String
}