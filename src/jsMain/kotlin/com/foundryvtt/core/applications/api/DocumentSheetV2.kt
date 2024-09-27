@file:JsQualifier("foundry.applications.api.DocumentSheetV2")

package com.foundryvtt.core.applications.api

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.FormDataExtended
import com.foundryvtt.core.abstract.Document
import kotlinx.html.org.w3c.dom.events.Event
import org.w3c.dom.HTMLFormElement
import kotlin.js.Promise


open external class DocumentSheetV2<D : Document>(
    options: DocumentSheetConfiguration<D> = definedExternally
) : ApplicationV2 {
    val document: D
    val isVisible: Boolean
    val isEditable: Boolean
    protected open fun _prepareSubmitData(
        event: Event,
        form: HTMLFormElement,
        formData: FormDataExtended<AnyObject>
    ): AnyObject

    protected open fun _processFormData(
        event: Event,
        form: HTMLFormElement,
        formData: FormDataExtended<AnyObject>
    ): AnyObject

    protected open fun _processSubmitData(
        event: Event,
        form: HTMLFormElement,
        submitData: AnyObject,
    ): Promise<Unit>

    fun submit(option: SubmitOptions = definedExternally): Promise<Unit>
}