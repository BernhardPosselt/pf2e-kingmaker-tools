package at.posselt.pfrpg2e.utils

import js.objects.Object
import js.objects.Record
import js.reflect.Reflect


@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class DocumentUpdateDsl

@DocumentUpdateDsl
open class PropertyUpdateBuilder<T>(
    val basePath: String,
    val updates: Record<String, Any?>,
    val propertyName: String
) {
    protected val propertyPath = if (basePath == "") propertyName else "$basePath.$propertyName"
    fun delete() {
        Object.keys(updates)
            .filter { it.startsWith("$propertyPath.") || it == propertyPath }
            .forEach { Reflect.deleteProperty(updates, it) }
        updates["$basePath-=$propertyName"] = null
    }

    fun set(value: T) {
        updates[propertyPath] = value
    }
}

@DocumentUpdateDsl
open class RecordPropertyUpdateBuilder<T>(
    basePath: String,
    updates: Record<String, Any?>,
    propertyName: String
) : PropertyUpdateBuilder<T>(basePath, updates, propertyName) {
    fun deleteEntry(key: String) {
        Reflect.deleteProperty(updates, "$propertyPath.$key")
        updates["$propertyPath.-=$key"] = null
    }

    fun deleteEntries(keys: Set<String>) = keys.forEach(::deleteEntry)

    operator fun set(key: String, value: T) {
        updates["$propertyPath.$key"] = value
    }
}
