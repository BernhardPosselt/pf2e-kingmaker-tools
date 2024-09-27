package com.foundryvtt.core.data.fields

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.FormInputConfig
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLDivElement

@JsPlainObject
external interface DataFieldOptions/*<T>*/ {
    var required: Boolean?
    var nullable: Boolean?

    var initial: Any?
    var readonly: Boolean?
    var gmOnly: Boolean?
    var label: String?
    var hint: String?
    var validationError: String?
}


@JsPlainObject
external interface DataFieldContext<T> {
    val name: String?
    val parent: DataField<T>?
}

@JsPlainObject
external interface CleanOptions {
    val partial: Boolean?
    val source: AnyObject
}

@JsPlainObject
external interface DataFieldValidationOptions {
    val partial: Boolean?
    val fallback: Boolean?
    val source: AnyObject?
    val dropInvalidEmbedded: Boolean?
}

@JsPlainObject
external interface EffectChangeData {
    val key: String
    val value: String
    val mode: Int
    val priority: Int
}


@JsPlainObject
external interface FormGroupConfig<T> {
    val label: String
    val units: String?
    val hint: String?
    val rootId: String?
    val input: Any // HTMLElement or HTMLCollection
    val classes: Array<String>?
    val stacked: Boolean?
    val localize: Boolean?
    val widget: ((
        field: DataField<T>,
        groupConfig: FormInputConfig<T>,
        inputConfig: FormInputConfig<T>
    ) -> HTMLDivElement)?
}
