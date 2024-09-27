package com.foundryvtt.core.collections

import com.foundryvtt.core.abstract.*
import com.foundryvtt.core.documents.User
import com.foundryvtt.core.utils.Collection
import js.collections.JsSet
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface InvalidDocumentOptions {
    val strict: Boolean?
}

@JsPlainObject
external interface ModifySourceOptions {
    val modifySource: Boolean?
}

external class EmbeddedCollection<T>(
    name: String,
    parent: DataModel,
    sourceArray: Array<T>,
) : Collection<T> {
    val documentClass: Document
    val name: String
    val model: DataModel
    val _initialized: Boolean
    val _source: T
    val invalidDocumentIds: JsSet<String>

    fun createDocument(data: T, context: DocumentConstructionContext = definedExternally): Document
    fun initialize(context: DocumentConstructionContext = definedExternally)
    fun _initializeDocument(data: T, context: DocumentConstructionContext)
    fun _handleInvalidDocument(id: String, err: Throwable, options: InvalidDocumentOptions = definedExternally)
    fun set(key: String, value: Document, options: ModifySourceOptions = definedExternally)
    fun _set(key: String, value: Document)
    fun delete(key: String, options: ModifySourceOptions = definedExternally): Boolean
    fun _delete(key: String)
    fun update(changes: Array<DataModel>, options: ModifySourceOptions = definedExternally)
    fun _updateOrCreate(data: DataModel, options: ModifySourceOptions = definedExternally)
    fun getInvalid(id: String, options: InvalidDocumentOptions = definedExternally)
    fun toObject(source: Boolean = definedExternally): Array<T>
    fun _onModifyContents(
        action: DatabaseAction,
        documents: Array<Document>,
        result: Array<Any>,
        operation: DatabaseOperation,
        user: User
    )
}