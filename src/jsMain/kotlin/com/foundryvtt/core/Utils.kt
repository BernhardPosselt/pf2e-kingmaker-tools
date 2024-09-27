package com.foundryvtt.core

import com.foundryvtt.core.abstract.Document
import js.objects.ReadonlyRecord
import js.objects.Record
import kotlinx.js.JsPlainObject
import kotlin.js.Promise

typealias AnyObject = ReadonlyRecord<String, Any?>
typealias AnyMutableObject = Record<String, Any?>
typealias AudioContext = Any // not yet available in Kotlin yet, dom API

@JsPlainObject
external interface FromUuidOptions {
    val relative: Document?
    val invalid: Boolean?
}

external fun fromUuid(uuid: String, options: FromUuidOptions = definedExternally): Promise<Document?>
