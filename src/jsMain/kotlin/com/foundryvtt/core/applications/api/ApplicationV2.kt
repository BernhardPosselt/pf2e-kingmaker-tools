@file:JsQualifier("foundry.applications.api")

package com.foundryvtt.core.applications.api

import com.foundryvtt.core.AnyObject
import js.objects.ReadonlyRecord
import kotlinx.html.org.w3c.dom.events.Event
import org.w3c.dom.AddEventListenerOptions
import org.w3c.dom.DOMTokenList
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLLIElement
import org.w3c.dom.events.EventListener
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.js.Promise


open external class ApplicationV2(
    options: ApplicationConfiguration = definedExternally
) {
    @OptIn(ExperimentalStdlibApi::class)
    @JsExternalInheritorsOnly
    open class ApplicationV2Static {
        val RENDER_STATES: RenderStates
        val BASE_APPLICATION: JsClass<ApplicationV2>
        open val DEFAULT_OPTIONS: ApplicationConfiguration
        val emittedEvents: Array<String>
        fun parseCSSDimension(style: String, parentDimensions: Int): Int

    }

    companion object : ApplicationV2Static

    val options: ApplicationConfiguration
    val window: ApplicationWindow
    val classList: DOMTokenList
    val id: String
    val title: String
    val element: HTMLElement
    val minimized: Boolean
    val position: ApplicationPosition
    val rendered: Boolean
    val hasFrame: Boolean
    val tabGroups: ReadonlyRecord<String, String>
    val state: Int

    protected open fun _initializeApplicationOptions(options: AnyObject): ApplicationConfiguration
    protected fun _getHeaderControls(): Array<ApplicationHeaderControlsEntry>
    fun render(options: ApplicationRenderOptions = definedExternally): Promise<ApplicationV2>
    fun render(force: Boolean, options: ApplicationRenderOptions = definedExternally): Promise<ApplicationV2>
    protected open fun _configureRenderOptions(options: ApplicationRenderOptions)
    protected open fun _prepareContext(options: ApplicationRenderOptions): Promise<AnyObject>
    protected open fun _renderHtml(context: AnyObject, options: ApplicationRenderOptions): Promise<Any?>
    protected open fun _replaceHtml(result: Any?, content: HTMLElement, options: ApplicationRenderOptions)
    protected open fun _renderFrame(options: ApplicationRenderOptions): Promise<HTMLElement>
    protected open fun _renderHeaderControl(control: HTMLLIElement)
    protected open fun _updateFrame(options: ApplicationRenderOptions)
    protected open fun _insertElement(element: HTMLElement)
    fun close(options: ApplicationClosingOptions = definedExternally): Promise<ApplicationV2>
    protected open fun _removeElement(element: HTMLElement)
    fun setPosition(position: ApplicationPosition? = definedExternally): ApplicationPosition
    protected open fun _updatePosition(position: ApplicationPosition): ApplicationPosition
    fun toggleControls(expanded: Boolean)
    fun minimize(): Promise<Unit>
    fun maximize(): Promise<Unit>
    fun bringToFront()
    fun changeTab(tab: String, group: String, options: ApplicationTabOptions)
    protected open fun _canRender(options: ApplicationRenderOptions): dynamic
    protected open fun _preFirstRender(context: AnyObject, options: ApplicationRenderOptions): Promise<Unit>
    protected open fun _onFirstRender(
        context: AnyObject,
        options: ApplicationRenderOptions
    ): Promise<Unit>

    protected open fun _preRender(context: AnyObject, options: ApplicationRenderOptions): Promise<Unit>
    protected open fun _onRender(context: AnyObject, options: ApplicationRenderOptions)
    protected open fun _preClose(options: ApplicationRenderOptions): Promise<Unit>
    protected open fun _onClose(options: ApplicationRenderOptions): Promise<Unit>
    protected open fun _prePosition(position: ApplicationPosition)
    protected open fun _onPosition(position: ApplicationPosition)
    protected open fun _attachFrameListeners()
    protected open fun _onClickAction(event: PointerEvent, target: HTMLElement)
    protected open fun _onSubmitForm(formConfig: ApplicationFormConfiguration, event: Event): Promise<Unit>
    protected open fun _onChangeForm(formConfig: ApplicationFormConfiguration, event: Event): Promise<Unit>
    protected open fun _awaitTransition(element: HTMLElement, timeout: Int): Promise<Unit>

    fun addEventListener(type: String, listener: EventListener, options: AddEventListenerOptions)
    fun removeEventListener(type: String, listener: EventListener)
    fun dispatchEvent(event: Event): Boolean
}