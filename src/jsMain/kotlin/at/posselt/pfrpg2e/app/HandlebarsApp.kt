@file:JsQualifier("foundryvttKotlinPatches")

package at.posselt.pfrpg2e.app

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.FormDataExtended
import com.foundryvtt.core.applications.api.ApplicationRenderOptions
import com.foundryvtt.core.applications.api.ApplicationV2
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import js.core.Void
import js.objects.Record
import kotlinx.html.org.w3c.dom.events.Event
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLFormElement
import kotlin.js.Promise


@JsName("SaneHandlebarsApplicationV2")
open external class HandlebarsApp<T : HandlebarsRenderContext>(
    options: HandlebarsFormApplicationOptions,
) : ApplicationV2 {
    protected open fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
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

    protected open fun _attachPartListeners(
        partId: String,
        htmlElement: HTMLElement,
        options: ApplicationRenderOptions
    )

    protected open fun onSubmit(
        event: Event,
        form: HTMLFormElement,
        formData: FormDataExtended<AnyObject>
    ): Promise<Void>
}