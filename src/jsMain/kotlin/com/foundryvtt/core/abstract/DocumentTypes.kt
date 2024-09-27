package com.foundryvtt.core.abstract

import com.foundryvtt.core.AnyObject
import js.objects.ReadonlyRecord
import js.objects.jso
import kotlinx.js.JsPlainObject
import kotlin.js.Promise


typealias DatabaseAction = String

sealed external interface DatabaseOperation

@JsPlainObject
external interface DatabaseGetOperation : DatabaseOperation {
    val query: ReadonlyRecord<String, Any>?
    val broadcast: Boolean?
    val index: Boolean?
    val indexFields: Array<String>?
    val pack: String?
    val parent: Document
    val parentUuid: String?
}

@JsPlainObject
external interface DatabaseCreateOperation : DatabaseOperation {
    val broadcast: Boolean?
    val data: Array<AnyObject>?
    val keepId: Boolean?
    val keepEmbeddedIds: Boolean?
    val modifiedTime: Int?
    val noHook: Boolean?
    val render: Boolean?
    val renderSheet: Boolean?
    val parent: Document?
    val pack: String?
    val parentUuid: String?
    val _result: Array<Any>?
}

@JsPlainObject
external interface DatabaseUpdateOperation : DatabaseOperation {
    val modifiedTime: Int?
    val diff: Boolean?
    val recursive: Boolean?
    val render: Boolean?
    val broadcast: Boolean?
    val updates: Array<AnyObject>?
    val noHook: Boolean?
    val parent: Document?
    val pack: String?
    val parentUuid: String?
    val _result: Array<Any>?
}

@JsPlainObject
external interface DatabaseDeleteOperation : DatabaseOperation {
    val broadcast: Boolean?
    val ids: Array<String>?
    val deleteAll: Boolean?
    val modifiedTime: Int?
    val noHook: Boolean?
    val render: Boolean?
    val parent: Document?
    val pack: String?
    val parentUuid: String?
    val _result: Array<Any>?
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
fun Document.update(data: Document, operation: DatabaseUpdateOperation = jso()): Promise<Document?> =
    update(data as AnyObject, operation)