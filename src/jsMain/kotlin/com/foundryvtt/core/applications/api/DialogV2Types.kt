package com.foundryvtt.core.applications.api

import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.events.Event
import kotlin.js.Promise

@JsPlainObject
external interface DialogV2Button {
    val action: String
    val label: String
    val icon: String?
    val `class`: String?
    val default: Boolean?
    val callback: ((event: Event, button: HTMLButtonElement, dialog: DialogV2) -> Any)?
}

@JsPlainObject
external interface BaseDialogV2Options : ApplicationConfiguration {
    val content: String
    val buttons: Array<DialogV2Button>?

    // this receives either the action of clicked button, or if the button had a
    // callback, the awaited result of said button
    val submit: ((Any) -> Promise<Unit>)?
    val modal: Boolean?
}

@JsPlainObject
external interface DialogV2Options : BaseDialogV2Options {
    override val buttons: Array<DialogV2Button>
}

@JsPlainObject
external interface DialogV2WaitOptions {
    val rejectClose: Boolean?
}

@JsPlainObject
external interface ConfirmOptions : DialogV2WaitOptions, BaseDialogV2Options {
    val yes: DialogV2Button?
    val no: DialogV2Button?
}

@JsPlainObject
external interface PromptOptions : DialogV2WaitOptions, BaseDialogV2Options {
    val ok: DialogV2Button?
}

@JsPlainObject
external interface WaitOptions : DialogV2WaitOptions, BaseDialogV2Options