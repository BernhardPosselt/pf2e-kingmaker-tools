package com.foundryvtt.core.applications.api

import js.objects.ReadonlyRecord
import js.objects.Record
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import kotlin.js.Promise

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

// required to make instance of work, but since the classes are not registered here
// at page load, we can't use @file:JsQualifier
@JsName("foundry.applications.api.HandlebarsApplicationMixin(foundry.applications.api.ApplicationV2)")
@Suppress("NAME_CONTAINS_ILLEGAL_CHARS")
open external class HandlebarsApplication<T>(
    options: ApplicationConfiguration = definedExternally
) : ApplicationV2 {
    @OptIn(ExperimentalStdlibApi::class)
    @JsExternalInheritorsOnly
    open class HandlebarsApplicationStatic : ApplicationV2Static {
        open var PARTS: ReadonlyRecord<String, HandlebarsTemplatePart>
    }

    companion object : HandlebarsApplicationStatic

    open val parts: ReadonlyRecord<String, HandlebarsTemplatePart>

    protected open fun _preparePartContext(
        partId: String,
        context: T,
        options: HandlebarsRenderOptions
    ): Promise<T>

    protected open fun _preSyncPartState(
        partId: String,
        newElement: HTMLElement,
        priorElement: HTMLElement,
        state: Record<String, Any>
    )

    protected open fun _syncPartState(
        partId: String,
        newElement: HTMLElement,
        priorElement: HTMLElement,
        state: Record<String, Any>
    )

    protected open fun _attachPartListeners(partId: String, htmlElement: HTMLElement, options: ApplicationRenderOptions)
}